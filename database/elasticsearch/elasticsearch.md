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
   - **删除：** 



