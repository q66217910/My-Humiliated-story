ZSET
===

1.zset结构：
---
    
    /*
     * 有序集合
     */
    typedef struct zset {
    
        // 字典，键为成员，值为分值
        // 用于支持 O(1) 复杂度的按成员取分值操作
        dict *dict;
    
        // 跳跃表，按分值排序成员
        // 用于支持平均复杂度为 O(log N) 的按分值定位成员操作
        // 以及范围操作
        zskiplist *zsl;
    
    }
    
    /*
     * 跳跃表
     */
    typedef struct zskiplist {
    
        // 表头节点和表尾节点
        struct zskiplistNode *header, *tail;
    
        // 表中节点的数量
        unsigned long length;
    
        // 表中层数最大的节点的层数
        int level;
    
    }
    
    /*
     * 跳跃表节点
     */
    typedef struct zskiplistNode {
    
        // 成员对象
        robj *obj;
    
        // 分值
        double score;
    
        // 后退指针
        struct zskiplistNode *backward;
    
        // 层
        struct zskiplistLevel {
    
            // 前进指针
            struct zskiplistNode *forward;
    
            // 跨度
            unsigned int span;
    
        } level[];
    
    }
    
  dict：用于保存key/value，便于通过key(元素)获取score(分值)
  zskiplist：保存有序的元素列表，便于执行range之类的命令。