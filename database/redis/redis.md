# REDIS

## 1.Redis 底层数据结构

### 1-1.Redis dict(字典)

dict类似java中map，是键值对。Redis很多操作都基于dict。

dict字典中有两个哈希表，一般使用ht[0]，只有在rehash(扩容和缩减)时才使用ht[1].

```c
//字典
typedef struct dict {
    dictType *type; //字典类型
    void *privdata; //私有数据
    dictht ht[2];	//哈希表，一般有两部分，正常哈希表数据部分 + rehash重置哈希表的暂存数据部分
    long rehashidx; //是否正在进行哈希重置，默认为不重置 rehashidx == -1
    unsigned long iterators; // 正在运行的迭代器数
}

//哈希表
typedef struct dictht {
    dictEntry **table; //哈希表数据，数组+链表
    unsigned long size; //size 2的幂次
    unsigned long sizemask;//size-1
    unsigned long used; //存储数量
}

//每个间值对元素，链表
typedef struct dictEntry {
    void *key; //键
    union {
        void *val;
        uint64_t u64;
        int64_t s64;
        double d;
    } v; //值
    struct dictEntry *next; //指向下一个 hash 节点的地址
}
```

### 哈希表的新增:

​		1.根据key计算hash

​		2.hash与table.size-1相与，取到当前key存储的table的位置

​		3.新节点插入到表头

```c
int dictAdd(dict *d, void *key, void *val){
    //设置一个只有空值的Entry
    dictEntry *entry = dictAddRaw(d,key,NULL);
    if (!entry) return DICT_ERR;
    //设置Entry的val
    dictSetVal(d, entry, val);
    return DICT_OK;
}

dictEntry *dictAddRaw(dict *d, void *key, dictEntry **existing){
    long index;
    dictEntry *entry;
    dictht *ht;

    if (dictIsRehashing(d)) _dictRehashStep(d);

    //-1代表key已经存在，否使new一个新的Entry
    if ((index = _dictKeyIndex(d, key, dictHashKey(d,key), existing)) == -1)
        return NULL;

    ht = dictIsRehashing(d) ? &d->ht[1] : &d->ht[0];
    entry = zmalloc(sizeof(*entry));
    //新节点插入到表头
    entry->next = ht->table[index];
    ht->table[index] = entry;
    ht->used++;

    dictSetKey(d, entry, key);
    return entry;
}
```

### 字典rehash：

​		触发rehash：1.调用_dictRehashStep()

​								2.定时dictRehashMilliseconds()



​		扩容将ht[0]数据重新hash迁移到ht[1],新表的size是旧表的2倍，当前迁移时设置

​		rehashidx标识，读写时会往新表里读写，直到ht[0]迁移结束，将ht[1]指向ht[0]，

​		最后更新rehashidx标识。

```c
int dictRehash(dict *d, int n) {
    int empty_visits = n*10; 
    if (!dictIsRehashing(d)) return 0;

    while(n-- && d->ht[0].used != 0) {
        dictEntry *de, *nextde;

        assert(d->ht[0].size > (unsigned long)d->rehashidx);
        while(d->ht[0].table[d->rehashidx] == NULL) {
            d->rehashidx++;
            if (--empty_visits == 0) return 1;
        }
        de = d->ht[0].table[d->rehashidx];
      
        while(de) {
            uint64_t h;

            nextde = de->next;
       
            h = dictHashKey(d, de->key) & d->ht[1].sizemask;
            de->next = d->ht[1].table[h];
            d->ht[1].table[h] = de;
            d->ht[0].used--;
            d->ht[1].used++;
            de = nextde;
        }
        d->ht[0].table[d->rehashidx] = NULL;
        d->rehashidx++;
    }

    if (d->ht[0].used == 0) {
        zfree(d->ht[0].table);
        d->ht[0] = d->ht[1];
        _dictReset(&d->ht[1]);
        d->rehashidx = -1;
        return 0;
    }
    return 1;
}
```

### 1-2. ziplist压缩列表

```c
typedef struct zlentry {
    unsigned int prevrawlensize; //前一个节点的长度
    unsigned int prevrawlen;    
    unsigned int lensize;      
    unsigned int len;          
    unsigned int headersize;    
    unsigned char encoding;     
    unsigned char *p;           
}
```



## 2.Redis常用类型

### 2-1.Hash类型:

```

```



## 3.Redis数据库

```c
struct redisServer {
    redisDb *db;//redis 数据库
    int dbnum; //数据库数量
}

typedef struct redisDb {
    dict *dict;                 //redis的KEY映射
    dict *expires;              //过期时间
    dict *blocking_keys;        /* Keys with clients waiting for data (BLPOP)*/
    dict *ready_keys;           /* Blocked keys that received a PUSH */
    dict *watched_keys;         /* WATCHED keys for MULTI/EXEC CAS */
    int id;                     //数据库id
    long long avg_ttl;          /* Average TTL, just for stats */
    unsigned long expires_cursor; /* Cursor of the active expire cycle. */
    list *defrag_later;         /* List of key names to attempt to defrag one by one, gradually. */
} redisDb;
```

