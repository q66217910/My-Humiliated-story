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

ziplist压缩列表结构:

​	zlbytes(整个压缩列表占用字节数)->zltail(压缩列表起始节点到尾节点的字节数)->zllen(压缩列表节点长度)

->entry...(压缩列表存储节点链表)->zlend(存储特殊值0xFF ( =255 ) ，用于标记压缩列表尾端)

```c
//每一个节点,都能存储一个字节数组或整数值
typedef struct zlentry {
    unsigned int prevrawlensize; //前一个节点的长度
    unsigned int prevrawlen;     //前一个节点编码所需长度   
    unsigned int lensize;        //当前节点长度   
    unsigned int len;       	 //当前节点编码所需长度
    unsigned int headersize;     //头大小 
    unsigned char encoding;      // 编码类型 
    unsigned char *p;            // 字节指针 
}

previous_entry_length : 即（前一个节点指针地址=当前节点指针地址-previous_entry_length）
    	用于逆向遍历。若节点长度小于254,则前一个节点属性长度为1个字节，
    	否则一个节点的属性长度为5个字节。
encoding  :  记录了节点content属性所保存数据的类型和长度
```

### ziplist的创建

​		1.申请表头和表尾的内存（10B+1B）

​		2.设置zlbytes/zltail/zllen/zlend的值

```c
//ZIPLIST头存储长度 32位:总字节数 32位:到最后一个节点的偏移量  16位:节点字段数量
//zlbytes+zltail+zllen
define ZIPLIST_HEADER_SIZE     (sizeof(uint32_t)*2+sizeof(uint16_t))
//存储特殊值0xFF ( =255 ) ，用于标记压缩列表尾端  1个字节
define ZIPLIST_END_SIZE        (sizeof(uint8_t))
define ZIP_END 255
    
unsigned char *ziplistNew(void) {
    //ziplist头+尾需要的内存
    unsigned int bytes = ZIPLIST_HEADER_SIZE+ZIPLIST_END_SIZE;
    //申请分配内存
    unsigned char *zl = zmalloc(bytes);
    //ziplist总字节数
    ZIPLIST_BYTES(zl) = intrev32ifbe(bytes);
    //设定尾节点相对头部的偏移量
    ZIPLIST_TAIL_OFFSET(zl) = intrev32ifbe(ZIPLIST_HEADER_SIZE);
    //节点数量,初始0个
    ZIPLIST_LENGTH(zl) = 0;
    //设置最后一个字节为结束标记符
    zl[bytes-1] = ZIP_END;
    return zl;
}
```

### ziplist的新增

​		1.获取上一个节点的偏移量

​		2.将当前内容进行编码，计算出当前节点的编码长度

​		3.判断空间是否足够插入

​		4.扩容

​		5.插入节点并设置节点值

```c
define ZIPLIST_HEAD 0
//前一个节点的最大字节
define ZIP_BIG_PREVLEN 254
//返回第一个节点的指针
define ZIPLIST_ENTRY_HEAD(zl)  ((zl)+ZIPLIST_HEADER_SIZE)
//返回节点链表的最后指针，即尾节点的前一个节点
define ZIPLIST_ENTRY_END(zl)   ((zl)+intrev32ifbe(ZIPLIST_BYTES(zl))-1)
//最后一个节点的偏移量
define ZIPLIST_TAIL_OFFSET(zl) (*((uint32_t*)((zl)+sizeof(uint32_t))))
define ZIPLIST_ENTRY_TAIL(zl)  ((zl)+intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl)))
    
//返回前一个元素的长度和字节数 
define ZIP_DECODE_PREVLENSIZE(ptr, prevlensize) do {  
    //若节点长度小于254,则前一个节点属性长度为1个字节，否则一个节点的属性长度为5个字节
    if ((ptr)[0] < ZIP_BIG_PREVLEN) {                                          
        (prevlensize) = 1;                                                     
    } else {                                                                   
        (prevlensize) = 5;                                                     
    }                                                                          
} while(0);
define ZIP_DECODE_PREVLEN(ptr, prevlensize, prevlen) do {     
    //计算前一个节点属性的长度
    ZIP_DECODE_PREVLENSIZE(ptr, prevlensize);                                  
    if ((prevlensize) == 1) {  
        //只有一位,当前节点的值就是前一个节点的长度
        (prevlen) = (ptr)[0];                                                  
    } else if ((prevlensize) == 5) {                                           
        assert(sizeof((prevlen)) == 4);
        //5位，第一个字节(0xFE( =254 )),后4个字节保存前一个节点的长度
        memcpy(&(prevlen), ((char*)(ptr)) + 1, 4);                             
        memrev32ifbe(&prevlen);                                                
    }                                                                          
} while(0);

//计算节点长度
unsigned int zipRawEntryLength(unsigned char *p) {
    unsigned int prevlensize, encoding, lensize, len;
    //计算前一个节点属性的长度
    ZIP_DECODE_PREVLENSIZE(p, prevlensize);
    ZIP_DECODE_LENGTH(p + prevlensize, encoding, lensize, len);
    return prevlensize + lensize + len;
}

//往表尾添加
unsigned char *ziplistPush(unsigned char *zl, unsigned char *s,
                           unsigned int slen, int where) {
    unsigned char *p;
    //where==0返回第一个节点的起始位置，否则返回尾位置
    p = (where == ZIPLIST_HEAD) ? ZIPLIST_ENTRY_HEAD(zl) : ZIPLIST_ENTRY_END(zl);
    return __ziplistInsert(zl,p,s,slen);
}

//往表头添加
unsigned char *ziplistInsert(unsigned char *zl, unsigned char *p, 
                             unsigned char *s, unsigned int slen) {
    return __ziplistInsert(zl,p,s,slen);
}

unsigned char *__ziplistInsert(unsigned char *zl, unsigned char *p, 
                               unsigned char *s, unsigned int slen) {
    //当前压缩链表长度
    size_t curlen = intrev32ifbe(ZIPLIST_BYTES(zl)), reqlen;
    //前一个节点的长度
    unsigned int prevlensize, prevlen = 0;
    size_t offset;
    int nextdiff = 0;
    unsigned char encoding = 0;
    //初始化失败警告
    long long value = 123456789;
    zlentry tail;

    //如果不是结尾(0xff)代表不是空表
    if (p[0] != ZIP_END) {
     	//获取上一个节点的长度和上一个节点编码长度
        ZIP_DECODE_PREVLEN(p, prevlensize, prevlen);
    } else {
        //尾节点地址
        unsigned char *ptail = ZIPLIST_ENTRY_TAIL(zl);
        if (ptail[0] != ZIP_END) {
            //若不是空表，计算尾节点长度
            prevlen = zipRawEntryLength(ptail);
        }
    }

    //先尝试整数编码
    if (zipTryEncoding(s,slen,&value,&encoding)) {
       	// 整数编码长度
        reqlen = zipIntSize(encoding);
    } else {
       	//字符编码
        reqlen = slen;
    }
   
    // 前一个节点的编码长度
    reqlen += zipStorePrevEntryLength(NULL,prevlen);
    //当前节点的编码长度
    reqlen += zipStoreEntryEncoding(NULL,encoding,slen);

    int forcelarge = 0;
    //若不是尾端插入,zipPrevLenByteDiff判断空间是否足够，nextdiff:相差多少空间
    nextdiff = (p[0] != ZIP_END) ? zipPrevLenByteDiff(p,reqlen) : 0;
    //nextdiff:相差为 -4 代表需要扩展
    if (nextdiff == -4 && reqlen < 4) {
        nextdiff = 0;
        forcelarge = 1;
    }
    
	// 存储p相对于列表zl的偏移地址，这里存储 偏移量而不是直接 指针地址，是因为可能 扩展空间而地址改变
    offset = p-zl;
    // 扩展空间，curlen 当前列表长度，reqlen 新节点长度，新节点后继节点header 偏移长度
    zl = ziplistResize(zl,curlen+reqlen+nextdiff);
    //p节点的新地址
    p = zl+offset;

   // 非表尾插入，需要重新计算表尾的偏移量
    if (p[0] != ZIP_END) {
       	// 移动现有元素，为新元素的插入提供空间
        memmove(p+reqlen,p-nextdiff,curlen-offset-1+nextdiff);
        // p+reqlen为新节点前置节点移动后的位置，将新节点的长度编码至前置节点
        if (forcelarge)
            zipStorePrevEntryLengthLarge(p+reqlen,reqlen);
        else
            zipStorePrevEntryLength(p+reqlen,reqlen);

        // 更新列表尾相对于表头的偏移量，将新节点的长度算上
        ZIPLIST_TAIL_OFFSET(zl) =
            intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))+reqlen);

       // 如果新节点后面有多个节点，那么表尾的偏移量需要算上nextdiff的值
        zipEntry(p+reqlen, &tail);
        if (p[reqlen+tail.headersize+tail.len] != ZIP_END) {
            ZIPLIST_TAIL_OFFSET(zl) =
                intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))+nextdiff);
        }
    } else {
        // 表尾插入，直接计算偏移量
        ZIPLIST_TAIL_OFFSET(zl) = intrev32ifbe(p-zl);
    }

    // 当nextdiff不为0时，表示需要新节点的后继节点对头部进行扩展
    if (nextdiff != 0) {
        offset = p-zl;
        // 需要对p所指向的机电header进行扩展更新，有可能会引起连锁更新
        zl = __ziplistCascadeUpdate(zl,p+reqlen);
        p = zl+offset;
    }

   	// 将新节点前置节点的长度写入新节点的header
    p += zipStorePrevEntryLength(p,prevlen);
    // 将新节点的值长度写入新节点的header
    p += zipStoreEntryEncoding(p,encoding,slen);
    // 写入节点值
    if (ZIP_IS_STR(encoding)) {
        memcpy(p,s,slen);
    } else {
        zipSaveInteger(p,value,encoding);
    }
    // 更新列表节点计数
    ZIPLIST_INCR_LENGTH(zl,1);
    return zl;
}

//ziplist扩容
unsigned char *ziplistResize(unsigned char *zl, unsigned int len) {
    zl = zrealloc(zl,len);
    ZIPLIST_BYTES(zl) = intrev32ifbe(len);
    zl[len-1] = ZIP_END;
    return zl;
}

```

### ziplist的查询

​		1.根据index判断是正向查询还是逆向查询

​			2.若是正向，获取头节点的偏移量，后移index个节点

​			3.若是逆向,	获取为节点的偏移量, 每个节点都记录了前置节点的长度，前移index个节点

```C
unsigned char *ziplistIndex(unsigned char *zl, int index) {
    unsigned char *p;
    unsigned int prevlensize, prevlen = 0;
    // index为负，从尾部开始遍历
    if (index < 0) {
        index = (-index)-1;
        // 获取尾指针
        p = ZIPLIST_ENTRY_TAIL(zl);
        if (p[0] != ZIP_END) {
            // 解码前置节点长度
            ZIP_DECODE_PREVLEN(p, prevlensize, prevlen);
            while (prevlen > 0 && index--) {
                p -= prevlen;
                // 解码前置节点长度
                ZIP_DECODE_PREVLEN(p, prevlensize, prevlen);
            }
        }
    } else {
        // index为正，从头部开始遍历
        p = ZIPLIST_ENTRY_HEAD(zl);
        while (p[0] != ZIP_END && index--) {
             // 获取当前节点的整体长度，包括pre_entry_length，encoding，contents三部分
            p += zipRawEntryLength(p);
        }
    }
    return (p[0] == ZIP_END || index > 0) ? NULL : p;
}
```

### ziplist的删除

```c
unsigned char *ziplistDelete(unsigned char *zl, unsigned char **p) {
    size_t offset = *p-zl;
    zl = __ziplistDelete(zl,*p,1);
    *p = zl+offset;
    return zl;
}

unsigned char *__ziplistDelete(unsigned char *zl, unsigned char *p, unsigned int num) {
    unsigned int i, totlen, deleted = 0;
    size_t offset;
    int nextdiff = 0;
    zlentry first, tail;

    // 获取p指向的节点信息
    zipEntry(p, &first);
    // 计算num个节点占用的内存
    for (i = 0; p[0] != ZIP_END && i < num; i++) {
        p += zipRawEntryLength(p);
        deleted++;
    }

    totlen = p-first.p; 
    if (totlen > 0) {
        if (p[0] != ZIP_END) {
            // 执行到这里，表示被删除节点后面还存在节点
            // 判断最后一个被删除的节点的后继节点的header中的存放前置节点长度的空间
            // 能不能容纳第一个被删除节点的前置节点的长度
            nextdiff = zipPrevLenByteDiff(p,first.prevrawlen);
            p -= nextdiff;
            zipStorePrevEntryLength(p,first.prevrawlen);

             // 更新尾部相对于头部的偏移量
            ZIPLIST_TAIL_OFFSET(zl) =
                intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))-totlen);

             // 如果被删除节点后面还存在节点，就需要将nextdiff计算在内
            zipEntry(p, &tail);
            if (p[tail.headersize+tail.len] != ZIP_END) {
                ZIPLIST_TAIL_OFFSET(zl) =
                   intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))+nextdiff);
            }

           // 将被删除节点后面的内存空间移动到删除的节点之后
            memmove(first.p,p,
                intrev32ifbe(ZIPLIST_BYTES(zl))-(p-zl)-1);
        } else {
             // 执行到这里，表示被删除节点后面没有节点了
            ZIPLIST_TAIL_OFFSET(zl) =
                intrev32ifbe((first.p-zl)-first.prevrawlen);
        }

      	// 缩小内存并更新ziplist的长度
        offset = first.p-zl;
        zl = ziplistResize(zl, intrev32ifbe(ZIPLIST_BYTES(zl))-totlen+nextdiff);
        ZIPLIST_INCR_LENGTH(zl,-deleted);
        p = zl+offset;

        // 如果nextdiff不等于0，说明被删除节点后面节点的header信息还需要更改
        if (nextdiff != 0)
             // 连锁更新
            zl = __ziplistCascadeUpdate(zl,p);
    }
    return zl;
}

unsigned char *__ziplistCascadeUpdate(unsigned char *zl, unsigned char *p) {
    size_t curlen = intrev32ifbe(ZIPLIST_BYTES(zl)), rawlen, rawlensize;
    size_t offset, noffset, extra;
    unsigned char *np;
    zlentry cur, next;
    while (p[0] != ZIP_END) {
        // 将p所指向节点的信息保存到cur结构体中
        zipEntry(p, &cur);
        // 当前节点的长度
        rawlen = cur.headersize + cur.len;
        // 编码当前节点的长度所需的字节数
        rawlensize = zipStorePrevEntryLength(NULL,rawlen);
        // 如果没有后续节点需要更新了，就退出
        if (p[rawlen] == ZIP_END) break;
        // 去除后续节点的信息保存到next结构体中
        zipEntry(p+rawlen, &next);
        // 当后续节点的空间已经足够了，就直接退出
        if (next.prevrawlen == rawlen) break;
        // 当后续节点的空间不足够，则需要进行扩容操作
        if (next.prevrawlensize < rawlensize) {
            // 记录p的偏移值
            offset = p-zl;
            // 记录需要增加的长度
            extra = rawlensize-next.prevrawlensize;
            // 扩展zl的大小
            zl = ziplistResize(zl,curlen+extra);
            // 获取p相对于新的zl的值
            p = zl+offset;
            // 记录下一个节点的偏移量
            np = p+rawlen;
            noffset = np-zl;
            // 当 next 节点不是表尾节点时，更新列表到表尾节点的偏移量
            if ((zl+intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))) != np) {
                ZIPLIST_TAIL_OFFSET(zl) =
                    intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))+extra);
            }
            // 向后移动cur节点之后的数据，为新的header腾出空间
            memmove(np+rawlensize,
                np+next.prevrawlensize,
                curlen-noffset-next.prevrawlensize-1);
            zipStorePrevEntryLength(np,rawlen);
            // 移动指针，继续处理下一个节点
            p += rawlen;
            curlen += extra;
        } else {
            if (next.prevrawlensize > rawlensize) {
                // 执行到这里，next节点编码前置节点的header空间有5个字节，但是此时只需要一个字节，Redis不提供缩小操作，而是直接将长度强制性写入五个字节中
                zipStorePrevEntryLengthLarge(p+rawlen,rawlen);
            } else {
                // 运行到这里，说明刚好可以存放
                zipStorePrevEntryLength(p+rawlen,rawlen);
            }
            // 退出，代表空间足够，后续空间不需要更改
            break;
        }
    }
    return zl;
}
```



## 2.Redis数据库

```c
struct redisServer {
    redisDb *db;//redis 数据库
    int dbnum; //数据库数量
}

typedef struct redisDb {
    dict *dict;                 //redis的KEY映射 Map<key,redisObject>
    dict *expires;              //过期时间
    dict *blocking_keys;        /* Keys with clients waiting for data (BLPOP)*/
    dict *ready_keys;           /* Blocked keys that received a PUSH */
    dict *watched_keys;         /* WATCHED keys for MULTI/EXEC CAS */
    int id;                     //数据库id
    long long avg_ttl;          /* Average TTL, just for stats */
    unsigned long expires_cursor; /* Cursor of the active expire cycle. */
    list *defrag_later;         /* List of key names to attempt to defrag one by one, gradually. */
} redisDb;

//redis type类型
define OBJ_STRING 0  
define OBJ_LIST 1      
define OBJ_SET 2      
define OBJ_ZSET 3     
define OBJ_HASH 4  
define OBJ_MODULE 5  
define OBJ_STREAM 6 

//encoding ，也表示ptr的数据结构
define OBJ_ENCODING_RAW 0     
define OBJ_ENCODING_INT 1    
define OBJ_ENCODING_HT 2  //字典  
define OBJ_ENCODING_ZIPMAP 3 
define OBJ_ENCODING_LINKEDLIST 4 
define OBJ_ENCODING_ZIPLIST 5   //压缩列表
define OBJ_ENCODING_INTSET 6
define OBJ_ENCODING_SKIPLIST 7 
define OBJ_ENCODING_EMBSTR 8 
define OBJ_ENCODING_QUICKLIST 9
define OBJ_ENCODING_STREAM 10  

typedef struct redisObject {
    unsigned type:4; //redis type(OBJ_STRING|OBJ_LIST|OBJ_SET|OBJ_ZSET|OBJ_HASH|OBJ_MODULE|OBJ_STREAM)
    unsigned encoding:4;
    unsigned lru:LRU_BITS; //全局时钟  LFU的8位+时间戳16位
    int refcount;
    void *ptr; //底层数据指针
}
```

### 2-1.根据key获取redisObject对象

```c
define LOOKUP_NONE 0
robj *lookupKeyWrite(redisDb *db, robj *key) {
    return lookupKeyWriteWithFlags(db, key, LOOKUP_NONE);
}

robj *lookupKeyWriteWithFlags(redisDb *db, robj *key, int flags) {
    //是否需要过期
    expireIfNeeded(db,key);
    //查找key
    return lookupKey(db,key,flags);
}

robj *lookupKey(redisDb *db, robj *key, int flags) {
    //从dh.dict字典中查询key
    dictEntry *de = dictFind(db->dict,key->ptr);
    if (de) {
        //若是存在,获取对象
        robj *val = dictGetVal(de);
        //更新
        if (!hasActiveChildProcess() && !(flags & LOOKUP_NOTOUCH)){
            if (server.maxmemory_policy & MAXMEMORY_FLAG_LFU) {
                updateLFU(val);
            } else {
                val->lru = LRU_CLOCK();
            }
        }
        return val;
    } else {
        //字段中没有返回NULL
        return NULL;
    }
}
```

### 2-2.创建redisObject对象

```c
define MAXMEMORY_FLAG_LFU (1<<1) 

robj *createObject(int type, void *ptr) {
    //申请对象空间
    robj *o = zmalloc(sizeof(*o));
    //设置object类型
    o->type = type;
    o->encoding = OBJ_ENCODING_RAW;
    o->ptr = ptr;
    o->refcount = 1;

	//设置LRU时钟或者LFU计数器
    //maxmemory_policy:过期策略
    if (server.maxmemory_policy & MAXMEMORY_FLAG_LFU) {
        o->lru = (LFUGetTimeInMinutes()<<8) | LFU_INIT_VAL;
    } else {
        o->lru = LRU_CLOCK();
    }
    return o;
}

unsigned int LRU_CLOCK(void) {
    unsigned int lruclock;
    if (1000/server.hz <= LRU_CLOCK_RESOLUTION) {
        lruclock = server.lruclock;
    } else {
        lruclock = getLRUClock();
    }
    return lruclock;
}
```



## 3.Redis常用类型

### 3-1.Hash类型:

```
 Hash类型底层的数据结构为:压缩列表 ziplist 和 字典 dict 
 
 当满足这两条件时,以ziplist存储,否则转化为dict。
 1.当键值对小于hash-max-ziplist-entries(默认128)
 2.保存的所有键值对的长度都小于hash-max-ziplist-value(默认64)字节
```

#### ziplist转化为dict

```c
void hashTypeConvert(robj *o, int enc) {
    if (o->encoding == OBJ_ENCODING_ZIPLIST) {
        hashTypeConvertZiplist(o, enc);
    } else if (o->encoding == OBJ_ENCODING_HT) {
        serverPanic("Not implemented");
    } else {
        serverPanic("Unknown hash encoding");
    }
}

define DICT_OK 0
define C_ERR                   -1
    
define OBJ_HASH_KEY 1
define OBJ_HASH_VALUE 2
//ziplist转化为其他类型,enc:要转化成的类型
void hashTypeConvertZiplist(robj *o, int enc) {
    serverAssert(o->encoding == OBJ_ENCODING_ZIPLIST);

    if (enc == OBJ_ENCODING_ZIPLIST) {
		//ziplist转ziplist，不需要操作
    } else if (enc == OBJ_ENCODING_HT) {
        //ziplist转dict
        hashTypeIterator *hi;
        dict *dict;
        int ret;
		// 新建迭代器
        hi = hashTypeInitIterator(o);
        //创建一个字典
        dict = dictCreate(&hashDictType, NULL);
		// 遍历元素
        while (hashTypeNext(hi) != C_ERR) {
            sds key, value;

            // 键值对在 压缩列表中是连续成对出现，所以获取都是连续读取
            key = hashTypeCurrentObjectNewSds(hi,OBJ_HASH_KEY);
            value = hashTypeCurrentObjectNewSds(hi,OBJ_HASH_VALUE);
            //将键值添加到字典
            ret = dictAdd(dict, key, value);
            if (ret != DICT_OK) {
                //添加失败
                serverLogHexDump(LL_WARNING,"ziplist with dup elements dump",
                    o->ptr,ziplistBlobLen(o->ptr));
                serverPanic("Ziplist corruption detected");
            }
        }
        // 释放迭代器
        hashTypeReleaseIterator(hi);
        //释放ziplist的内存
        zfree(o->ptr);
        //编码设置为字典
        o->encoding = OBJ_ENCODING_HT;
        //数据指向新建的字典
        o->ptr = dict;
    } else {
        serverPanic("Unknown hash encoding");
    }
}
```

#### hash类型查询并创建对象

​		1.从redisDb.dict中根据key查询

​				2.若对象不存在则创建一个初始HashObject(即redisObject   type:OBJ_HASH），

​				初始结构为ziplist，并保存dbAdd()

​				3.若对象存在，则判断是否是HASH类型，若类型错误返回NULL

```c
robj *hashTypeLookupWriteOrCreate(client *c, robj *key) {
    //根据key获取对象
    robj *o = lookupKeyWrite(c->db,key);
    if (o == NULL) {
        //对象不存在，创建一个Hash类型的对象
        o = createHashObject();
        //新增key
        dbAdd(c->db,key,o);
    } else {
        //对象存在,类型不是OBJ_HASH,类型不正确则返回NULL
        if (o->type != OBJ_HASH) {
            addReply(c,shared.wrongtypeerr);
            return NULL;
        }
    }
    return o;
}

define OBJ_HASH 4
robj *createHashObject(void) {
    //创建一个压缩链表
    unsigned char *zl = ziplistNew();
    //创建一个redisObject对象，type为OBJ_HASH,数据为压缩链表
    robj *o = createObject(OBJ_HASH, zl);
    //编码也也会压缩列表
    o->encoding = OBJ_ENCODING_ZIPLIST;
    return o;
}

void dbAdd(redisDb *db, robj *key, robj *val) {
    sds copy = sdsdup(key->ptr);
    //新key增加到redisDb的字典中
    int retval = dictAdd(db->dict, copy, val);

    serverAssertWithInfo(NULL,key,retval == DICT_OK);
    if (val->type == OBJ_LIST ||
        val->type == OBJ_ZSET ||
        val->type == OBJ_STREAM)
        signalKeyAsReady(db, key);
    if (server.cluster_enabled) slotToKeyAdd(key->ptr);
}
```



####  hset指令

```c
void hsetCommand(client *c) {
    int i, created = 0;
    robj *o;
	// 验证是否缺少参数，正常参数应该是 偶数(键值对)
    if ((c->argc % 2) == 1) {
        addReplyError(c,"wrong number of arguments for HMSET");
        return;
    }
	//查找对象,若查找不到则创建一个新的对象
    //若返回值是NULL则说明key存在但类型不正确
    if ((o = hashTypeLookupWriteOrCreate(c,c->argv[1])) == NULL) return;
    
    //判断是否需要将 ziplist 转为 hashtable
    hashTypeTryConversion(o,c->argv,2,c->argc-1);

    for (i = 2; i < c->argc; i += 2)
        created += !hashTypeSet(o,c->argv[i]->ptr,c->argv[i+1]->ptr,HASH_SET_COPY);

    /* HMSET (deprecated) and HSET return value is different. */
    char *cmdname = c->argv[0]->ptr;
    if (cmdname[1] == 's' || cmdname[1] == 'S') {
        /* HSET */
        addReplyLongLong(c, created);
    } else {
        /* HMSET */
        addReply(c, shared.ok);
    }
    signalModifiedKey(c,c->db,c->argv[1]);
    notifyKeyspaceEvent(NOTIFY_HASH,"hset",c->argv[1],c->db->id);
    server.dirty++;
}

//判断是否需要将 ziplist 转为 hashtable
void hashTypeTryConversion(robj *o, robj **argv, int start, int end) {
    int i;
	//当前对象类型已经是字典直接返回
    if (o->encoding != OBJ_ENCODING_ZIPLIST) return;

    for (i = start; i <= end; i++) {
        //若插入的数量大小超过hash_max_ziplist_value时转化为字典结构
        if (sdsEncodedObject(argv[i]) &&
            sdslen(argv[i]->ptr) > server.hash_max_ziplist_value){
            hashTypeConvert(o, OBJ_ENCODING_HT);
            break;
        }
    }
}
```



## 4. Redis缓存淘汰策略

```
1.noeviction:当内存使用超过配置的时候会返回错误，不会驱逐任何键
2.allkeys-lru:加入键的时候，如果过限，首先通过LRU算法驱逐最久没有使用的键
3.volatile-lru:加入键的时候如果过限，首先从设置了过期时间的键集合中驱逐最久没有使用的键
4.allkeys-random:加入键的时候如果过限，从所有key随机删除
5.volatile-random：加入键的时候如果过限，从过期键的集合中随机驱逐
6.volatile-ttl:从配置了过期时间的键中驱逐马上就要过期的键
7.volatile-lfu:从所有配置了过期时间的键中驱逐使用频率最少的键
8.allkeys-lfu:从所有键中驱逐使用频率最少的键
```

### LRU的原理

```c

```

