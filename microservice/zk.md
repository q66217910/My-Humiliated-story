## 1.Znode

*Znode是 Zookeeper中数据的最小单元*

**作用：** 存储数据，或者挂载子节点。

#### 1.Znode的类型

```java
public enum CreateMode {
    PERSISTENT(0, false, false, false, false),//持久化节点
    EPHEMERAL(1, true, false, false, false),//临时节点
    PERSISTENT_SEQUENTIAL(2, false, true, false, false),//自增持久化节点
    EPHEMERAL_SEQUENTIAL(3, true, true, false, false),//自增临时节点
    CONTAINER(4, false, false, true, false), //容器节点
    PERSISTENT_WITH_TTL(5, false, false, false, true), //持久化过期
    PERSISTENT_SEQUENTIAL_WITH_TTL(6, false, true, false, true); // 持久化顺序过期
}
```

- **ephemeral：** 是否为临时节点（EPHEMERAL临时、PERSISTENT持久化，失去客户端连接时是否自动删除/客户端主动删除节点）
- **sequential ：** 创建node时设置顺序标识，由父节点维护，并一直自增。
- **isContainer：** 是否为容器节点
- **isTTL：** 在过期时间内未被修改会被删除节点

#### 2.Znode节点状态

```java
public class Stat implements Record {
	private long czxid;//节点被创建的Zxid值
    private long mzxid;//节点被修改的Zxid值
    private long ctime;//节点被创建的时间
    private long mtime;//节点最后一次被修改的时间
    private int version;//节点被修改的版本号
    private int cversion;//节点的所拥有子节点被修改的版本号
    private int aversion;//节点的ACL被修改的版本号
    private long ephemeralOwner;//如果此节点为临时节点，那么它的值为这个节点 拥有者的会话ID；
    private int dataLength;//节点数据域的长度
    private int numChildren;//节点拥有的子节点个数
    private long pzxid;//子节点最有一次被修改时的事务ID
}
```



## 2.watcher

-  **一次性：** 客户端和服务端 ，一旦watcher被触发，都会被移除 
- **时效性：**  在session_timeout之内，都可能保证 watches 

#### 1.watcher类型

```java
enum WatcherType {
        Children(1), //ChildWatches,znode的孩子节点发生变更触发的watch事件
        Data(2), //DataWatches,基于znode节点的数据变更从而触发 watch 事件
        Any(3); //任意一种类型
 }
```

-  **DataWatches:**  
-  **ChildWatches** 

#### 2.watcher的机制

- **注册：** client向server注册watcher

  ```java
  public class ZooKeeper implements AutoCloseable {
      ZooKeeper(String connectString, int sessionTimeout, Watcher watcher);//构造函数
      List<String> getChildren(final String path, Watcher watcher);//获取路径下的子节点
      byte[] getData(final String path, Watcher watcher, Stat stat);//获取路径下的节点状态
      Stat exists(final String path, Watcher watcher);//判断节点是否存在
  }
  ```

  1. **创建WatchRegistration对象：**

     WatchRegistration对象中包含了*节点路径*和*Watcher对象*

     ```java
     public abstract static class WatchRegistration {
         private Watcher watcher;
         private String clientPath;
     }
     ```

  2. **把WatchRegistration对象封装进Packet对象中：**

      packet是一个最小的通信单元,用于传输信息。

     ```java
      static class Packet {
      	RequestHeader requestHeader;//请求头
         ReplyHeader replyHeader;//
         Record request;//请求数据
         Record response;//响应数据
         ByteBuffer bb;
         String clientPath;//客户端视图路径
         String serverPath;//服务端视图路径
         boolean finished;//是否请求结束
         AsyncCallback cb;//响应回调
         Object ctx;//上下文
         WatchRegistration watchRegistration;//监听注册
         public boolean readOnly;//是否只读
         WatchDeregistration watchDeregistration; 
      }
     ```

  3. **发送packet对象：**

     把packet添加到队列中,通过*SendThread*发送请求。

     ```java
     public class ClientCnxn {
     	public Packet queuePacket(）{
              packet = new Packet();
              //...
              outgoingQueue.add(packet);
              sendThread.getClientCnxnSocket().packetAdded();
              return packet;
         }
     }
     ```

- **存储：** 服务端接受到请求，采用责任链的方式处理请求。

  1. **根据不同操作不同处理**

     ```java
     /**
     *  操作类型
     **/
     public interface OpCode {
     	int notification = 0;//通知
         int create = 1;//创建节点
         int delete = 2;//删除节点
         int exists = 3;//节点是否存在
         int getData = 4;//获取节点数据
         int setData = 5;//保存节点数据
         int getACL = 6;//获取权限
         int setACL = 7;//设置权限
         int getChildren = 8;//获取子节点
         int sync = 9;//同步数据
         int ping = 11;//ping
         int getChildren2 = 12;//获取子节点
         int check = 13;
         int multi = 14;
         int create2 = 15;
         int reconfig = 16;
         int checkWatches = 17;//检查监听
         int removeWatches = 18;//移除监听
         int createContainer = 19;//创建容器
         int deleteContainer = 20;//删除容器
         int createTTL = 21;
         int multiRead = 22;
         int auth = 100;//认证
         int setWatches = 101;//设置监听
         int sasl = 102;
         int getEphemerals = 103;
         int getAllChildrenNumber = 104;
         int setWatches2 = 105;
         int addWatch = 106;//添加监听
         int whoAmI = 107;
         int createSession = -10;
         int closeSession = -11;
         int error = -1;
     }
     ```

  2. **保存监听到watchTable和watch2Paths**

     ```java
     public class WatchManager implements IWatchManager {
     	//路径-Watcher
         private final Map<String, Set<Watcher>> watchTable = new HashMap<>();
         //Watcher-路径
         private final Map<Watcher, Set<String>> watch2Paths = new HashMap<>();
         
         public synchronized boolean addWatch(String path, Watcher watcher,
                                              WatcherMode watcherMode){
             Set<Watcher> list = watchTable.get(path);
             if (list == null) {
                 list = new HashSet<>(4);
                 watchTable.put(path, list);
             }
             list.add(watcher);
     
             Set<String> paths = watch2Paths.get(watcher);
             if (paths == null) {
                 paths = new HashSet<>();
                 watch2Paths.put(watcher, paths);
             }
     
             watcherModeManager.setWatcherMode(watcher, path, watcherMode);
             return paths.add(path);
         }
     } 
     ```

  3. **客户端收到服务端响应,将WatchRegistration注册到 ZKWatchManager中** 

     ```java
     public class ClientCnxn {
     	protected void finishPacket(Packet p) {
             int err = p.replyHeader.getErr();
             if (p.watchRegistration != null) {
                 //注册
                 p.watchRegistration.register(err);
             }
         }
     }
     ```

  4. **ZKWatchManager保存监听**

     ```java
     class ZKWatchManager implements ClientWatchManager {
         //DataWatches
      	private final Map<String, Set<Watcher>> dataWatches = new HashMap<>();
         private final Map<String, Set<Watcher>> existWatches = new HashMap<>();
         //ChildWatches
         private final Map<String, Set<Watcher>> childWatches = new HashMap<>();
         private final Map<String, Set<Watcher>> persistentWatches = new HashMap<>();
         private final Map<String, Set<Watcher>> persistentRecursiveWatches 
             = new HashMap<>();
     }
     ```

- **通知：**   

  1. **当发生变化时发起通知**
     在server端watcher触发一次就会删除

     ```java
     public class WatchManager implements IWatchManager {
     
         public WatcherOrBitSet triggerWatch(String path, 
                                             EventType type, WatcherOrBitSet supress) {
             Set<Watcher> watchers = new HashSet<>();
             //...从watchtable中获取watcher
             for (Watcher w : watchers) {
                 if (supress != null && supress.contains(w)) {
                     continue;
                 }
                 w.process(e);
             }
         }
         
     }
     ```

  2. **发送通知给客户端**

     ```java
     public class NIOServerCnxn extends ServerCnxn {
     
         public void process(WatchedEvent event) {
             ReplyHeader h = new ReplyHeader(ClientCnxn.NOTIFICATION_XID, -1L, 0);
     
             // Convert WatchedEvent to a type that can be sent over the wire
             WatcherEvent e = event.getWrapper();
     		//发送响应通知
             int responseSize = sendResponse(h, e, "notification",
                                             null, null, ZooDefs.OpCode.error);
             ServerMetrics.getMetrics().WATCH_BYTES.add(responseSize);
         }
         
     }
     ```

  3. **客户端收到通知，*SendThread*处理响应**

     ```java
      class SendThread extends ZooKeeperThread {
      	
          void readResponse(ByteBuffer incomingBuffer) throws IOException {
              ByteBufferInputStream bbis = new ByteBufferInputStream(incomingBuffer);
              BinaryInputArchive bbia = BinaryInputArchive.getArchive(bbis);
              ReplyHeader replyHdr = new ReplyHeader();
     
              replyHdr.deserialize(bbia, "header");
              switch (replyHdr.getXid()) {
                case PING_XID:
                      return;
                case AUTHPACKET_XID:
                      return;
                case NOTIFICATION_XID:
                     //通知事件
                     //...
                     //交由eventThread处理
                     eventThread.queueEvent(we);
              }
          }
          
      }
     ```

  4. **事件处理**

     ```java
     class EventThread extends ZooKeeperThread {
     	private void processEvent(Object event) {
             if (event instanceof WatcherSetEventPair) {
                         WatcherSetEventPair pair = (WatcherSetEventPair) event;
                         for (Watcher watcher : pair.watchers) {
                             try {
                                 watcher.process(pair.event);
                             } catch (Throwable t) {
                                 LOG.error("Error while calling watcher.", t);
                             }
                         }
                     }
         }
     }
     ```

     

  

  

