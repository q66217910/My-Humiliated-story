一致性算法
===

Raft:
---
状态：
        
        follower：所有结点都以 follower 的状态开始。
                如果没收到 leader消息则会变成 candidate状态。
        candidate：会向其他结点“拉选票”，如果得到大部分的票则成为leader。
                这个过程就叫做Leader选举(Leader Election)。
        leader： 所有对系统的修改都会先经过leader。
       
       
Leader election（选举）
---
原来的leader挂掉后，必须选出一个新的leader

        
Log replication (日志复制)
---
