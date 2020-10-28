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



#### 3. Index(索引)

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



#### 4.  Node(节点)

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



#### 5. Shard(分片)





## 2.Elasticsearch的读写过程

#### 1.写操作：

1. **倒排索引带来的不变性：** 倒排索引被写入磁盘后，不可改变。

   - **不需要锁：** 因为不更新索引，没有多线程修改数据的问题。
   - **文件系统缓存：** 因为索引不变，数据一旦进入缓存，大部分请求会命中缓存。

2. **分段( segment )搜索：** 每一个segment本身就是一个倒排索引。一个 *Lucene*  Indx(分片)包含了3个segment
   和一个commit point。

   