# redis获取一个hash的长度



通过 hlen() 方法获取 redis hash的长度。

在redis中哈希表的结构

​	typedef struct dictht{

​		dictEntry table；//哈希表数组

​		unsigned long size；//哈希表大小

​		unsigned long sizemask；//哈希表大小掩码，用于计算索引，总等于size-1

​		unsigned long used；//已使用的节点数

​	}



即hlen()实际上是读取了 dictht中 used的值。