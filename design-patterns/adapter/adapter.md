adapter
===

    Target:定义所需要的方法 Y
    Client: 方法具体处理
    Adaptee : 被适配者 X
    Adapter : 适配者 class


1.类适配器模式：
---

    extend X implements Y

2.对象适配器模式：
---

    extend Y {
    
        X x;
    }