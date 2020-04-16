#System


System.identityHashCode
-----------------------
类似于Object.hashcode，一个对象在其生命周期中identity hash code肯定不会变化

     System.identityHashCode与Object.hashcode区别：
        System.identityHashCode: 不会受hashcode方法重写的影响,返回与默认的hashcode返回一样
        Object.hashcode: 会被重写    
