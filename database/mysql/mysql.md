MYSQL
===



### 1.事务

事务是最小的工作单元。一个事务对应着一个完整的业务。

```
事务的特征(ACID):
	原子性: 事务是最小单位，不可再分.
	一致性: 事务的DML语句,必须保证同时成功或者同时失败
	隔离性: 事务之间具有隔离性，事务之间的数据不会相互干扰。
	持久性: 事务一旦提交，对数据库的修改是永久性（内存数据持久化到硬盘中）。
```

##### 1-1. 原子性（undo log）：

```
	InnoDB实现回滚的原理是undo log。当数据库发生修改时,会生成对应的undo log。
	当事务失败或者调用rollback时,便根据undo log中的信息将数据回滚。
```

##### 1-2. 持久性（redo log）：

```
	InnoDB作为MySQL的存储引擎,数据是存储在磁盘中的，为了防止每次读操作都进行io操作，所以有
	缓存(Buffer Pool)，读时先从缓存中读取，没有再读取磁盘加载到缓存。缓存中的数据会定期更新
	到磁盘中(刷脏).缓存虽然加快了读取的速度,减少了IO,但是遇到Mysql宕机就会发生数据丢失。
	
	所以InnoDB使用redo log，先在log记录当前操作,当事务commit时，调用fsync接口对redo log进行刷盘。
	若mysql发生了宕机，重启时读取redo log恢复数据。
	
	redo log是预写式日志，所有修改先写入日志，再更新到缓存。（redo log只包含了要写入磁盘的数据）,
	大大的减少了IO。
	
```

##### 1-3. 隔离性:

​		隔离性要求同一时刻只有一个事务对数据库进行写操作。

```
	1.脏读：事务A中读取到了事务B中未提交的数据
	2.不可重复读： 一个事务中多次查询一个数据,结果不同。
	3.幻读：事务A读取结果集,事务B修改事务A的结果并提交,事务A查询的结果集被修改。
	
	事务的隔离级别:
		1.读未提交(RU)：一个事务还未提交，它的变更能被其他事务看见.
		2.读提交(RC): 一个事务提交之后, 它做的变更才会被其他事务看到.
		3.可重复读(RR): 一个事务执行过程中看到的数据, 总是跟这个事务在启动时看到的数据是一致的.
		4.串行化(S): 对于同一行记录, 读写都会加锁. 当出现读写锁冲突的时候, 
		           后访问的事务必须等前一个事务执行完成才能继续执行.
		           
	锁的实现：
		InnoDB：
			1.行级锁：行级锁开销大,加锁慢,锁的颗粒度最小,发生锁的冲突概率最低,并发最高
			(根据索引查询时才会使用)
			2.表级锁：表级锁开销小，加锁快，锁定粒度大、发生锁冲突最高，并发度最低
			3.页级锁：介于行级锁和表级锁之间，一次锁定相邻的一组数据。
			4.MVCC(多版本并发控制协议):同一时刻,不同事务读到数据不同。不需要加锁，并发
			性能好，主要依靠标记位(版本号/删除时间/undo log指针)与undo log实现。
			
		select * from information_schema.innodb_locks; #查看锁的概况
		
		
```

##### 1-4. 一致性：

​		事务结束后，数据库的完整约束被破坏，事务执行的前后都是合法数据。

```
	1.主键存在且唯一
	2.列的完整性（字段的类型、大小、长度）
	3.外键约束
```



### 2. MYSQL的日志

```
	1.binlog(二进制日志)
	2.errorlog(错误日志)
	3.general log(查询日志)
	4.slow_query log(慢查询日志)
	InnoDB日志：
		1.redo log(重做日志)
		2.undo log(回滚日志) 
```



##### 2-1. undo log(回滚日志) 

​	记录sql执行相关信息。当发生回滚时,执行undo log相反的内容

```
	undo log:逻辑日志，根据每行记录进行记录。
	例：
		insert->delete
		delete->insert
		update->update回原先的记录
```

##### 2-2. redo log(重做日志)

​	防止数据库宕机影响持久性

```
	redo log:物理日志，记录的是数据页的物理修改,用来恢复提交后的物理数据页，
	且只能恢复到最后一次提交。
```

###### 2-2-1：(日志提交)

```
	redo log分为buffer和file两部分，事务提交时，必须先将所有事务的日志写入到磁盘file上。
	log buffer处于mysql用户空间，会调用fsync()方法,经过操作系统空间OS BUFFER将日志写
	入到log file。
	
	注：
	innodb_flush_log_at_trx_commit:控制log buffer刷新到log file
		1(默认):每次事务提交，都由buffer写入OS buffer，调用fsync刷新日志到file
		0: 事务提交不会将buffer写入OS buffer，而是每秒由buffer写入OS buffer并调用
		fsync刷新日志到file(当发生宕机时有一秒的数据丢失)
		2: 事务提交不会写入buffer而是直接写入OS buffer，然后每秒调用fsync将日志写入到file。
```

###### 2-2-2：（日志块log block）

```
	redo log是以为块为单位的，每个块512字节。在buffer/os buffe/file中都是512的字节日志块存储。
	日志是由 
		日志头： 12个字节 
			log_block_hdr_no:	日志块在buffer中的位置id (4字节)
			log_block_hdr_data_len: log的大小  (2字节)
			log_block_first_rec_group： 日志块中第一个log的开始偏移位置 (2字节)
			lock_block_checkpoint_no: 写入检查点信息的位置 (4字节)
		日志尾: 
			log_block_trl_no: 日志块在buffer中的位置id,与日志头中log_block_hdr_no相同
```

##### 2-3. binlog(二进制日志)

​	可以基于时间点恢复数据,用于主从复制。

```
	binlog模式：
		STATMENT: 基于SQL语句的复制,每一条修改的sql都会记录到binlog
			(记录的日志比较少,磁盘IO低,在主从模式中,sleep()等函数会导致主从间的数据不一致)
		ROW； 基于行的复制,记录哪条数据被修改了,修改了什么内容。
			(会产生大量的日志)
		MIXED : 混合模式。 一般使用STATMENT模式保存,对于STATMENT无法保存的操作使用ROW模式，
			MYSQL会根据SQL语句选择保存方式
```

##### 2-4. errorlog(错误日志)

```
 1.MYSQL执行过程中的错误信息
 2.MYSQL执行过程中的警告信息
 3.MYSQL启动和停止过程产生的信息
 4.event scheduler运行时所产生信息
 
 相关属性：
 	log_error: 错误日志的存放路径
 	log_warnings：0：不记录警告信息， 1：记录警告信息
```

##### 2-5.  general log(查询日志)

```
	记录查询的日志,语句的正确与否都会被记录，由于查询日志很多，默认不开启。
	
	相关属性：
		general_log ： OFF 不开启
		general_log_file ： 查询日志路径
		log_output   ： 文件输出格式
```

##### 2-6. slow_query log(慢查询日志)

```
	记录响应比较慢的查询语句
	
	相关属性：
		slow_query_log: 是否开启慢查询 ON OFF
		slow_query_log_file: 慢查询日志的路径
		long_query_time:  慢查询时间 10s
		log_queries_not_using_indexes： 查询里未使用索引是否记录 ON OFF
```

### 3. MYSQL的锁（InnoDB）

```
	1.行锁:行级锁开销大,加锁慢,锁的颗粒度最小,发生锁的冲突概率最低,并发最高(根据索引查询时才会使用)
	2.表锁:表级锁开销小，加锁快，锁定粒度大、发生锁冲突最高，并发度最低
	3.页级锁:介于行级锁和表级锁之间，一次锁定相邻的一组数据。
	4.MVCC
	
	InnoDB加锁方法：
		1.隐式加锁: 根据隔离级别在需要的时候自动加锁，锁只有在commit或者rollback时,才会讲
			所有锁一起释放。
		2.显示加锁:
			select ... lock in share mode : 共享锁
			select ... for update : 排他锁
```

##### 3-1. 行锁

```
	行锁分为:
		1.共享锁(读锁): 允许一个事务去读取一行,阻止其他事务获取相同数据集的排他锁。
		2.排他锁(写锁): 允许获得排他锁的事务更新事务，阻止其他事务取得相同数据集的共享锁和排他锁。
	实现原理:
		行锁是通过给索引项加锁来实现。所以只有使用索引时,才会使不然使用表锁。
	
    行锁优化:
    	1.尽量使用索引，避免升级为表锁
    	2.合理设计索引，尽可能缩小锁的范围
    	3.减少范围查询的范围，避免间隙锁的影响。
    
	注：
		1.不同索引的若是访问相同行，也会冲突堵塞。行锁通过索引后，会对行数据加锁。
		2.行锁实现比较复杂，开销大。在大面积修改的情况下，表锁的性能会更好。
```

##### 3-2. 表锁

```
	表锁分为:
		1.意向共享锁: 事务打算给数据行加共享锁，加共享锁前需要先获取意向共享锁
		2.意向排他锁: 事务打算给数据行加排他锁，加共享锁前需要先获取意向排他锁
```

##### 3-3.间隙锁

```
	间隙锁：在范围查找中不仅为对存在的记录加锁，也会对不存在的记录(间隙)加锁。
```

##### 3-4.多版本并发控制(MVCC)

​		适用于读提交(RC)、可重复读(RR)

```
	原理:
		通过保存某个时间点的快照,一个事务无论运行多久,在同一个事务中看到的数据都是
		一致的。事务的开启时刻不同，事务看到表里的数据也可能会不同。
     
     版本链:
     	存在undo log中
     	聚簇索引中包含两个隐藏列
     		trx_id: 数据修改时,记录事务版本号
     		roll_pointer: 数据修改时,存储一个指针,指针指向记录修改前的信息
     		
     ReadView(快照):
     	m_ids: 生成ReadView时当前系统活跃的事务id
     	min_trx_id: 生成ReadView时系统活跃的最小事务id
     	max_trx_id: 生成ReadView时系统分配给下一个事务的id
     	creator_trx_id: 生成ReadView的事务id
    
    实现:
		InnoDB中每一列都额外保存列（trx_id），开始事务时，系统版本号会递增，
		系统版本号当做事务的版本号，用来查询每行记录的版本号。
		
		1.insert操作:插入一条记录,trx_id设置为cur_trx_id
		2.update操作: undo log记录原记录信息,然后插入一条新记录,roll_pointer
				指向原记录
		3.detele操作: 在版本链最新的位置复制一份，trx_id设置为cur_trx_id，
				头信息(record header)的(deleted flag)标记为true。
        4.query操作(查询ReadView):  
      			1).trx_id == creator_trx_id (当前事务)
      			2).trx_id <=min_trx_id  (记录在事务开启前生成)
      			3).trx_id (min_trx_id,max_trx_id) 之间 && trx_id不在m_ids中
      			
      		查询时根据数据行trx_id在ReadView中判断数据对当前事务是否可见，若不可见，则根据
      		roll_pointer在undo log中查找上一个版本，一直沿着版本链找到可见记录或者版本链结束。
      		
    
     RC和RR的区别：
     	RC每次读数据都会生成一个ReadView。
     	RR只有第一次读会生成ReadView。
     	
     注：
     	数据修改的过程：
     		begin-> 用排他锁(写锁)锁定该行->记录redo log->记录undo log
     		->修改当前行的值，写事务编号，回滚指针指向undo log中的修改前的行
```



### 4. MYSQL索引

```
	索引的作用:
		1.减少了服务器扫描的数量
		2.避免了排序和临时表
		3.InnoDB中会使用行级锁,提交了并发访问量
```

##### 4-1. InnoDB聚簇索引

```
	1.行数据与叶子节点存储在一起.非叶子节点为索引项(主键)	
```

##### 4-2. InnoDB非聚簇索引（二级索引）

```
	回表:
		非聚簇索引的叶子存储的是聚簇索引的主键值。所以二级索引在查询时,会先得到聚簇索引的索引项，
		再跟索引项再查询一个聚簇索引的表,这个过程就时回表。
```

##### 4-3. page结构

```
	数据页(page)是 InnoDB最小的磁盘单位。存放表中行的实际数据，Page是一个双向链表。
	
	1.File Header(文件头):
		FIL_PAGE_SPACE_OR_CHKSUM(4k): 该页的checksum值
        FIL_PAGE_OFFSET(4k): 表空间中页的偏移值
        FIL_PAGE_PREV(4k): 当前页的上一页
        FIL_PAGE_NEXT(4k): 当前页的下一页
        FIL_PAGE_LSN(8k): 最后被修改的日志序列位置
        FIL_PAGE_TYPE(2k): 页的类型 (B+树叶子节点/索引节点/undo log)
        FIL_PAGE_FILE_FLUSH_LSN: 
        FIL_PAGE_ARCH_LOG_NO_OR_SPACE_ID: 页属于哪个表空间
       
    2.Page Header(页头): 记录数据页的状态
    
    3.Infimum/Supremum(边界记录):
    	Infimum:比任何主键值要小
    	Supremum:比任何主键要大
    	
   	4.User Records(16k):
   		实际存储的行数据的内容。
   	5.Page Directory(页目录):
   		存放了记录的相对位置
   	6.File Trailer:
   		FIL_PAGE_END_LSN:File Heade的FIL_PAGE_SPACE_OR_CHKSUM和FIL_PAGE_LSN
    	
    页分裂:
    	插入数据按照主键排序，当插入新数据，如果数据大小能放入页中，就按顺序将页填满。若当前
    	页已经填满，则根据FIL_PAGE_NEXT插入下一页。
    	若下一页也是满的,创建一个新页，然后当前要分裂的点开始移动到新页。页分裂会导致物理页的错位。
    页合并:
    	删除记录，不会物理删除,数据标记为flaged。当页中删除的量达到MERGE_THRESHOLD时，会寻找
    	前后的页看是否能合并页
```

##### 4-4. B+树

```
	1.叶子节点中包含了全部的元素信息，叶子节点按照关键字大小顺序排列
	2.中间节点不包含数据，只用来索引
```

B+数实现：

```java
public class BTree<K extends Comparable<K>, V> {

    private Node<K, V> root;

    /**
     * 节点数量
     */
    private static final int M = 4;

    public V find(K key) {
        if (root != null) return root.find(key);
        return null;
    }

    public void insert(K key, V value) {

    }

    class Entry<K extends Comparable<K>, V> {

        private K key;

        private V value;

        private Node<K, V> node;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public Entry(Entry<K, V> entry, Node node) {
            this.key = entry.key;
            this.value = entry.value;
            this.node = node;
        }
    }

    /**
     * 节点抽象类（节点分两种，叶子节点和非叶子节点）
     */
    abstract class Node<K extends Comparable<K>, V> {

        protected Node<K, V> parent;//父节点
        protected Entry<K, V>[] entries; //键
        protected int keyNum;//键的数量

        abstract V find(K key);//查询

        abstract Node<K, V> insert(K key, V value);//插入

        protected Node<K, V> insertNode(Node<K, V> node1, Node<K, V> node2, K key) {
            return null;
        }
    }

    /**
     * 非叶子节点，只用来索引不包含数据
     */
    class BPlusNode<K extends Comparable<K>, V> extends Node<K, V> {

        /**
         * 递归查询,只为确定落在哪个区间
         * 找到叶子节点会调用叶子节点的find方法
         */
        @Override
        V find(K key) {
            for (int i = 0; i < this.keyNum; i++) {
                if (key.compareTo(this.entries[i].key) <= 0) {
                    return this.entries[i].node.find(key);
                }
            }
            return null;
        }

        /**
         * 递归查询,只为确定落在哪个区间
         * 在该区间插入
         */
        @Override
        Node<K, V> insert(K key, V value) {
            for (int i = 0; i < this.keyNum; i++) {
                if (key.compareTo(this.entries[i].key) <= 0) {
                    return this.entries[i].node.insert(key, value);
                }
            }
            return this.entries[keyNum - 1].node.insert(key, value);
        }

        /**
         * 当叶子节点，节点数超过m个，分裂结束后，递归向父节点插入元素
         */
        protected Node<K, V> insertNode(Node<K, V> node1, Node<K, V> node2, K key) {
            K oldKey = null;//原始key
            if (this.keyNum > 0) oldKey = this.entries[this.keyNum - 1].key;
            if (key == null || this.keyNum == 0) {
                //空节点，直接插入两个节点
                this.entries[0] = new Entry<>(node1.entries[node1.keyNum - 1], node1);
                this.entries[1] = new Entry<>(node1.entries[node1.keyNum - 1], node2);
                this.keyNum += 2;
                return this;
            }
            //当前节点为非空节点
            int i = 0;
            while (key.compareTo(this.entries[i].key) != 0) {
                i++;
            }
            //左边节点的最大值可以直接插入,右边的要挪一挪再进行插入
            this.entries[i] = node1.entries[node1.keyNum - 1];
            Entry<K, V>[] temp = new Entry[M];
            System.arraycopy(this.entries, 0, temp, 0, i);
            System.arraycopy(this.entries, i, temp, i + 1, this.keyNum - i);
            temp[i + 1] = node2.entries[node2.keyNum - 1];
            this.keyNum++;

            //判断是否分裂
            if (this.keyNum <= M) {
                System.arraycopy(temp, 0, this.entries, 0, this.keyNum);
                return this;
            }

            BPlusNode<K, V> tempNode = new BPlusNode<K, V>();
            tempNode.keyNum = this.keyNum - this.keyNum / 2;
            tempNode.parent = this.parent;
            if (this.parent == null) {
                //若没有父节点,创建一个非叶子节点
                BPlusNode<K, V> parentNode = new BPlusNode<>();
                tempNode.parent = parentNode;
                this.parent = parentNode;
                oldKey = null;
            }
            //将数据复制到新节点,原来的叶子节点只留左半边
            System.arraycopy(temp, this.keyNum / 2, tempNode.entries, 0, tempNode.keyNum);
            for (int j = this.keyNum / 2; j < this.entries.length; j++) this.entries[j] = null;
            return this.parent.insertNode(this, tempNode, oldKey);
        }
    }

    /**
     * 叶子节点，其中包含全部元素的信息,
     * 所有叶子是一个双向链表
     */
    class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {

        protected LeafNode<K, V> left;//链表前节点
        protected LeafNode<K, V> right; //链表后节点

        @Override
        V find(K key) {
            //二分查找
            int l = 0, r = this.keyNum;
            while (l < r) {
                int m = (l + r) >>> 1;
                K midKey = this.entries[m].key;
                if (key.compareTo(midKey) == 0) {
                    return this.entries[m].value;
                } else if (key.compareTo(midKey) < 0) {
                    r = m;
                } else {
                    l = m;
                }
            }
            return null;
        }

        @Override
        Node<K, V> insert(K key, V value) {
            K oldKey = null;//原始key
            if (this.keyNum > 0) oldKey = this.entries[this.keyNum - 1].key;
            //寻找要插入的区间
            int i = 0;
            while (i++ < this.keyNum) {
                if (key.compareTo(this.entries[i].key) < 0) break;
            }

            //copy一个新的子节点数组,第i位后向后移,新元素插入i的位置,当前节点存储数量+1
            Entry<K, V>[] temp = new Entry[M];
            System.arraycopy(this.entries, 0, temp, 0, i);
            System.arraycopy(this.entries, i, temp, i + 1, this.keyNum - i);
            temp[i] = new Entry<>(key, value);
            this.keyNum++;
            if (this.keyNum <= M) {
                //没超过上限不需要分裂
                System.arraycopy(temp, 0, this.entries, 0, this.keyNum);
                //判断是否更新父节点的边界值,递归往上赋值
                Node<K, V> node = this;
                while (node.parent != null) {
                    if (node.entries[node.keyNum - 1].key
                            .compareTo(node.parent.entries[node.parent.keyNum - 1].key) > 0) {
                        //若当前最大节点比父节点最大节点值大
                        node.parent.entries[node.parent.keyNum - 1].key = node.entries[node.keyNum - 1].key;
                    }
                    node = node.parent;
                }
                //不分裂提前返回
                return this;
            }
            //开始分裂
            //新建叶子节点(迁移数量为当前节点数量的一半)
            LeafNode<K, V> tempNode = new LeafNode<K, V>();
            tempNode.keyNum = this.keyNum - this.keyNum / 2;
            tempNode.parent = this.parent;
            if (this.parent == null) {
                //若没有父节点,创建一个非叶子节点
                BPlusNode<K, V> parentNode = new BPlusNode<>();
                tempNode.parent = parentNode;
                this.parent = parentNode;
                oldKey = null;
            }
            //将数据复制到新节点,原来的叶子节点只留左半边
            System.arraycopy(temp, this.keyNum / 2, tempNode.entries, 0, tempNode.keyNum);
            for (int j = this.keyNum / 2; j < this.entries.length; j++) this.entries[j] = null;

            //设置叶子节点链表
            this.right = tempNode;
            tempNode.left = this;
            //插入父节点
            return this.parent.insertNode(this, tempNode, oldKey);
        }
    }
}

```



mysql的组成
---

    连接器：管理连接，权限控制
    分析器：词法分析, 语法分析.
    优化器：执行计划生产，索引的选择。
    执行器： 操作存储引擎, 返回执行结果.