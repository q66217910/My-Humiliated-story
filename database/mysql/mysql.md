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
	日志块是由 
		日志头： 12个字节 
			log_block_hdr_no:	日志块在buffer中的位置id (4字节)
			log_block_hdr_data_len: log的大小  (2字节)
			log_block_first_rec_group： 日志块中第一个log的开始偏移位置 (2字节)
			lock_block_checkpoint_no: 写入检查点信息的位置 (4字节)
```

##### 2-3. binlog(二进制日志)

​	可以基于时间点恢复数据,用于主从复制。

```

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

### 3. MYSQL的锁



### 4. MYSQL索引

mysql的组成
---

    连接器：管理连接，权限控制
    分析器：词法分析, 语法分析.
    优化器：执行计划生产，索引的选择。
    执行器： 操作存储引擎, 返回执行结果.