Raft:
===
状态：
        
        follower：所有结点都以 follower 的状态开始。
                如果没收到 leader消息则会变成 candidate状态。
        candidate：会向其他结点“拉选票”，如果得到大部分的票则成为leader。
                这个过程就叫做Leader选举(Leader Election)。
        leader： 所有对系统的修改都会先经过leader。
        
        
        各个状态的变迁：
        1.开始： follower
        2.开始选举： follower->candidate
        3.开始新的一轮选举：candidate->candidate
        4.发现当前的leader或者开启了新一轮的term： candidate-> follower
        5.选举获得超过半数选票： candidate->leader
        6.发现了更高的term：leader-> follower
        
        leader节点会一直给 其他节点发送心跳,若一定时间没有收到leader的心跳，follower发起选举<2>
        如果收到半数的选票变为leader状态 <5>,如果有其他节点的term更高退回follower<4>
       
Leader election（选举）
---
一个系统最多只有一个leader节点,会发送心跳
Note：leader你不BB别人就会有人跳出来BB你

term(任期)
---
任期递增,充当了逻辑时钟

    选举过程
    1.增加本地节点term,并且切换到candidate,投票给自己
    2.并行给其他节点发送 RPC
    3.等待其他节点的选票结果
        A.获得超过半数选票，成为leader
        B.被告知别人已经当选,切换会follower
        C.没有收到回复,保持candidate,过段时间重新发起选举
        
Log replication (日志复制)
---
客户端一切请求发送到leader,leader来调度这些请求的顺序,并给follower同步,保证leader
与follower的一致性,两者以同样的顺序执行请求


状态机
---
为保证一致性,leader会将所要请求commands封装到log entry,将这些log entry复制(replicate)给follower，
然后所有节点按照相同的顺序执行(apply) log entry中的command,以此来保证一致性。

    请求过程：
    1. leader append logEntry
    2. leader 并行同步RPC follower logEntry
    3. leader 等待多数节点反馈
    4. leader apply entry to state machine
    5. leader reply to client
    6. leader notify follower apply log
    

log matching
---
leader在某一term的任一位置只会创建一个log entry。
leader在AppendEntries中包含最新log entry之前的一个log 的term和index。

    1.leader 初始化nextIndex[x]为 leader最后一个log index + 1 （index为leader选举成功的心跳）
    2.AppendEntries里prevLogTerm prevLogIndex来自 logs[nextIndex[x] - 1]
    3.如果follower判断prevLogIndex位置的log term不等于prevLogTerm，那么返回 False，否则返回True
    4.leader收到follower的回复，如果返回值是False，则nextIndex[x] -= 1, 跳转到2.
    5.同步nextIndex[x]后的所有log entries
    
    
脑裂
---
在网络分割的状态下，出现多个leader但在不同的term
