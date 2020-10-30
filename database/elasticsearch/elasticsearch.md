# Elasticsearch

## 1.Elasticsearch介绍

#### 1. Field (字段)

- **字符串：** text、 keyword 

  **text与keyword的区别:**

  |          | text     | keyword                                                     |
  | -------- | -------- | ----------------------------------------------------------- |
  | 分词     | 支持     | 不支持                                                      |
  | 搜索类型 | 全文检索 | 索引查找                                                    |
  | 模糊查询 | 支持     | 支持                                                        |
  | 精确查询 | 支持     | 支持                                                        |
  | 聚合     | 不支持   | 支持                                                        |
  | 排序     | 支持     | 支持                                                        |
  | 支持长度 | 无限制   | 32766个UTF-8类型的字符<br />(ignore_above:指定自持字符长度) |

- **整数型：**  long 、 integer 、 short 、 byte 

  |         | 取值范围     | 字节  |
  | ------- | ------------ | ----- |
  | byte    | -128~127     | 8bit  |
  | short   | -32768~32767 | 16bit |
  | integer | -2^31~2^31-1 | 32bit |
  | long    | -2^63~2^63-1 | 64bit |

- **浮点型：** double 、 float 、 half_float 、 scaled_float 

  浮点型由三部分组成：sign(符号)、exponent(指数)、fraction(分数)

  计算公式:   (−1)^signbit × 2^(exponentbits−127) × 1.fractionbits 

  |              | 精度               | 指数  | 小数   |
  | ------------ | ------------------ | ----- | ------ |
  | double       | 64位双精度         | 11bit | 52bitt |
  | float        | 32位单精度         | 8bit  | 23bit  |
  | half_float   | 16位半精度         | 5bit  | 10bit  |
  | scaled_float | 缩放类型的的浮点数 |       |        |

- **布尔值：**  boolean 

- **日期：**  date 

  1.  日期格式的字符串 
  2.  时间戳

- **二进制：**  binary 

    使用base64表示索引中存储的二进制数据，binary 类型只存储不索引。

- **范围类型 ：**  integer_range、float_range、long_range、double_range、date_range 

-  **JSON层级结构 :**  

    - **object :** 单个对象
    - **nested :** 对象数组

-  **地理类型  ：**  

    - **geo_point:** 用于地理位置经纬度坐标 
    - **geo_shape:**  用于复杂形状 

-  **专门的数据类型 :**

    -  **ip : **用于IPv4和IPv6地址 
    -  **completion :** 用于自动补全提示
    -  **token_count :**用于计数字符串中的token
    -  **murmur3 :**计算值的hashcode，并将其存储到索引中 



#### 2. Document (文档)

1. **元数据：**
   - **_index:**   代表一个document存放的index
   - **_type:**    代表document属于index下的类别 
   - **_id:**  document唯一标识（可以手动创建或者ES自动创建）,自动生成( 长度为20个字符，URI安全，base64编码，GUID )
   - **_version:**   ES内部使用乐观锁对document的写操作进行控制 (初始版本号为1，更新成功+1)
   - **found:**   document的搜索标志 
   - **_source:**  新增时放在http request body的json串内容 
2. **Document操作：**
   - **新增：**  PUT /{index}/{type}/id/_create  | PUT /{index}/{type}/id?op_type=create  (强制新增)
     			      PUT  /{index}/{type}/id  (手动指定id)
           			PUT /{index}/{type}    ( 自动生成 id)
   - **删除：**   DELETE  /{index}/{type}/id    (删除只是将文档标记为deleted)



#### 3. Shard(分片)

一个索引由多个分片组成，用于 Elasticsearch集群 解决单机存储容量问题。会跟路由功能根据Document的_id分配到不同分片上。

-  **路由(一致性hash)：** shard_num = hash(_routing) % num_primary_shards
-  **副本分片(replica):**   副本节点，可以用来做读写分离，以及高可用和数据安全保障 。最佳：节点最大数 = 分片数 * （副本数 + 1）



#### 4. Index(索引)

 索引中存储具有相同结构的文档(Document) ,通过 mapping定义，用于定义字段名和类型 。

1.  **索引元数据：**

   - **aliases：** 索引别名
   - **settings：** 索引的配置项，分为静态配置和动态配置，静态配置创建索引后不可修改。
   - **mappings：**  字段映射相关参数 

2.  **settings配置项：**

   -  **index.number_of_shards (静)：** 索引分片数量

   -  **index.shard.check_on_startup (静)：**  分片打开前是否检查分片，检测到分片损坏阻止分片打开。

     - **false:** 不检测
     - **checksum:** 只检测物理结构
     - **true:**  检查物理和逻辑损坏，相对比较耗CPU 

   -  **index.codec(静):**  数据存储的压缩算法 

     -  **LZ4(默认)：**  无损压缩算法，压缩速度为每核心 400 MB/s 
     -  **best_compression  ：**  DEFLATE是同时使用了LZ77算法 ， 比LZ4可以获得更好的压缩 ，但是存储性能比LZ4低。

   -  **index.routing_partition_size (静)：**  路由分区数 ， _routing 默认为__id

     原路由算法：  **hash(_routing) % number_of_shardings** 

     设置后路由算法： **(hash(_routing) + hash(_id) % index.routing_parttion_size ) % number_of_shards** 

   

   -  **index.number_of_replicas :**  每个主分片的副本数 ，数据备份数。
   -  **index.auto_expand_replicas：**  根据集群中数据节点的数量自动扩展副本的数量 
   -  **index.refresh_interval ：**  多久执行一次刷新操作，使得最近的索引更改对搜索可见。默认是1秒。设置为-1表示禁止刷新。 
   -  **index.max_result_window** ：在这个索引下检索的 from + size 的最大值。默认是10000。 
   -  **index.max_inner_result_window ：**  用于控制top aggregations，默认为100。内部命中和顶部命中聚合占用堆内存 。
   -  **index.max_rescore_window ：**  在rescore的搜索中，rescore请求的window_size的最大值。 
   -  **index.max_docvalue_fields_search ：**   一次查询最多包含开启doc_values字段的个数，默认为100。 
   -  **index.max_script_fields ：**  查询中允许的最大script_fields数量。默认为32。 
   -  **index.max_ngram_diff ：**  NGramTokenizer和NGramTokenFilter的min_gram和max_gram之间允许的最大差异。默认为1。 
   -  **index.max_shingle_diff ：**  对于ShingleTokenFilter, max_shingle_size和min_shingle_size之间允许的最大差异。默认为3。 
   -  **index.blocks.read_only ：**  索引数据、索引元数据是否只读 。
   -  **index.blocks.read_only_allow_delete ：** 索引数据、索引元数据是否只读，只允许删除。
   -  **index.blocks.read ：** 禁用索引数据的读操作。
   -  **index.blocks.write ：** 禁用索引数据的写操作。
   -  **index.blocks.metadata ：**  不允许对索引元数据进行读与写 。
   -  **index.max_refresh_listeners ：**  索引的每个分片上当刷新索引时最大的可用监听器数量。 
   -  **index.highlight.max_analyzed_offset ：**  高亮显示请求分析的最大字符数。 
   -  **index.max_terms_count：**   可以在terms查询中使用的术语的最大数量。  默认为65536。 
   -  **index.routing.allocation.enable ：**  Allocation机制 ，解决集群分片问题。
     -  **all ：**  所有类型的分片都可以重新分配，默认。 
     -  **primaries ：**  只允许分配主分片。 
     -  **new_primaries ：**  只允许分配新创建的主分片。 
     -  **none ：**  所有的分片都不允许分配。 
   -  **index.routing.rebalance.enable ：**  索引的分片重新平衡机制。 
     -  **all :**  默认值，允许对所有分片进行再平衡。 
     -  **primaries :**  只允许对主分片进行再平衡。 
     -  **replicas :**  只允许对复制分片进行再平衡。 
     -  **none :**  不允许对任何分片进行再平衡 
   -  **index.gc_deletes :**  文档删除后（删除后版本号）还可以存活的周期，默认为60s。 
   -  **index.max_regex_length ：**  用于正在表达式查询(regex query)正在表达式长度，默认为1000。 
   -  **index.default_pipeline ：**  默认的管道聚合器。 

3. **mappings配置项：**

   -  **type:**  Field类型
   -  **index:** 控制字段是否被索引
   -  **analyzer ：**  指定分词器。
     -   `standard`  
     -  `whitespace`  
     -  `simple`  
     -  `english `
   - **dynamic:**  Mapping生成字段类型后，不允许修改，因为倒排索引。
     -  **true （默认）  ：**  允许自动新增字段 
     -  **false  ：**   不允许自动新增字段，但是文档可以正常写入，但无法对新增字段进行查询等操作 
     -  **strict  ：**  文档不能写入，报错 
   - **copy_to:**   将该字段复制到目标字段 
   - **boost:**  字段分值权重
   - **coerce:**  数据隐式转换
   - **doc_values：** 支持排序和聚合
   - **eager_global_ordinals：**  是否预构建全局序号 
   - **enabled:**  用于 top-level mapping 和 object类型，使Elasticsearch完全跳过对字段内容的解析。 
   - **format:**  用于时间date类型，使用内置格式或者自定义格式
   - **ignore_above：**  长于 ignore_above 值的字符串不会被存储和索引。
   -  **null_value ：** null值是否可以被索引和搜索。



#### 5.  Node(节点)

1. **节点类型：**
   - **master(主节点  node.master ) :**  负责管理集群的变更， 例如增加、删除索引，或者增加、删除节点等 
   - **data(数据节点  node.data )：**  存储数据和其对应的倒排索引。默认每一个节点都是数据节点（包括主节点） 
   -  **协调节点：**   用来响应客户请求，均衡每个节点的负载。  
   -  **ingest ：**   内置的数据处理器 ，提供了 convert，grok之类的操作 。
2. **节点配置：**
   -  **node.master：**  使该*node*被选举为master节点 。
   -  **node.data ：**   使该*node*被选举为data节点 
   -  **node.ingest ：**   使该*node*被选举为ingest节点 
   -  **node.name ：**  节点名称
   -  **node.portsfile ：**  用于控制是否将文件写入到包含给定传输类型端口的日志目录中 



## 2.Elasticsearch倒排索引

#### 1.分析器(分词)：

-  **Character filters （字符过滤器）：**   字符过滤器以字符流的形式接收原始文本 ，通过过滤链（类似 类似Servlet中的过滤器 ），多次过滤得到新的字符流。
-  **Tokenizer （分词器）： **   一个分词器接收一个字符流，并将其拆分成单个token （通常是单个单词），并输出一个token流。  分词器还负责记录每个term的顺序或位置，以及该term所表示的原单词的开始和结束字符偏移量。一个分析器只能有一个分词器。
-  **Token filters （token过滤器）：**  token过滤器接收token流 , 对token流进行操作。token过滤器也是一个过滤链。

#### 2. 倒排索引的组成：

-  **单词词典:**  term的集合，每个索引项包括term的一些信息和   倒排列表的指针

-  **倒排列表(Roaring bitmaps):**   记录了每个term在document集合中出现过该term和出现的位置(倒排项)，所以根据倒排列表就可以查询哪些document包含了某些term。

-  **倒排文件 ：**  倒排文件是存储倒排索引的物理文件。 

-  **Term Index(FST)：**  

   前缀树的优点：

  - **空间占用小 **
  - **查询速度快( O(len(str)) )**

  Term Index存储着前缀与 Term Dictionary 的映射关系，Term Index查到对应Term Dictionary的位置后，再去磁盘读取找term。

-  **联合索引：**  跳跃表 或者  Roaring bitmaps 按位与。

  - **跳跃表：** 对最短的倒排列表，与其他field的倒排列表做交集。
  - **Roaring bitmaps：** 直接按位与，得到的结果就是最后的交集。



## 3.Elasticsearch的读写过程

#### 1.分片内部原理：

1. **倒排索引带来的不变性：** 倒排索引被写入磁盘后，不可改变。

   - **不需要锁：** 因为不更新索引，没有多线程修改数据的问题。
   - **文件系统缓存：** 因为索引不变，数据一旦进入缓存，大部分请求会命中缓存。
2. **分段( segment )搜索：** 每一个segment本身就是一个倒排索引。一个 *Lucene*  Indx(分片)包含了3个segment
   和一个commit point。



#### 2.写过程:

1. client向一个节点**提交** 索引新文档的请求 
2. 节点经过**路由**计算，计算新文档加入哪个分片。
3. 协调者节点将请求发送给对应的节点(主分片所在节点)
4. 开始写入新文档
5. 写入**内存(in-memory buffer)**,并写入 **transaction log（5s进行一次fsync）**  (文档还不可以被搜索)
6.  ES会每隔1秒时间,进行一次**刷新操作（refresh）** 
7. 这一秒内写入的新文档都会被写入一个文件系统缓存，并构成一个 分段（segment） （文档可以被搜索，但还未落盘） 。 
8.  每隔30分钟或者translog文件变得很大，则执行一次**fsync操作**。translog将被删除 。
9. 写入完成，将请求发送到其他副本分片。



#### 3.读过程：

1. 节点收到查询请求
2. 当前节点变成协调者
3. 广播请求到索引中每个节点的分片，查询请求可以被主分片或者副本分片处理。
4.  协调节点将在之后的请求中轮询所有的分片拷贝来分摊负载。 
5.  每个分片将会在本地构建一个优先级队列。如果客户端要求返回结果排序中从第from名开始的数量为size的结果集 。
6.  分片仅会返回一个轻量级的结果给协调节点，包含结果集中的每一个文档的ID和进行排序所需要的信息。 
7.  协调节点会将所有分片的结果汇总，并进行全局排序，得到最终的查询排序结果。此时查询阶段结束。 
8.  查询过程得到的是一个排序结果，  协调节点会确定实际需要返回的文档，并向含有该文档的分片发送get请求 ，分片获取文档返回给协调节点；协调节点将结果返回给客户端。 



## 其他文章转载

####  [Tencent ES优化](https://zhuanlan.zhihu.com/p/146083622?utm_source=wechat_session&utm_medium=social&utm_oi=1168151302969729024)：

1. **降低磁盘成本：**
   -  **冷热分离 ：** 将冷数据和热数据分离，用更低成本的磁盘存储冷数据。
   -  **Rollup(数据卷) ：**   把超过指定时间段的数据**按预定的方式进行聚合**。
   -  **备份归档 ：**  一组数据集，专门用于长期保存并供将来参考。当原始数据或备份从初始站点移除后，数据可在归档的备份中找到。 
   -  **数据裁剪 ：**  裁剪数据。降低存储。
2. **降低内存使用率：**
   - **原因：** 因为ES使用的 Lucene ，底层存储采用了倒排索引，为了减少磁盘IO，引入FST的二级索引存储在内存上。导致存储内容越多，内存使用越大，以至于ES的JVM堆内存占用率过高。
   - **解决方案：** 将FST存储从堆内存中迁移到堆外内存并且使用LRU淘汰策略，并在堆内存中存在堆内FST缓存key对应着 Buffer 存储着数据地址，通过零拷贝避免将堆外内存写入堆内再读取数据。通过把堆内对象设置为 WeakRefrence  ，在GC回收时也回收掉堆外对象。
   - **原理：** JVM存在指针压缩，使用64位的指针会比32位的指针多使用1.5倍的内存,并且大指针寻址占用较大的带宽同时GC的压力也会加大。在JVM中为了减少内存消耗会启用压缩指针。因为32位的地址能表示4G的对象地址，当内存小于4G，JVM会直接去除高32位，只用低32位来寻址。而当内存大于32G(寄存器使用35位所有上限是32G)，压缩指针就会失效，强制使用64位的指针。

 