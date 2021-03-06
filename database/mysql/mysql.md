MYSQL
===



### 1.事务

事务是最小的工作单元。一个事务对应着一个完整的业务。

##### 事务的特征(ACID):

​	**原子性:** 事务是最小单位，不可再分.
​	**一致性:** 事务的DML语句,必须保证同时成功或者同时失败
​	**隔离性:** 事务之间具有隔离性，事务之间的数据不会相互干扰。
​	**持久性:** 事务一旦提交，对数据库的修改是永久性（内存数据持久化到硬盘中）。

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
		
	
	为什么mysql的隔离级别默认是RR?
		因为mysql binlog在STATMENT在主从同步时，会出现主从不一致，只有RR级别引入间接锁，
		或者binlog改为row格式。
		
		
```

##### 1-4. 一致性：

​		事务结束后，数据库的完整约束被破坏，事务执行的前后都是合法数据。

```
	1.主键存在且唯一
	2.列的完整性（字段的类型、大小、长度）
	3.外键约束
```

##### 1-5. 事务的传播属性

当多个事务同时存在时，spring的处理

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {
	
    //事务的传播类型
    Propagation propagation() default Propagation.REQUIRED;
    //事务的隔离级别
    Isolation isolation() default Isolation.DEFAULT;
}

public enum Propagation {
    
    //支持当前事务,不存在创建一个新事务
    REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
    //支持当前事务,如果事务不存在则以非事务形式执行
    SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),
    //支持当前事务，如果事务不存在则抛异常
    MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
    //创建一个事务，如果存在事务则停止当前事务(两个独立的事务)
    REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
    //以非事务执行
    NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
    //执行非事务,事务存在则抛异常
    NEVER(TransactionDefinition.PROPAGATION_NEVER),
    //如果有运行的事务,嵌套在上一个事务中
    NESTED(TransactionDefinition.PROPAGATION_NESTED);
}

public enum Isolation {
    
    //使用数据库默认的隔离级别
    DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),
    //读未提交(RU)
    READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),
    //读提交(RC)
    READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),
    //可重复读(RR)
    REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),
    //串行化(S)
    SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);
}
```

##### 1-6.事务失效

-  **底层数据库引擎不支持事务**
-  **非public方法修饰**
-  **try catch了异常，异常没有抛出**
-  **方法中调用同类的方法** ： 即this.method(),因为@Transactional是生成代理类，同类代码调用时，相当于this
       没有使用代理类，所以不生效
-  **rollbackFor和noRollbackFor属性设置错误**： 指定错误异常
-  **propagation属性设置错误**
-  **注解加在接口上:**  当注解加在接口上，而设置的AOP模式为cglib，cglib只能代理class(针对目标类生成子类)。



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
    	1.尽量使用索引，避免升级为表 锁
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



#### 隔离级别RR与RC对比

- RR存在间隙锁，死锁的概率比RC大

- RR级别下，条件列未命中会锁表，RC级别下只会锁行。

- 在RC级别下，有**半一致性读**的特性增加了updatec操作的并发。

  （半一致性读：在update语句读到记录已经加锁时，会重新发起一次读操作，读取最新版本的记录并加锁。）



#### MYSQL的XA事务

1. **prepare阶段：**  写入redo log，并将回滚段置为prepared状态，此时binlog不做操作。
2. **commit阶段：** innodb释放锁，释放回滚段，设置提交状态，写入binlog，然后存储引擎层提交。



#### mysql数据库崩溃恢复

1. 扫描最后一个binlog文件，提取其中的xid；
2. InnoDB维持了状态为Prepare的事务链表，将这些事务的xid和binlog中记录的xid做比较，如果在binlog中存在，则提交，否则回滚事务。



#### 事务隔离级别的实现

- **可重复读(RR):**
  1. 利用间隙锁，防止幻读的出现，保证了可重复读
  2. MVCC的快照生成时机不同(只会在第一次读的时候生成)
  
- **读提交(RC):**

  MVCC每次读取时就会生成一个新的快照

  

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
            System.arraycopy(temp, this.keyNum / 2, 
                             tempNode.entries, 0, tempNode.keyNum);
            for (int j = this.keyNum / 2; j < this.entries.length; j++) 
                this.entries[j] = null;
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
                            .compareTo(node.parent.entries[node.parent.keyNum - 1].key)
                        > 0) {
                        //若当前最大节点比父节点最大节点值大
                        node.parent.entries[node.parent.keyNum - 1].key 
                            = node.entries[node.keyNum - 1].key;
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



#### 索引建立规则

1. 索引并非越多越好，索引的建立会占用磁盘空间，并且影响insert,delete,update等语句的性能。
2. 避免更新频繁的表建立更多的索引。
3. 数据量少的表尽量不要使用索引，因为全表查询可能会比走索引有回表要快。
4. 不同值很少的列没必要建立索引例如(性别)，因为对查询效率提升不大
5. 在频繁进行排序或者分组的列上建立索引



#### 回表查询和索引覆盖

**索引覆盖：**   在非聚簇索引叶子节点存储的是索引的列，如果查询的列都在索引中，就不需要回表操作变可以返回

**回表查询：**  因为非聚簇索引非叶子节点存的是主键，所以当需要查询非聚簇索引中没有的数据的时候，只能根据主键到聚簇索引中查询。



#### 唯一索引和普通索引

**唯一索引查询比普通索引快，插入比普通索引慢。**当非聚簇索引插入时，先判断索引页是否在内存中，如果在直接插入。若不在则先放入Change Buffer 中，再以一定频率和情况进行Insert Buffer和原数据页合并(merge)操作。Change Buffer的优势是，可以将多个插入操作合并成一个，提高了非聚簇索引的插入性能。**而唯一索引插入慢的原因是因为无法利用Change Buffer。**因为唯一索引要保证唯一性，必须将数据页加载到内存才能判断。**唯一索引查询快的原因，普通索引要查询到下一个条不满足的才中止，而唯一索引只需要查询到满足的就中止。**



#### 联合索引

1. 最左匹配原则：范围查找就会停止匹配



5.分库分表
---

#### 垂直拆分：

​	将表的**数据列**进行拆分，将列比较多的表拆分为小表

1. 不常用的字段单独放一张表。
2.  把text，blob等大字段拆分出来放在附表中; 
3. 经常组合查询的列放在一张表中。



#### 水平拆分：

  将表的**数据行**进行拆分，将列比较多的表拆分为小表

- **Range(范围分表):** 根据范围进行分发。例如 时间、id区间

  优点：扩容方便

  缺点：热点数据查询慢

- **Hash(哈希取模分表)：** 根据哈希运算然后取模确定要存储表的位置。

  优点：数据分发均匀，不会产生热点数据

  缺点：扩容时不方便，涉及rehash和数据迁移。



#### 分布式id生成：

- **雪花算法：**  一共64位，0(第一位)+时间戳(41位毫秒数)+数据中心(5位)+机器id(5位)+流水号(12位)

  每秒能生成1024*（2^12）个id

  优点：不依赖db，高性能高可用，id呈递增（数据库插入性能好）

  缺点: 基于时间，若机器发生时钟回拨，会造成id冲突和id乱序

- **Leaf-segment：**通过 proxy server 批量获取，每次获取( step步长)的 segment 号段。用完再去数据库获取新的号段。各个业务通过 biz_tag 区分

  优点：方便线性扩展。容灾性高，DB宕机了仍然可以对外服务。

  缺点:  依赖数据库，id可计算，号码不够随机。



## 6.分布式事务



-  **事务协调器（TC）:**  维护全局事务和分支事务的状态，驱动全局提交或回滚。 
-  **事务管理器(TM)：**   定义全局事务的范围：开始全局事务，提交或回滚全局事务。 
-  **资源管理器（RM）：**  管理分支事务正在处理的资源，与TC进行对话以注册分支事务并报告分支事务的状态，并驱动分支事务的提交或回滚。 



#### 两段式提交(2PC)

准备阶段参与者错误，会发生回滚。提交阶段失败会一直重试。

**准备阶段：** 协调者向所有参与者发送准备命令（除了提交事务外的所有指令）

**提交阶段（提交事务/回滚）：** 同步等待所有资源响应，协调者则向所有参与者发送提交事务命令。

**2pc的缺点:** 

1. **同步阻塞问题：** 资源会被锁定，其他操作都无法访问该资源，需要等到事务提交。
2.  **单点故障:** 一旦协调者故障，参与者会一直阻塞，一直锁定资源。
3. **数据一致性问题:**   网络异常或者脑裂的情况下，部分参与者没有收到commit。



#### 三段式提交(3PC)

3pc主要解决了单点故障的问题，减少了阻塞。

**CanCommit:** 询问此时的参与者是否有条件接这个事务，防止一开始就锁定资源。

**PreCommit:**  2PC准备阶段

**DoCommit:**  2PC提交阶段



#### TCC

**try:**   资源的预留和锁定

**Confirm：**  真正的执行事务

**Cancel ：** 撤销预留

1. 发起事务（调用者->事务管理者）
2. 调用try（调用者->参与者）
3. 提交或者回滚（调用者收到try的返回->事务管理者）
4. 调用confirm或cancel （事务管理者->参与者）

**tcc的缺点：**

- **空回滚：** 分支事务所在的服务器宕机或者网络异常，调用记录为失败，当故障修复后，分布式事务进行回滚调用cannel
- **幂等:** TCC提交重试机制，防止重复执行。
- **悬挂:** 悬挂是指cancel接口比try接口先执行。



#### 本地消息表(数据库本地消息表，定时轮询)

 将业务的执行和将消息放入消息表中的操作放在同一个事务中，执行下一操作，若执行成功，消息表的状态改为成功。再由定时读取本地消息表，筛选出未成功的消息再次调用相应的服务。



## 消息事务(rocketMq)

1. **发送半消息：** （**半消息指 消息对消费者不可见**）
2. **发送半消息成功，发送方执行本地事务**
3. **根据结果向Broker发送commit或者rollback请求**
4. **若是rollback，则丢弃消息队列消息。**
5. **若是commit，则发送给订阅者。**
6. **订阅者执行本地事务。**
7. **执行成功消费消息。**



#### Seata事务的生命周期：

1.  TM要求TC开始一项新的全局交易。TC生成代表全局事务的XID。 (**@GlobalTransactional**)
2.  XID通过微服务的调用链传播.
3.  RM将本地事务注册为XID到TC的相应全局事务的分支。 
4.  TM要求TC提交或回退XID的相应全局事务。 
5.  TC驱动XID的相应全局事务下的所有分支事务，以完成分支提交或回滚。 





## 7.mysql常见问题

#### Mysql主键：

在不设置主键的情况，innodb会生成一个隐藏列作为自增主键。**自己指定主键的好处是，查询时可以可以显式使用，提升查询效率**。主键推荐使用自增主键，因为在innodb中主键时聚簇索引，插入记录时会按照顺序插入，**非自增的主键插入时可能会在中间插入，导致页分裂，产生表碎片**。



#### timestamp和datetime区别：

- timestamp： 四个字节的整数，时间范围为1970-01-01 08:00:01到2038-01-19 11:14:07。**`timestamp`类型是带有时区信息的。**
- datetime：datetime储存占用8个字节，它存储的时间范围为1000-01-01 00:00:00 ~ 9999-12-31 23:59:59。**datetime存储的时间区间大，但是不带时区信息。**



#### 为什么不推荐存储大量的内容：

因为会导致biglog内容较多，会影响主从同步的效率。



#### Explain:

- **id:**   选择标识符, SELECT的查询序列号, **SQL从id大到小执行**，id大的为子查询，id相同时按顺序执行
- **select_type：** select子句的类型。
  -  *SIMPLE* ：简单sql，不包含union和子查询
  -  *PRIMARY* ：子查询中的最外层
  -  *UNION* ：uion查询中，在union后的子语句
  -  *DEPENDENT UNION* ：
  -  *UNION RESULT* ：union的结果集
  -  SUBQUERY :  子查询
  -  *DERIVED*  ： 派生表
- **table:**  数据库表名
- **partitions:**  分区
- **type:** 表示表的连接类型 
  -  *ALL* ：扫描全表
  - *index* ：遍历索引树
  -  *range* ：使用索引来选择行
  - *ref* ： 表的连接匹配条件 
  -  *eq_ref* ：主键外连接
  -  *const* ： 查询某部分进行优化，并转换为一个常量时 
  -  NULL ： 优化过程中分解语句，执行时甚至不用访问表或索引 
-  **possible_keys：**  表示查询时，可能使用的索引 
-  **key:** 表示实际使用的索引  
-  **key_len:** 索引字段的长度 
-  **ref:** 列与索引的比较 
-  **rows:** 扫描出的行数(估算的行数) 
-  **filtered:** 按表条件过滤的行百分比 
-  **Extra:** 执行情况的描述和说明 

  

## 8. @Transactional

### 8.1 核心类 PlatformTransactionManager 

-  **TransactionDefinition ：** 事务的定义，包括隔离级别、传播级别、超时时间、是否只读
-  **TransactionStatus：**事务状态，包含内部保存点。
-  **getTransaction():** 获取当前事务或者返回新的事务
-  **commit(TransactionStatus status)**： 提交事务
-  **rollback(TransactionStatus status)：** 事务回滚

```java
//JDBC、MyBatis：DataSourceTransactionManager
//JPA:JpaTransactionManager
public interface PlatformTransactionManager extends TransactionManager {

    TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
			throws TransactionException;
    
    void commit(TransactionStatus status) throws TransactionException;
    
    void rollback(TransactionStatus status) throws TransactionException;
}
```

### 8.2 事务管理启动

- **mode()：** 代理模式，JDK proxy或者AspectJ代理，默认JDK。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {

    boolean proxyTargetClass() default false;
    AdviceMode mode() default AdviceMode.PROXY;
    int order() default Ordered.LOWEST_PRECEDENCE;
}

public class TransactionManagementConfigurationSelector extends 	AdviceModeImportSelector<EnableTransactionManagement> {
    
    @Override
	protected String[] selectImports(AdviceMode adviceMode) {
		switch (adviceMode) {
			case PROXY:
				return new String[] {AutoProxyRegistrar.class.getName(),
						ProxyTransactionManagementConfiguration.class.getName()};
			case ASPECTJ:
				return new String[] {determineTransactionAspectClass()};
			default:
				return null;
		}
	}
    
}
```

### 8.3 事务的执行

```java
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {

    private static final ThreadLocal<TransactionInfo> transactionInfoHolder =
			new NamedThreadLocal<>("Current aspect-driven transaction");
    
   protected Object invokeWithinTransaction(Method method, 
                                            @Nullable Class<?> targetClass,
			final InvocationCallback invocation) throws Throwable {
            
   }
    
}
```



## 9.SQL优化

#### 1.SELECT

-  **UDF用户自定义函数:**   SQL返回多少行， 自定义函数就执行多少次。
-  **text类型检查：**   如果select出现text类型的字段，就会消耗大量的网络和IO带宽，由于返回的内容过大。
-  **group_concat谨慎使用：**  gorup_concat是一个字符串聚合函数，会影响SQL的响应时间，如果返回的值过大超过了 
-  **内联子查询：**    在select后面有子查询的情况称为内联子查询，SQL返回多少行，子查询就需要执行过多少次 

#### 2.FROM

-  **表的链接方式：**  不建议使用Left Join，即使ON过滤条件列索引，一些情况也不会走索引 
-  **子查询：**    不建议使用子查询，可以改写成Inner Join。 

#### 3.WHERE

-  **索引列被运算:**   任何运算，会导致索引失效。 
-  **类型转换:**   对于Int类型的字段，传varchar类型的值是可以走索引，MySQL内部自动做了隐式类型转换； 
-  **列字符集:**   所有对象字符集应该使用用utf8mb4 , 避免在关联查询Join时字段字符集不匹配导致索引失效 

#### 4.GROUP BY

-  **前缀索引:**   前缀索引，是不能消除排序。
-  **函数运算:**   函数运算后不能消除排序。

#### 5.ORDER BY

-  **前缀索引:**   前缀索引，是不能消除排序。
-  **字段顺序：**  排序字段顺序，asc/desc升降要跟索引保持一致。 

#### 6.LIMIT

-  **limit m,n要慎重:**   m越大的情况下SQL的耗时会越来越长 

#### 7.表结构

- **表名、列名：** 不能使用MySQL的关键字 
- **NOT NULL：**  尽量将字段都添加上NOT NULL DEFAULT VALUE属性，如果列值存储了大量的NULL，会影响索引的稳定性。 