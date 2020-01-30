AtomicReference(原子操作类)
===
对某个元素进步原子操作,对象中存储了一个volatile的value值,该值为
我们要原子操作的值。AtomicReference操作基本通过Unsafe类进行内存操作。

valueOffset: value属性在AtomicReference中的偏移量(因为每个对象的偏移量相同，故是static)

```java
class AtomicReference<V> implements java.io.Serializable{ 

     private static final Unsafe unsafe = Unsafe.getUnsafe();
     //value属性在AtomicReference中的偏移量
     private static final long valueOffset;

     static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicReference.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
     }

     private volatile V value; 
                     
     //懒加载，（）
     public final void lazySet(V newValue) {
             unsafe.putOrderedObject(this, valueOffset, newValue);
     }
}
```