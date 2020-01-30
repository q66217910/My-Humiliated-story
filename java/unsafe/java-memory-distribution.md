java内存分布
===
java的内存模型
---
    
    对象头:markdown（4字节）/Class对象指针（4字节，存储对象自身运行数据，hashcode/gc分代年龄
    实例数据: 对象的实际数据 
    对齐填充: 对齐 （按8字节对齐） 占位符作用
 
偏移量
--
什么是偏移量？

偏移量是指在创建对象后，java内存开辟了一块地址(header+content+padding),例如对象中有一个属性，
该属性的内存存在content，偏移量就为header的长度。多个属性就为前面的总和。 
   
在unsafe中，

unsafe.getInt(object, offset): 获取对象中该偏移量的值
unsafe.objectFieldOffset(field) : 获取某个属性在某个实例中的偏移量



    