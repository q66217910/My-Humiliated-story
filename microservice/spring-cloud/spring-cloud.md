# Spring-cloud

```
1.服务发现: 服务发现允许集群中的进程和服务找到彼此并进行通信。
2.负载平衡: 改进跨多个计算资源的工作负载分布.
3.分布式系统相关的复杂性: 包括网络问题，延迟开销，带宽问题，安全问题。
4.减少性能问题: 减少因各种操作开销导致的性能问题
5.解决冗余问题:冗余问题经常发生在分布式系统中。
```

## 1.服务发现

```
在微服务中，服务发现用于集群中的进程和服务彼此发现并进行通信
常用的服务发现组件：
	1.eureka
	2.zookeeper
	3.Consul
```

### 1-1.Eureka

- **Eureka InstanceInfo(元数据)**

  ```java
  public class InstanceInfo {
      
      //用于指定实例属于哪个数据中心(eureka.instance.data-center-info)
      //Netflix, Amazon, MyOwn三个类型，默认MyOwn
      //作用：例如要在上部署Eureka需要Amazon识别
      private volatile DataCenterInfo dataCenterInfo;
      //(eureka.instance.data-center-hostname)	
    private volatile String hostName;
      //1.在appName范围内唯一值,配置(eureka.instance.instance-id)
      //2.没有配置instance.instance-id时若dataCenterInfo实现了UniqueIdentifier,调用getName
      //3.都没有返回hostName
      //因为server缓存是个三级的Map,所以要保证appName范围内instanceId唯一
      private volatile String instanceId;
      //ip 地址
      private volatile String ipAddr;
      //application的名称(spring.application.name)
      private volatile String appName;
     	//实例状态（UP:准备接受流量  DOWN:停止运行 STARTING:初始化中 OUT_OF_SERVICE:关闭流量）
      private volatile InstanceStatus overriddenStatus = InstanceStatus.UNKNOWN;
      //实例的操作(ADDED: 实例新增  MODIFIED:更新  DELETED:实例移除)
      private volatile ActionType actionType;
      //上次操作时间戳
      private volatile Long lastUpdatedTimestamp;
  }
  ```
  
- **Eureka server urls**:

  ```
  Region: 地理上的分区（例如华东/华南）
  Zone: 为Region内具体分区（华东-1/华东-2）
  
  配置:
  eureka.client.region: 配置eureka region
  eureka.client.service-url.defaultZone:  使用默认ZONE地址(defaultZone更换可以配置不同zone)
  ```

  ```java
  @ConfigurationProperties(EurekaClientConfigBean.PREFIX)
  public class EurekaClientConfigBean implements EurekaClientConfig, Ordered {
  	
      private Map<String, String> serviceUrl = new HashMap<>();
      
      public static Map<String, List<String>> getServiceUrlsMapFromConfig(
  			EurekaClientConfig clientConfig, String instanceZone, 
          	boolean preferSameZone) {
      Map<String, List<String>> orderedUrls = new LinkedHashMap<>();
      //获取region
      String region = getRegion(clientConfig);
      //获取该region的zone
      String[] availZones = clientConfig.getAvailabilityZones(clientConfig.getRegion());
      if (availZones == null || availZones.length == 0) {
          availZones = new String[1];
          availZones[0] = DEFAULT_ZONE;
      }
  	//计算使用哪个zone
      int myZoneOffset = getZoneOffset(instanceZone, preferSameZone, availZones);
  
      String zone = availZones[myZoneOffset];
      //获取eureka集群地址
      List<String> serviceUrls = clientConfig.getEurekaServerServiceUrls(zone);
      if (serviceUrls != null) {
          orderedUrls.put(zone, serviceUrls);
      }
  	//zone-eureke服务地址
      return orderedUrls;
  }
      
      //获取region(eureka.client.region)
      public static String getRegion(EurekaClientConfig clientConfig) {
          String region = clientConfig.getRegion();
          if (region == null) {
              region = DEFAULT_REGION;
          }
          region = region.trim().toLowerCase();
          return region;
   }
      
      //获取zone(eureka.client.availability-zones)
      public String[] getAvailabilityZones(String region) {
  		String value = this.availabilityZones.get(region);
  		if (value == null) {
  			value = DEFAULT_ZONE;
  		}
  		return value.split(",");
  }
      
      @Override
      //获取eureka集群地址(eureka.client.service-url)
  	public List<String> getEurekaServerServiceUrls(String myZone) {
  		String serviceUrls = this.serviceUrl.get(myZone);
  		if (serviceUrls == null || serviceUrls.isEmpty()) {
  			serviceUrls = this.serviceUrl.get(DEFAULT_ZONE);
  		}
  		if (!StringUtils.isEmpty(serviceUrls)) {
  			final String[] serviceUrlsSplit = StringUtils
  					.commaDelimitedListToStringArray(serviceUrls);
  			List<String> eurekaServiceUrls = new ArrayList<>(serviceUrlsSplit.length);
  			for (String eurekaServiceUrl : serviceUrlsSplit) {
  				if (!endsWithSlash(eurekaServiceUrl)) {
  					eurekaServiceUrl += "/";
  				}
  				eurekaServiceUrls.add(eurekaServiceUrl.trim());
  			}
  			return eurekaServiceUrls;
  		}
  		return new ArrayList<>();
  	}
  }
  ```

- **HttpClient**

  Eureka的httpClient调用链

  ```
1.SessionedEurekaHttpClient: 每隔一段时间重新建立会话
  2.RetryableEurekaHttpClient: 失败重试，尝试所有服务端地址，直至所有的都失败
3.RedirectingEurekaHttpClient: 重定向，例如注册与查询分离
  4.AbstractJerseyEurekaHttpClient(默认)：具体请求
  4-1.RestTemplateHttpClient: rest风格
  ```
  
  ```java
  public final class EurekaHttpClients {
      
      static EurekaHttpClientFactory canonicalClientFactory(final String name,
                        final EurekaTransportConfig transportConfig,
                        final ClusterResolver<EurekaEndpoint> clusterResolver,
                        final TransportClientFactory transportClientFactory) {
          return new EurekaHttpClientFactory() {
              @Override
              public EurekaHttpClient newClient() {
                  return new SessionedEurekaHttpClient(
                          name,
                          RetryableEurekaHttpClient.createFactory(
                                  name,
                                  transportConfig,
                                  clusterResolver,
                                  RedirectingEurekaHttpClient
                              .createFactory(transportClientFactory),
                                  ServerStatusEvaluators.legacyEvaluator()),
                          transportConfig
                      .getSessionedClientReconnectIntervalSeconds() * 1000
                  );
              }
  
              @Override
              public void shutdown() {
                  wrapClosable(clusterResolver).shutdown();
              }
          };
      }   
  }
  
  public class SessionedEurekaHttpClient extends EurekaHttpClientDecorator {
      
      @Override
      protected <R> EurekaHttpResponse<R> execute(RequestExecutor<R> requestExecutor) {
          long now = System.currentTimeMillis();
          long delay = now - lastReconnectTimeStamp;
          if (delay >= currentSessionDurationMs) {
           	//超过设置时间(默认2小时)
              lastReconnectTimeStamp = now;
              //重新设置过期时间，会随机，防止同一时间一起过期
              currentSessionDurationMs = randomizeSessionDuration(sessionDurationMs);
              //shutdown
              TransportUtils.shutdown(eurekaHttpClientRef.getAndSet(null));
          }
  
          EurekaHttpClient eurekaHttpClient = eurekaHttpClientRef.get();
          if (eurekaHttpClient == null) {
              //重新创建连接
              eurekaHttpClient = TransportUtils
                  .getOrSetAnotherClient(eurekaHttpClientRef, clientFactory
                                         .newClient());
          }
          return requestExecutor.execute(eurekaHttpClient);
      }
  }
  
  public class RetryableEurekaHttpClient extends EurekaHttpClientDecorator {
      
      @Override
      protected <R> EurekaHttpResponse<R> execute(RequestExecutor<R> requestExecutor) {
          List<EurekaEndpoint> candidateHosts = null;
          int endpointIdx = 0;
          //最大重试次数
          for (int retry = 0; retry < numberOfRetries; retry++) {
              EurekaHttpClient currentHttpClient = delegate.get();
              EurekaEndpoint currentEndpoint = null;
              if (currentHttpClient == null) {
                  //没有配置service url
                  if (candidateHosts == null) {
                      candidateHosts = getHostCandidates();
                      if (candidateHosts.isEmpty()) {
                          throw new TransportException("cluster server list is empty");
                      }
                  }
                  if (endpointIdx >= candidateHosts.size()) {
                      throw new TransportException("Cannot execute request");
                  }
  
                  //获取节点
                  currentEndpoint = candidateHosts.get(endpointIdx++);
                  //创建下一个HttpClient
                  currentHttpClient = clientFactory.newClient(currentEndpoint);
              }
  
              try {
                  //发送请求
                  EurekaHttpResponse<R> response = requestExecutor
                      .execute(currentHttpClient);
                  if (serverStatusEvaluator.accept(response.getStatusCode(), 	requestExecutor.getRequestType())) {
                      delegate.set(currentHttpClient);
                      if (retry > 0) {
                          logger.info("Request execution succeeded on retry #{}", retry);
                      }
                      return response;
                  }
              } catch (Exception e) {
  
              }
              delegate.compareAndSet(currentHttpClient, null);
              if (currentEndpoint != null) {
                quarantineSet.add(currentEndpoint);
              }
        }
          throw new TransportException("Retry limit reached");
      }
      
  }
  
  public class RedirectingEurekaHttpClient extends EurekaHttpClientDecorator {
      
      @Override
      protected <R> EurekaHttpResponse<R> execute(RequestExecutor<R> requestExecutor) {
          EurekaHttpClient currentEurekaClient = delegateRef.get();
          if (currentEurekaClient == null) {
              AtomicReference<EurekaHttpClient> currentEurekaClientRef = new AtomicReference<>(factory.newClient(serviceEndpoint));
              try {
                  EurekaHttpResponse<R> response = executeOnNewServer(requestExecutor, currentEurekaClientRef);
                  TransportUtils.shutdown(delegateRef.getAndSet(currentEurekaClientRef.get()));
                  return response;
              } catch (Exception e) {
                  logger.error("Request execution error", e);
                  TransportUtils.shutdown(currentEurekaClientRef.get());
                  throw e;
              }
          } else {
              try {
                  return requestExecutor.execute(currentEurekaClient);
              } catch (Exception e) {
                  logger.error("Request execution error", e);
                  delegateRef.compareAndSet(currentEurekaClient, null);
                  currentEurekaClient.shutdown();
                  throw e;
              }
          }
      }
      
  }
  
  @Configuration
  //指定使用RestTemplateHttpClient
  public class EurekaConfig {
  
      @Bean
      public AbstractDiscoveryClientOptionalArgs<?> optionalArgs() {
          return new RestTemplateDiscoveryClientOptionalArgs();
      }
  
  }
  ```
  
- **服务注册**：

  客户端：

  ​	第一次延迟(eureka.client.initial-instance-info-replication-interval-seconds,默认40s)，如果注册失败，

  ​	则每隔(eureka.client.instance-info-replication-interval-seconds,默认30s)进行重注册

  ```java
  public class DiscoveryClient implements EurekaClient {
      
      private int initialInstanceInfoReplicationIntervalSeconds = 40;
      
  	private void initScheduledTasks() {
      
      if (clientConfig.shouldRegisterWithEureka()) {
          //...
          //服务注册
          instanceInfoReplicator = new InstanceInfoReplicator(
                  this,
                 instanceInfo,
                  clientConfig.getInstanceInfoReplicationIntervalSeconds(),
                  2);
          //服务注册开始
          instanceInfoReplicator.start(
              clientConfig.getInitialInstanceInfoReplicationIntervalSeconds());
      } else {
          logger.info("Not registering with Eureka server per configuration");
      	}
  	}
      
      //注册
      boolean register() throws Throwable {
          EurekaHttpResponse<Void> httpResponse;
          try {
              httpResponse = eurekaTransport.registrationClient.register(instanceInfo);
          } catch (Exception e) {
              throw e;
          }
          return httpResponse.getStatusCode() == Status.NO_CONTENT.getStatusCode();
      }
  }
  
  class InstanceInfoReplicator implements Runnable {
      
      public void start(int initialDelayMs) {
          //上锁,防止重复注册
          if (started.compareAndSet(false, true)) {
              //初始注册
              instanceInfo.setIsDirty(); 
              //默认40s后执行
              Future next = scheduler.schedule(this, initialDelayMs, TimeUnit.SECONDS);
              scheduledPeriodicRef.set(next);
          }
      }
      
      public void run() {
          try {
              //刷新本地InstanceInfo
              discoveryClient.refreshInstanceInfo();
  
              //若上次执行已经被置脏了，则不执行
              Long dirtyTimestamp = instanceInfo.isDirtyWithTime();
              if (dirtyTimestamp != null) {
                  //服务注册
                  discoveryClient.register();
                  //置脏
                  instanceInfo.unsetIsDirty(dirtyTimestamp);
              }
          } catch (Throwable t) {
              logger.warn("There was a problem with the instance info replicator", t);
          } finally {
              //若注册失败，则延迟后重新服务
              Future next = scheduler.schedule(this, 
                          replicationIntervalSeconds,TimeUnit.SECONDS);
              scheduledPeriodicRef.set(next);
          }
      }
  }
  
  public abstract class AbstractJerseyEurekaHttpClient implements EurekaHttpClient {
      
      @Override
      public EurekaHttpResponse<Void> register(InstanceInfo info) {
          //注册地址
          String urlPath = "apps/" + info.getAppName();
          ClientResponse response = null;
          try {
              Builder resourceBuilder = jerseyClient.resource(serviceUrl)
                  .path(urlPath).getRequestBuilder();
              //添加服务注册头标识
              addExtraHeaders(resourceBuilder);
              response = resourceBuilder
                      .header("Accept-Encoding", "gzip")
                      .type(MediaType.APPLICATION_JSON_TYPE)
                      .accept(MediaType.APPLICATION_JSON)
                      .post(ClientResponse.class, info);
              return anEurekaHttpResponse(response.getStatus())
                  .headers(headersOf(response)).build();
          } finally {
              if (response != null) {
                  response.close();
              }
          }
      }
      
      @Override
      //增加注册头信息
      protected void addExtraHeaders(Builder webResource) {
          webResource.header(PeerEurekaNode.HEADER_REPLICATION, "true");
      }
      
  }
  ```

  服务端：

  ```java
  public class ApplicationResource {
  
      @POST
      @Consumes({"application/json", "application/xml"})
      public Response addInstance(InstanceInfo info,
                                  @HeaderParam(PeerEurekaNode.HEADER_REPLICATION) String isReplication) {
          //检验元数据
          if (isBlank(info.getId())) {
              return Response.status(400).entity("Missing instanceId").build();
          } else if (isBlank(info.getHostName())) {
              return Response.status(400).entity("Missing hostname").build();
          } else if (isBlank(info.getIPAddr())) {
              return Response.status(400).entity("Missing ip address").build();
          } else if (isBlank(info.getAppName())) {
              return Response.status(400).entity("Missing appName").build();
          } else if (!appName.equals(info.getAppName())) {
              return Response.status(400).entity("Mismatched appName, expecting " + appName + " but was " + info.getAppName()).build();
          } else if (info.getDataCenterInfo() == null) {
              return Response.status(400).entity("Missing dataCenterInfo").build();
          } else if (info.getDataCenterInfo().getName() == null) {
              return Response.status(400).entity("Missing dataCenterInfo Name").build();
          }
  
          //获取数据中心数据
          DataCenterInfo dataCenterInfo = info.getDataCenterInfo();
          if (dataCenterInfo instanceof UniqueIdentifier) {
              String dataCenterInfoId = ((UniqueIdentifier) dataCenterInfo).getId();
              if (isBlank(dataCenterInfoId)) {
                  boolean experimental = "true"
                      .equalsIgnoreCase(serverConfig.getExperimental(
                          "registration.validation.dataCenterInfoId"));
                  if (experimental) {
                      String entity = "DataCenterInfo of type " + dataCenterInfo
                          .getClass() + " must contain a valid id";
                      return Response.status(400).entity(entity).build();
                  } else if (dataCenterInfo instanceof AmazonInfo) {
                      AmazonInfo amazonInfo = (AmazonInfo) dataCenterInfo;
                      String effectiveId = amazonInfo
                          .get(AmazonInfo.MetaDataKey.instanceId);
                      if (effectiveId == null) {
                          amazonInfo.getMetadata()
                              .put(AmazonInfo.MetaDataKey
                                   .instanceId.getName(), info.getId());
                      }
                  }
              }
          }
  
          //服务注册
          registry.register(info, "true".equals(isReplication));
          return Response.status(204).build();  // 204 to be backwards compatible
      } 
  }
  
  public static final int DEFAULT_DURATION_IN_SECS = 90;
  
  @Singleton
  public class PeerAwareInstanceRegistryImpl extends AbstractInstanceRegistry 
      implements PeerAwareInstanceRegistry {
      
      @Override
      public void register(final InstanceInfo info, final boolean isReplication) {
          //默认续期时间
          int leaseDuration = Lease.DEFAULT_DURATION_IN_SECS;
          //因为是注册,所以不执行续期相关
          if (info.getLeaseInfo() != null 
              && info.getLeaseInfo().getDurationInSecs() > 0) {
              leaseDuration = info.getLeaseInfo().getDurationInSecs();
          }
          //注册
          super.register(info, leaseDuration, isReplication);
          //同步到集群其他节点
          replicateToPeers(Action.Register, info.getAppName(),
                           info.getId(), info, null, isReplication);
      }
  }
  ```

- **心跳**:

  续期对象：

  ```java
  public class Lease<T> {
      private T holder;
      private long evictionTimestamp;
      private long registrationTimestamp;
      //实例开始成功运行时间
      private long serviceUpTimestamp;
      //上一次调用时间
      private volatile long lastUpdateTimestamp;
      private long duration;
  }
  
  public class LeaseInfo {
  
      private long registrationTimestamp;
      //上次续约时间
      private long lastRenewalTimestamp;
      
      private long evictionTimestamp;
      //实例开始成功运行时间
      private long serviceUpTimestamp;
      
  }
  ```

  客户端：

  

  ```java
  @Singleton
  public class DiscoveryClient implements EurekaClient {
      @Inject
      DiscoveryClient(ApplicationInfoManager applicationInfoManager, 
                   EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs args,
                      Provider<BackupRegistry> backupRegistryProvider) {
          
          //......
          //定时任务线程池
          //1.心跳定时
       	//2.缓存定时
          //3.服务注册
          scheduler = Executors.newScheduledThreadPool(2,
                      new ThreadFactoryBuilder()
                              .setNameFormat("DiscoveryClient-%d")
                              .setDaemon(true)
                              .build());
          //心跳定时
          heartbeatExecutor = new ThreadPoolExecutor(1, 
              clientConfig.getHeartbeatExecutorThreadPoolSize(), 0, TimeUnit.SECONDS,
                      new SynchronousQueue<Runnable>(),
                      new ThreadFactoryBuilder()
                              .setNameFormat("DiscoveryClient-HeartbeatExecutor-%d")
                              .setDaemon(true)
                              .build()
          //......
          //开启定时
          initScheduledTasks();
          //......   
      }
                                                     
      private void initScheduledTasks() {
          //.......
          //心跳定时(默认30s)
          scheduler.schedule(
                      new TimedSupervisorTask(
                              "heartbeat",
                              scheduler,
                              heartbeatExecutor,
                              renewalIntervalInSecs,
                              TimeUnit.SECONDS,
                              expBackOffBound,
                          	//心跳任务
                              new HeartbeatThread()
                      ),
                      renewalIntervalInSecs, TimeUnit.SECONDS);
          //.......
      }
  }
                                                     
  private class HeartbeatThread implements Runnable {
  
      public void run() {
          	//续期
              if (renew()) {
                  //若续期成功,将上次续期成功时间设置为当前时间戳
                  lastSuccessfulHeartbeatTimestamp = System.currentTimeMillis();
              }
          }
      
      //续期
      boolean renew() {
          EurekaHttpResponse<InstanceInfo> httpResponse;
          try {
              //发送心跳
              httpResponse = eurekaTransport.registrationClient
                  .sendHeartBeat(instanceInfo.getAppName(),
                                 instanceInfo.getId(), instanceInfo, null);
              //eureka服务端404
              if (httpResponse.getStatusCode() == 404) {
                  //心跳计数器
                  REREGISTER_COUNTER.increment();
                  //设置标志
                  long timestamp = instanceInfo.setIsDirtyWithTime();
                  boolean success = register();
                  if (success) {
                      instanceInfo.unsetIsDirty(timestamp);
                  }
                  return success;
              }
              //200，返回成功
              return httpResponse.getStatusCode() == 200;
          } catch (Throwable e) {
              return false;
          }
      }
  }
                                                     
  public class RestTemplateEurekaHttpClient implements EurekaHttpClient {
      
      @Override
      public EurekaHttpResponse<InstanceInfo> sendHeartBeat(String appName, String id, InstanceInfo info, InstanceStatus overriddenStatus) {
          String urlPath = serviceUrl + "apps/" + appName + '/' + id + "?status="
  				+ info.getStatus().toString() + "&lastDirtyTimestamp="
  				+ info.getLastDirtyTimestamp().toString() + (overriddenStatus != null
  						? "&overriddenstatus=" + overriddenStatus.name() : "");
  
  		ResponseEntity<InstanceInfo> response = restTemplate.exchange(urlPath,
  				HttpMethod.PUT, null, InstanceInfo.class);
  
  		EurekaHttpResponseBuilder<InstanceInfo> eurekaResponseBuilder = anEurekaHttpResponse(
  				response.getStatusCodeValue(), InstanceInfo.class)
  						.headers(headersOf(response));
  
  		if (response.hasBody()) {
  			eurekaResponseBuilder.entity(response.getBody());
  		}
  
  		return eurekaResponseBuilder.build();
      }
  }
  ```

  服务端：

  ```java
  @PUT
  public Response renewLease(
              @HeaderParam(PeerEurekaNode.HEADER_REPLICATION) String isReplication,
              @QueryParam("overriddenstatus") String overriddenStatus,
              @QueryParam("status") String status,
              @QueryParam("lastDirtyTimestamp") String lastDirtyTimestamp) {
      	//是否是服务注册
          boolean isFromReplicaNode = "true".equals(isReplication);
      	//续期
          boolean isSuccess = registry.renew(app.getName(), id, isFromReplicaNode);
  
          //续期失败
          if (!isSuccess) {
              return Response.status(Status.NOT_FOUND).build();
          }
         
          Response response;
      	//检查时间防止实例已经发生改变
          if (lastDirtyTimestamp != null 
              && 	serverConfig.shouldSyncWhenTimestampDiffers()) {
              response = this.validateDirtyTimestamp
                  (Long.valueOf(lastDirtyTimestamp), isFromReplicaNode);
              if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()
                      && (overriddenStatus != null)
                      && !(InstanceStatus.UNKNOWN.name().equals(overriddenStatus))
                      && isFromReplicaNode) {
                  registry.storeOverriddenStatusIfRequired(
                      app.getAppName(), id, InstanceStatus.valueOf(overriddenStatus));
              }
          } else {
              response = Response.ok().build();
          }
          return response;
  }
  
  public abstract class AbstractInstanceRegistry implements InstanceRegistry {
      @Override
    public void storeOverriddenStatusIfRequired(
        String appName, String id, InstanceStatus overriddenStatus) {
        //获取实例的状态
        InstanceStatus instanceStatus = overriddenInstanceStatusMap.get(id);
        if ((instanceStatus == null) || (!overriddenStatus.equals(instanceStatus))) {
          //缓存存储
          overriddenInstanceStatusMap.put(id, overriddenStatus);
          //获取注册存储的元数据
          InstanceInfo instanceInfo = this.getInstanceByAppAndId(appName, id, false);
          //设置实例的状态
          instanceInfo.setOverriddenStatus(overriddenStatus);
      }
    }
  }
  ```

  

- **DataCenterInfo(数据中心)**

- **Eureka-Server的注册与续期**

  ```java
  public abstract class AbstractInstanceRegistry implements InstanceRegistry {
  	
      //读写锁
      private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
      private final Lock read = readWriteLock.readLock();
      private final Lock write = readWriteLock.writeLock();
      
      //appName-instanceId-Lease<InstanceInfo>
      private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry
              = new ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>();
      //guava缓存(存储instanceId-InstanceStatus)，每一个实例的状态，每个小时过期
      protected final ConcurrentMap<String, InstanceStatus> 
          overriddenInstanceStatusMap = CacheBuilder
              .newBuilder().initialCapacity(500)
              .expireAfterAccess(1, TimeUnit.HOURS)
              .<String, InstanceStatus>build().asMap();
      
     //续约状态规则(默认FirstMatchWinsCompositeRule,
     //即先检查当前是STARTING或者DOWN,再检查可能存在的状态)
     private final InstanceStatusOverrideRule instanceStatusOverrideRule;
      
      public void register(InstanceInfo registrant, 
                       int leaseDuration, boolean isReplication) {
   	try {
          //上读锁防止重复注册
      	read.lock();
          //获取appName的续期
      	Map<String, Lease<InstanceInfo>> gMap = registry.get(registrant.getAppName());
      	//注册计数
          REGISTER.increment(isReplication);
          if (gMap == null) {
              //获取
          	final ConcurrentHashMap<String, Lease<InstanceInfo>> gNewMap 
                  = new ConcurrentHashMap<String, Lease<InstanceInfo>>();
              gMap = registry.putIfAbsent(registrant.getAppName(), gNewMap);
              if (gMap == null) {
                 gMap = gNewMap;     
              }
          }
          //获取具体instanceId的续期
         	Lease<InstanceInfo> existingLease = gMap.get(registrant.getId());  
          //如果已经存在续期
        	if (existingLease != null && (existingLease.getHolder() != null)) {
           	//传上来的续约时间
           	Long existingLastDirtyTimestamp = existingLease.getHolder()
               .getLastDirtyTimestamp();
          	 //获取上次调用时间
          	Long registrationLastDirtyTimestamp = registrant.getLastDirtyTimestamp();
           	//比上一次续约时间大
              if (existingLastDirtyTimestamp > registrationLastDirtyTimestamp) {
               	//更新租约
                  registrant = existingLease.getHolder();
               }
           } else {
              //还没有租约，说明是新注册
              synchronized (lock) {
                if (this.expectedNumberOfClientsSendingRenews > 0) {
                   //新客户端注册，增加客户端续约数量
          		this.expectedNumberOfClientsSendingRenews = 												this.expectedNumberOfClientsSendingRenews + 1;
                   //更新
                  updateRenewsPerMinThreshold();
               }
           }
                
    		}
        //新建租约
        Lease<InstanceInfo> lease = new Lease<InstanceInfo>(registrant, leaseDuration);
        if (existingLease != null) {
             //更新租约时间
            lease.setServiceUpTimestamp(existingLease.getServiceUpTimestamp());
        }
        //保存
        gMap.put(registrant.getId(), lease);
        //对统计队列上锁
        synchronized (recentRegisteredQueue) {
           //添加到队列
           recentRegisteredQueue.add(new Pair<Long, String>(
            System.currentTimeMillis(),
               registrant.getAppName() + "(" + registrant.getId() + ")"));
        }      
        //上传的状态不是UNKNOWN状态 && 状态缓存中没有  
        if (!InstanceStatus.UNKNOWN.equals(registrant.getOverriddenStatus())) 
            if (!overriddenInstanceStatusMap.containsKey(registrant.getId())) {
              //状态缓存存储一份
          	overriddenInstanceStatusMap.put(
                  registrant.getId(), registrant.getOverriddenStatus());
              }
       }
       //获取实例的状态
       InstanceStatus overriddenStatusFromMap 
            =overriddenInstanceStatusMap.get(registrant.getId());
       //若为UNKNOWN使用前一个的状态的意思
       if (overriddenStatusFromMap != null) {
           //若当前实例状态不为null,设置当前实例要覆盖的状态为此状态
           registrant.setOverriddenStatus(overriddenStatusFromMap);
       }
       //根据规则设置状态
  	 InstanceStatus overriddenInstanceStatus = 
          getOverriddenInstanceStatus(registrant, existingLease, isReplication);
       //设置状态
       registrant.setStatusWithoutDirty(overriddenInstanceStatus);
  	 if (InstanceStatus.UP.equals(registrant.getStatus())) {
            //如果服务状态是运行,将实例开始运行时间更新为当前时间
           lease.serviceUp();
       }
       //将操作设置为新增
       registrant.setActionType(ActionType.ADDED);
       //增量队列
       recentlyChangedQueue.add(new RecentlyChangedItem(lease));
       //更新上次更新时间
       registrant.setLastUpdatedTimestamp();
       //无效特定缓存
       invalidateCache(registrant.getAppName(),
           registrant.getVIPAddress(),registrant.getSecureVipAddress());
      } finally {
         //释放锁
         read.unlock();
      }
    }
      
      //计算续约频率
      //续约客户端数*(60/预期客户端续约间隔时间)*续约百分比
      protected void updateRenewsPerMinThreshold() {
   //预期客户端续约间隔时间:(eureka.server.expected-client-renewal-interval-seconds,默认30s)
   //续约百分比:(eureka.server.renewal-percent-threshold,默认0.85)
        this.numberOfRenewsPerMinThreshold = 
            (int) (this.expectedNumberOfClientsSendingRenews
                  * (60.0 / serverConfig.getExpectedClientRenewalIntervalSeconds())
                  * serverConfig.getRenewalPercentThreshold());
      }
  }
  ```

- **获取注册表**

  客户端

  ```java
  @Singleton
  public class DiscoveryClient implements EurekaClient {
      @Inject
      DiscoveryClient(ApplicationInfoManager applicationInfoManager, 
                      EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs args,
                      Provider<BackupRegistry> backupRegistryProvider,
                      EndpointRandomizer endpointRandomizer) {
         //注册获取线程池
         cacheRefreshExecutor = new ThreadPoolExecutor(
                      1, clientConfig
             .getCacheRefreshExecutorThreadPoolSize(), 0, TimeUnit.SECONDS,
                      new SynchronousQueue<Runnable>(),
                      new ThreadFactoryBuilder()
                              .setNameFormat("DiscoveryClient-CacheRefreshExecutor-%d")
                              .setDaemon(true)
                              .build()  
      }
             
      private void initScheduledTasks() {
          if (clientConfig.shouldFetchRegistry()) {
            	//registry-fetch-interval-seconds(默认30s)
              int registryFetchIntervalSeconds = clientConfig
                  .getRegistryFetchIntervalSeconds();
              int expBackOffBound = clientConfig
                  .getCacheRefreshExecutorExponentialBackOffBound();
              scheduler.schedule(
                      new TimedSupervisorTask(
                              "cacheRefresh",
                              scheduler,
                              cacheRefreshExecutor,
                              registryFetchIntervalSeconds,
                              TimeUnit.SECONDS,
                              expBackOffBound,
                              new CacheRefreshThread()
                      ),
                      registryFetchIntervalSeconds, TimeUnit.SECONDS);
          }
      }
  }
             
  class CacheRefreshThread implements Runnable {
      @VisibleForTesting
      void refreshRegistry() {
          try {
    //是否立即获取
    boolean isFetchingRemoteRegionRegistries = isFetchingRemoteRegionRegistries();
    boolean remoteRegionsModified = false;
    //配置（fetch-remote-regions-registry）动态region
    String latestRemoteRegions = clientConfig.fetchRegistryForRemoteRegions();
    if (null != latestRemoteRegions) {
       //获取当前region
       String currentRemoteRegions = remoteRegionsToFetch.get();
      if (!latestRemoteRegions.equals(currentRemoteRegions)) {
          //若region不同
       synchronized (instanceRegionChecker.getAzToRegionMapper()) {
     	 if (remoteRegionsToFetch.compareAndSet(currentRemoteRegions, latestRemoteRegions)) {
         //分割配置的region
         String[] remoteRegions = latestRemoteRegions.split(",");
         remoteRegionsRef.set(remoteRegions);
         instanceRegionChecker.getAzToRegionMapper().setRegionsToFetch(remoteRegions);
          remoteRegionsModified = true;
    	  } 
      }
     } else {
       instanceRegionChecker.getAzToRegionMapper().refreshMapping();
         }
      }
  	//拉取注册表
      boolean success = fetchRegistry(remoteRegionsModified);
      if (success) {
         registrySize = localRegionApps.get().size();
         lastSuccessfulRegistryFetchTimestamp = System.currentTimeMillis();
      }
  
     } catch (Throwable e) {
      logger.error("Cannot fetch registry from server", e);
       }
   }
  }
             
  private boolean fetchRegistry(boolean forceFullRegistryFetch) {
          Stopwatch tracer = FETCH_REGISTRY_TIMER.start();
  
          try {
              Applications applications = getApplications();
  
              if (clientConfig.shouldDisableDelta()
                      || (!Strings.isNullOrEmpty(clientConfig.getRegistryRefreshSingleVipAddress()))
                      || forceFullRegistryFetch
                      || (applications == null)
                      || (applications.getRegisteredApplications().size() == 0)
                      || (applications.getVersion() == -1)) 
              {
                	//全量拉取
                  getAndStoreFullRegistry();
              } else {
                  //增量拉取
                  getAndUpdateDelta(applications);
              }
              applications.setAppsHashCode(applications.getReconcileHashCode());
              logTotalInstances();
          } catch (Throwable e) {
              return false;
          } finally {
              if (tracer != null) {
                  tracer.stop();
              }
          }
  
          //发出缓存更新通知
          onCacheRefreshed();
  
          //更新本地数据元
          updateInstanceRemoteStatus();
  
          //拉取成功
          return true;
      }
  ```

  服务端：

  ```java
  public class ApplicationsResource {
  
      @GET
      //全量拉取
      public Response getContainers(@PathParam("version") String version,
                    @HeaderParam(HEADER_ACCEPT) String acceptHeader,
                    @HeaderParam(HEADER_ACCEPT_ENCODING) String acceptEncoding,
                    @HeaderParam(EurekaAccept.HTTP_X_EUREKA_ACCEPT) String eurekaAccept,
                    @Context UriInfo uriInfo,
                   @Nullable @QueryParam("regions") String regionsStr) {
  		//判断是否有传region
          boolean isRemoteRegionRequested = null != regionsStr && !regionsStr.isEmpty();
          String[] regions = null;
          if (!isRemoteRegionRequested) {
              EurekaMonitors.GET_ALL.increment();
          } else {
              //分割，并排序
              regions = regionsStr.toLowerCase().split(",");
              Arrays.sort(regions);
              EurekaMonitors.GET_ALL_WITH_REMOTE_REGIONS.increment();
          }
  
        	
          if (!registry.shouldAllowAccess(isRemoteRegionRequested)) {
              return Response.status(Status.FORBIDDEN).build();
          }
          CurrentRequestVersion.set(Version.toEnum(version));
          KeyType keyType = Key.KeyType.JSON;
          String returnMediaType = MediaType.APPLICATION_JSON;
          if (acceptHeader == null || !acceptHeader.contains(HEADER_JSON_VALUE)) {
              keyType = Key.KeyType.XML;
              returnMediaType = MediaType.APPLICATION_XML;
          }
  
          //缓存的key
          Key cacheKey = new Key(Key.EntityType.Application,
                  ResponseCacheImpl.ALL_APPS,
                  keyType, CurrentRequestVersion.get(), EurekaAccept.fromString(eurekaAccept), regions
          );
  
          Response response;
          if (acceptEncoding != null && acceptEncoding.contains(HEADER_GZIP_VALUE)) {
              //根据key获取内容并压缩
              response = Response.ok(responseCache.getGZIP(cacheKey))
                      .header(HEADER_CONTENT_ENCODING, HEADER_GZIP_VALUE)
                      .header(HEADER_CONTENT_TYPE, returnMediaType)
                      .build();
          } else {
              response = Response.ok(responseCache.get(cacheKey))
                      .build();
          }
          return response;
      }
      
      @Path("delta")
      @GET
      //增量拉取
      public Response getContainerDifferential(
              @PathParam("version") String version,
              @HeaderParam(HEADER_ACCEPT) String acceptHeader,
              @HeaderParam(HEADER_ACCEPT_ENCODING) String acceptEncoding,
              @HeaderParam(EurekaAccept.HTTP_X_EUREKA_ACCEPT) String eurekaAccept,
              @Context UriInfo uriInfo, @Nullable @QueryParam("regions") String regionsStr) {
  
        	//与全量拉取同样的操作，只有ResponseCacheImpl.ALL_APPS_DELTA不同
          Key cacheKey = new Key(Key.EntityType.Application,
                  ResponseCacheImpl.ALL_APPS_DELTA,
                  keyType, CurrentRequestVersion.get(), 			EurekaAccept.fromString(eurekaAccept), regions
          );
  
          if (acceptEncoding != null
                  && acceptEncoding.contains(HEADER_GZIP_VALUE)) {
              return Response.ok(responseCache.getGZIP(cacheKey))
                      .header(HEADER_CONTENT_ENCODING, HEADER_GZIP_VALUE)
                      .header(HEADER_CONTENT_TYPE, returnMediaType)
                      .build();
          } else {
              return Response.ok(responseCache.get(cacheKey))
                      .build();
          }
      }
  }
  
  
  public class ResponseCacheImpl implements ResponseCache {
      
       //第一层缓存，会定时更新
       private final ConcurrentMap<Key, Value> readOnlyCacheMap = new ConcurrentHashMap<Key, Value>();
      
      private final LoadingCache<Key, Value> readWriteCacheMap;
      
      ResponseCacheImpl(EurekaServerConfig serverConfig,
                        ServerCodecs serverCodecs, AbstractInstanceRegistry registry) {
          //设置一个(eureka.server.initial-capacity-of-response-cache,默认1000)，
          //并且过期时间(eureka.server.response-cache-auto-expiration-in-seconds,默认180s)
          //的缓存。
          this.readWriteCacheMap =CacheBuilder.newBuilder()
              .initialCapacity(serverConfig.getInitialCapacityOfResponseCache())
                          .expireAfterWrite(
              serverConfig.getResponseCacheAutoExpirationInSeconds(), TimeUnit.SECONDS)
             .removalListener(new RemovalListener<Key, Value>() {
                              @Override
                 public void onRemoval(RemovalNotification<Key, Value> notification) {
                      Key removedKey = notification.getKey();
                      if (removedKey.hasRegions()) {
                          Key cloneWithNoRegions = removedKey.cloneWithoutRegions();
                              regionSpecificKeys.remove(cloneWithNoRegions, removedKey);
                                  }
                              }
                          })
              //根据key生成value
             .build(new CacheLoader<Key, Value>() {
                    @Override
                public Value load(Key key) throws Exception {
                   if (key.hasRegions()) {
                       Key cloneWithNoRegions = key.cloneWithoutRegions();
                          regionSpecificKeys.put(cloneWithNoRegions, key);
                       }
                   Value value = generatePayload(key);
                   return value;
              }
           });
          
          //允许使用只读缓存，开启定时刷新缓存
          //(eureka.server.response-cache-update-interval-ms,默认30s)
          if (shouldUseReadOnlyResponseCache) {
              timer.schedule(getCacheUpdateTask(),
               new Date(((System.currentTimeMillis() 
                    / responseCacheUpdateIntervalMs) * responseCacheUpdateIntervalMs)
                              + responseCacheUpdateIntervalMs),
                      responseCacheUpdateIntervalMs);
          }
      }
      
      //获取注册表并压缩
      public byte[] getGZIP(Key key) {
          //shouldUseReadOnlyResponseCache：是否只可以读取缓存
          //eureka.server.use-read-only-response-cache(默认true)
          Value payload = getValue(key, shouldUseReadOnlyResponseCache);
          if (payload == null) {
              return null;
          }
          return payload.getGzipped();
      }
      
      @VisibleForTesting
      Value getValue(final Key key, boolean useReadOnlyCache) {
          Value payload = null;
          try {
              if (useReadOnlyCache) {
                  final Value currentPayload = readOnlyCacheMap.get(key);
                  if (currentPayload != null) {
                      payload = currentPayload;
                  } else {
                      //只读缓存中没有从读写缓存中获取
                      payload = readWriteCacheMap.get(key);
                      //并存入只写缓存
                      readOnlyCacheMap.put(key, payload);
                  }
              } else {
                  //若没有设置，直接从读写缓存中读取
                  payload = readWriteCacheMap.get(key);
              }
          } catch (Throwable t) {
              logger.error("Cannot get value for key : {}", key, t);
          }
          return payload;
      }
      
      
      //定时任务
      private TimerTask getCacheUpdateTask() {
          return new TimerTask() {
              @Override
              public void run() {
                  //将只读缓存中的所有内容，更新成读写缓存中的内容
                  for (Key key : readOnlyCacheMap.keySet()) {
                      try {
                          CurrentRequestVersion.set(key.getVersion());
                          Value cacheValue = readWriteCacheMap.get(key);
                          Value currentCacheValue = readOnlyCacheMap.get(key);
                          if (cacheValue != currentCacheValue) {
                              readOnlyCacheMap.put(key, cacheValue);
                          }
                      } catch (Throwable th) {
                          logger.error("Error while");
                      }
                  }
              }
          };
      }
  }
  ```

  拉取详细

  ```java
  //实例信息列表
  public class Applications {
     	//实例信息队列
      private final AbstractQueue<Application> applications;
      
  }
  
  public class Application {
      
      private String name;
      
      private final Set<InstanceInfo> instances;
     
      private final AtomicReference<List<InstanceInfo>> shuffledInstances;
      
      private final Map<String, InstanceInfo> instancesMap;
  }
  
  private Value generatePayload(Key key) {
          Stopwatch tracer = null;
          try {
              String payload;
              //判断类型
              switch (key.getEntityType()) {
                  //Application: 注册列表
                  case Application:
                      boolean isRemoteRegionRequested = key.hasRegions();
  					//全量
                      if (ALL_APPS.equals(key.getName())) {
                          if (isRemoteRegionRequested) {
                              tracer = serializeAllAppsWithRemoteRegionTimer.start();
                              payload = getPayLoad(key, registry.getApplicationsFrom
                                                   MultipleRegions(key.getRegions()));
                          } else {
                              tracer = serializeAllAppsTimer.start();
                              payload = getPayLoad(key, registry.getApplications());
                          }
                      } else if (ALL_APPS_DELTA.equals(key.getName())) {
                          //增量
                          if (isRemoteRegionRequested) {
                              tracer = serializeDeltaAppsWithRemoteRegionTimer.start();
                              versionDeltaWithRegions.incrementAndGet();
                              versionDeltaWithRegionsLegacy.incrementAndGet();
                              payload = getPayLoad(key,
                                      registry.getApplicationDeltasFromM
                                                   ultipleRegions(key.getRegions()));
                          } else {
                              tracer = serializeDeltaAppsTimer.start();
                              versionDelta.incrementAndGet();
                              versionDeltaLegacy.incrementAndGet();
                              payload = getPayLoad(
                                  key, registry.getApplicationDeltas());
                          }
                      } else {
                          tracer = serializeOneApptimer.start();
                          payload = getPayLoad(
                              key, registry.getApplication(key.getName()));
                      }
                      break;
                  case VIP:
                  case SVIP:
                      tracer = serializeViptimer.start();
                      payload = getPayLoad(key, getApplicationsForVip(key, registry));
                      break;
                  default:
                      payload = "";
                      break;
              }
              return new Value(payload);
          } finally {
              if (tracer != null) {
                  tracer.stop();
              }
          }
      }
  
  //全量获取
  public Applications getApplicationsFromMultipleRegions(String[] remoteRegions) {
  
          Applications apps = new Applications();
          apps.setVersion(1L);
      	//遍历所有续期实例
          for (Entry<String, Map<String, 
              	Lease<InstanceInfo>>> entry : registry.entrySet()) {
              Application app = null;
              if (entry.getValue() != null) {
                  //appname下不为空
                  for (Entry<String, Lease<InstanceInfo>> 
                       stringLeaseEntry : entry.getValue().entrySet()) {
                      //遍历每一个实例
                      Lease<InstanceInfo> lease = stringLeaseEntry.getValue();
                      if (app == null) {
                          app = new Application(lease.getHolder().getAppName());
                      }
                      //将实例信息添加到Applications
                      app.addInstance(decorateInstanceInfo(lease));
                  }
              }
              if (app != null) {
                  //添加
                  apps.addApplication(app);
              }
          }
          apps.setAppsHashCode(apps.getReconcileHashCode());
          return apps;
      }
  
  //增量队列，当注册或者续期成功时会添加
  private ConcurrentLinkedQueue<RecentlyChangedItem> recentlyChangedQueue 
      = new ConcurrentLinkedQueue<RecentlyChangedItem>();
  
  public Applications getApplicationDeltasFromMultipleRegions(String[] remoteRegions) {
          Applications apps = new Applications();
          apps.setVersion(responseCache.getVersionDeltaWithRegions().get());
          Map<String, Application> applicationInstancesMap 
              = new HashMap<String, Application>();
          try {
              write.lock();
              //
              Iterator<RecentlyChangedItem> iter = this.recentlyChangedQueue.iterator();
              while (iter.hasNext()) {
                  Lease<InstanceInfo> lease = iter.next().getLeaseInfo();
                  InstanceInfo instanceInfo = lease.getHolder();
                  Application app = applicationInstancesMap
                      .get(instanceInfo.getAppName());
                  if (app == null) {
                      app = new Application(instanceInfo.getAppName());
                      applicationInstancesMap.put(instanceInfo.getAppName(), app);
                      apps.addApplication(app);
                  }
                  app.addInstance(new InstanceInfo(decorateInstanceInfo(lease)));
              }
  
              Applications allApps = getApplicationsFromMultipleRegions(remoteRegions);
              apps.setAppsHashCode(allApps.getReconcileHashCode());
              return apps;
          } finally {
              write.unlock();
          }
      }
  
  //定时清理增量队列
  private TimerTask getDeltaRetentionTask() {
          return new TimerTask() {
  
              @Override
              public void run() {
                  Iterator<RecentlyChangedItem> it = recentlyChangedQueue.iterator();
                  while (it.hasNext()) {
                      if (it.next().getLastUpdateTime() <
                              System.currentTimeMillis() - serverConfig
                          .getRetentionTimeInMSInDeltaQueue()) {
                          it.remove();
                      } else {
                          break;
                      }
                  }
              }
  
          };
      }
  ```

- **server集群同步**

  ```java
  private void replicateToPeers(Action action, String appName, String id,
                                    InstanceInfo info ,
                                    InstanceStatus newStatus, boolean isReplication) {
          Stopwatch tracer = action.getTimer().start();
          try {
              if (isReplication) {
                  numberOfReplicationsLastMin.increment();
              }
     
              if (peerEurekaNodes == Collections.EMPTY_LIST || isReplication) {
                  return;
              }
  			//循环所有节点
              for (final PeerEurekaNode node : peerEurekaNodes.getPeerEurekaNodes()) {
                  if (peerEurekaNodes.isThisMyUrl(node.getServiceUrl())) {
                      continue;
                  }
                  replicateInstanceActionsToPeers(action, appName, 
                                                  id, info, newStatus, node);
              }
          } finally {
              tracer.stop();
          }
      }
  
  private void replicateInstanceActionsToPeers(Action action, String appName,
                          String id, InstanceInfo info, InstanceStatus newStatus,
                                                   PeerEurekaNode node) {
          try {
              InstanceInfo infoFromRegistry = null;
              CurrentRequestVersion.set(Version.V2);
              switch (action) {
                  case Cancel:
                      node.cancel(appName, id);
                      break;
                  case Heartbeat:
                      //心跳
                      InstanceStatus overriddenStatus = overriddenInstanceStatusMap
                          .get(id);
                      infoFromRegistry = getInstanceByAppAndId(appName, id, false);
                      node.heartbeat(appName, id, 
                                     infoFromRegistry, overriddenStatus, false);
                      break;
                  case Register:
                      //注册
                      node.register(info);
                      break;
                  case StatusUpdate:
                      //状态更新
                      infoFromRegistry = getInstanceByAppAndId(appName, id, false);
                      node.statusUpdate(appName, id, newStatus, infoFromRegistry);
                      break;
                  case DeleteStatusOverride:
                      //下线
                      infoFromRegistry = getInstanceByAppAndId(appName, id, false);
                      node.deleteStatusOverride(appName, id, infoFromRegistry);
                      break;
              }
          } catch (Throwable t) {
              logger.error("Cannot replicate information to {} for action {}");
          }
      }
  ```


### 1-2. NACOS

```
nacos的服务发现：
	1.基于raft协议
	2.变化通知采用的udp
```

#### Raft

```
Term : 选举的届数
RaftStore：日志log
RaftPeerSet： 所有节点（leader）
RaftPeer：每一个节点
```

- **Raft节点:**

  ```java
  public class RaftPeer {
  	
      public String ip;//ip地址
      
      public String voteFor;//投票
      
      public AtomicLong term = new AtomicLong(0L);//选举届数
      
      //leader到期时间（重新发起选举）
      public volatile long leaderDueMs 
          = RandomUtils.nextLong(0, GlobalExecutor.LEADER_TIMEOUT_MS);
      
      //心跳间隔时间
      public volatile long heartbeatDueMs 
          = RandomUtils.nextLong(0, GlobalExecutor.HEARTBEAT_INTERVAL_MS);
      
      public volatile State state = State.FOLLOWER;//节点状态
      
      public enum State {
          /**
           * Leader of the cluster, only one leader stands in a cluster.
           */
          LEADER,
          /**
           * Follower of the cluster, report to and copy from leader.
           */
          FOLLOWER,
          /**
           * Candidate leader to be elected.
           */
          CANDIDATE
      }
  }
  ```

- **Raft启动:**

  ```java
  @DependsOn("ProtocolManager")
  @Component
  public class RaftCore {
  	
      @PostConstruct
      public void init() throws Exception {
          //执行通知
          executor.submit(notifier);
          
          final long start = System.currentTimeMillis();
          //从log文件加载数据（raftStore）
          raftStore.loadDatums(notifier, datums);
          //设置当前的选届
          setTerm(NumberUtils.toLong(raftStore.loadMeta().getProperty("term"), 0L));
          
          //处理所有通知任务
          while (true) {
              if (notifier.tasks.size() <= 0) {
                  break;
              }
              Thread.sleep(1000L);
          }
          //初始化标识
          initialized = true;
          
          //选举 500ms
          GlobalExecutor.registerMasterElection(new MasterElection());
          //心跳 500ms
          GlobalExecutor.registerHeartbeat(new HeartBeat());
      }
      
  }
  ```

- **Raft心跳:**

  raft的leader节点每隔一段时间会给所有的follower发送心跳，若follower长时间没有收到leader的心跳

  会认为leader挂了，重新发起选举

  ```java
  public class HeartBeat implements Runnable {
  	
     @Override
      public void run() {
          try {
              //节点是否存在
              if (!peers.isReady()) {
                  return;
              }
              //获取当前节点
              RaftPeer local = peers.local();
              
              //判断是否达到心跳时间
              local.heartbeatDueMs -= GlobalExecutor.TICK_PERIOD_MS;
              if (local.heartbeatDueMs > 0) {
                  return;
              }
              //重置心跳时间
              local.resetHeartbeatDue();
              //发送心跳
              sendBeat();
          } catch (Exception e) {
              Loggers.RAFT.warn("[RAFT] error while sending beat {}", e);
          }
          
      }
      
      private void sendBeat() throws IOException{
          //获取本地节点
          RaftPeer local = peers.local();
          //非单机节点并且是Leader节点才能发送心跳
          if (ApplicationUtils.getStandaloneMode() 
              || local.state != RaftPeer.State.LEADER) {
                  return;
          }
          //重设leader超时时间
          local.resetLeaderDue();
          
          //创建Node对象 (peer节点为本地的节点对象)
          ObjectNode packet = JacksonUtils.createEmptyJsonNode();
          packet.replace("peer", JacksonUtils.transferToJsonNode(local));
          ArrayNode array = JacksonUtils.createEmptyArrayNode();
          packet.replace("datums", array);
          
          //广播（构造参数并压缩）
          Map<String, String> params = new HashMap<String, String>(1);
          params.put("beat", JacksonUtils.toJson(packet));
          String content = JacksonUtils.toJson(params);
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          GZIPOutputStream gzip = new GZIPOutputStream(out);
          gzip.write(content.getBytes(StandardCharsets.UTF_8));
          gzip.close();
              
          byte[] compressedBytes = out.toByteArray();
          String compressedContent 
              = new String(compressedBytes, StandardCharsets.UTF_8);
          
          //遍历所有节点除了当前节点
          for (final String server : peers.allServersWithoutMySelf()) {
              try {
                  //（/raft/beat）
                  final String url = buildUrl(server, API_BEAT);
                  //发送请求
                  HttpClient.asyncHttpPostLarge(
                     url, null, compressedBytes, new AsyncCompletionHandler<Integer>() {
                        @Override
                        public Integer onCompleted(Response response) throws Exception {
                            if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
                                //响应不成功，记录心跳失败	
                                MetricsMonitor.getLeaderSendBeatFailedException()
                                      .increment();
                                  return 1;
                              }
                              //更新foller节点信息
                             peers.update(JacksonUtils
                                          .toObj(response.getResponseBody()
                                                 , RaftPeer.class));
                              
                              return 0;
                          }
                          
                          @Override
                          public void onThrowable(Throwable t) {
                              MetricsMonitor.getLeaderSendBeatFailedException()
                                  .increment();
                          }
                      });
              }catch (Exception e) {
                  
              }
          }
      }
  }
  ```

  客户端接收

  ```java
  @RestController
  @RequestMapping(UtilsAndCommons.NACOS_NAMING_CONTEXT + "/instance")
  public class InstanceController {
      
      @PostMapping("/beat")
      public JsonNode beat(HttpServletRequest request
                           , HttpServletResponse response) throws Exception {
          
          //解压
          String entity 
              = new String(IoUtils.tryDecompress(request.getInputStream())
                           , StandardCharsets.UTF_8);
          
          String value = URLDecoder.decode(entity, "UTF-8");
          value = URLDecoder.decode(value, "UTF-8");
          
          JsonNode json = JacksonUtils.toObj(value);
          
          //处理心跳
          RaftPeer peer 
              = raftCore.receivedBeat(JacksonUtils.toObj(json.get("beat").asText()));
          
          return JacksonUtils.transferToJsonNode(peer);
      }
      
      //处理心跳
      public RaftPeer receivedBeat(JsonNode beat) throws Exception {
          final RaftPeer local = peers.local();
          final RaftPeer remote = new RaftPeer();
          //设置leader的节点数据
          JsonNode peer = beat.get("peer");
          remote.ip = peer.get("ip").asText();
          remote.state = RaftPeer.State.valueOf(peer.get("state").asText());
          remote.term.set(peer.get("term").asLong());
          remote.heartbeatDueMs = peer.get("heartbeatDueMs").asLong();
          remote.leaderDueMs = peer.get("leaderDueMs").asLong();
          remote.voteFor = peer.get("voteFor").asText();
          
          if (remote.state != RaftPeer.State.LEADER) {
              //非leader节点的心跳，抛异常
              throw new IllegalArgumentException();
          }
          if (local.term.get() > remote.term.get()) {
              //当前节点 选届大于 leader节点的选届，抛异常
              throw new IllegalArgumentException();
          }
          if (local.state != RaftPeer.State.FOLLOWER) {
              //当前节点不是FOLLOWER,改为FOLLOWER节点
              local.state = RaftPeer.State.FOLLOWER;
              local.voteFor = remote.ip;
          }
          
          //更新时间
          final JsonNode beatDatums = beat.get("datums");
          local.resetLeaderDue();
          local.resetHeartbeatDue();
          
          //将心跳请求的节点当前leader，并更新每个节点的状态
          peers.makeLeader(remote);
      }
  }
  ```

-  **发布新记录:**

  ```java
  public class RaftCore {
  
       public void signalPublish(String key, Record value) throws Exception {
           
           if (!isLeader()) {
              //若当前节点不是leader节点
              ObjectNode params = JacksonUtils.createEmptyJsonNode();
              params.put("key", key);
              params.replace("value", JacksonUtils.transferToJsonNode(value));
              Map<String, String> parameters = new HashMap<>(1);
              parameters.put("key", key);
              
              //转发到leader节点
              final RaftPeer leader = getLeader();
              raftProxy
                  .proxyPostLarge(leader.ip, API_PUB, params.toString(), parameters);
              return;
          }
           
          try {
              OPERATE_LOCK.lock();
              final long start = System.currentTimeMillis();
              //Naming Service数据
              final Datum datum = new Datum();
              datum.key = key;
              datum.value = value;
              if (getDatum(key) == null) {
                  datum.timestamp.set(1L);
              } else {
                  datum.timestamp.set(getDatum(key).timestamp.incrementAndGet());
              }
              
              ObjectNode json = JacksonUtils.createEmptyJsonNode();
              json.replace("datum", JacksonUtils.transferToJsonNode(datum));
              json.replace("source", JacksonUtils.transferToJsonNode(peers.local()));
              
              //发布消息
              onPublish(datum, peers.local());
              
              final String content = json.toString();
              //给所有的follower节点异步同步
              final CountDownLatch latch = new CountDownLatch(peers.majorityCount());
              for (final String server : peers.allServersIncludeMyself()) {
                  if (isLeader(server)) {
                      latch.countDown();
                      continue;
                  }
                  final String url = buildUrl(server, API_ON_PUB);
                  HttpClient.asyncHttpPostLarge(url, 
                                                Arrays.asList("key=" + key), content,
                          new AsyncCompletionHandler<Integer>() {
                              @Override
                              public Integer onCompleted(Response response){
                                 if (response.getStatusCode()
                                    != HttpURLConnection.HTTP_OK) {
                   					return 1;
                                  }
                                  latch.countDown();
                                  return 0;
                              }
                              
                              @Override
                              public STATE onContentWriteCompleted() {
                                  return STATE.CONTINUE;
                              }
                          });
              }
          }finally {
              OPERATE_LOCK.unlock();
          }
       }
      
      public void onPublish(Datum datum, RaftPeer source) throws Exception {
          RaftPeer local = peers.local();
          if (datum.value == null) {
              throw new IllegalStateException("received empty datum");
          }
          if (!peers.isLeader(source.ip)) {
              //leader节点不对
              throw new IllegalStateException();
          }
          if (source.term.get() < local.term.get()) {
              //选届不能小于leader的选届
              throw new IllegalStateException()
          }
          local.resetLeaderDue();
          //写日志
          if (KeyBuilder.matchPersistentKey(datum.key)) {
              raftStore.write(datum);
          }
          datums.put(datum.key, datum);
          if (isLeader()) {
              //写入后选届+100
              local.term.addAndGet(PUBLISH_TERM_INCREASE_COUNT);
          } else {
              //follower节点
              if (local.term.get() + PUBLISH_TERM_INCREASE_COUNT > source.term.get()) {
      			//设置leader的选届
                  getLeader().term.set(source.term.get());
                  local.term.set(getLeader().term.get());
              } else {
                  local.term.addAndGet(PUBLISH_TERM_INCREASE_COUNT);
              }
          }
      }
  }
  ```

- **选举：**

  1. 初始启动时所有节点会以follower角色启动
  2. 在网络启动后，节点等待指定时长（TICK_PERIOD_MS，leader的心跳会重置，随机超时机制）
  3. 发起预选举（发送一个自身任期+1的请求），预选举成功才会发起选举（为了防止出现网络隔离时，非leader节点选届过大，当分区正常，被选为leader，正常日志被覆盖）
  4. 发起选举，节点把自己的角色转为Candidate，（自身的任期(term)+1 , 并为自己投一票，）
  5. 给其他节点发送选举信息
  6. 各个节点收到vote信息（ 一个选届只能选举一次，先来先得，会更新当前的选届为候选者选届 ）
  7. 选举结束，获得超过半数选票的节点成为leader
  8. beleader(),发送一个广播通知其他follower节点（其他节点也可以心跳时处理,将设置新的leader，更新其他节点信息）

  ```java
  public class MasterElection implements Runnable {
  	
      @Override
      public void run() {
              try {
                  
                  if (!peers.isReady()) {
                      return;
                  }
                  //是否超过一个选届时间
                  RaftPeer local = peers.local();
                  local.leaderDueMs -= GlobalExecutor.TICK_PERIOD_MS;
                  
                  if (local.leaderDueMs > 0) {
                      return;
                  }
                  
                  //重置超时时间
                  local.resetLeaderDue();
                  local.resetHeartbeatDue();
                  //发送选届
                  sendVote();
              } catch (Exception e) {
                  Loggers.RAFT.warn("[RAFT] error while master election {}", e);
              }
              
          }
      
      private void sendVote() {
              
              RaftPeer local = peers.get(NetUtils.localServer());
              
              peers.reset();
              
          	//当前节点，选届+1，为自己投一票，节点状态为CANDIDATE(候选者)
              local.term.incrementAndGet();
              local.voteFor = local.ip;
              local.state = RaftPeer.State.CANDIDATE;
              
          	//给其他节点发送选举信息
              Map<String, String> params = new HashMap<>(1);
              params.put("vote", JacksonUtils.toJson(local));
              for (final String server : peers.allServersWithoutMySelf()) {
                  final String url = buildUrl(server, API_VOTE);
                  try {
                      HttpClient.asyncHttpPost(
                          url, null, params, new AsyncCompletionHandler<Integer>() {
                          @Override
                          public Integer onCompleted(Response response){
                            if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
                                  return 1;
                              }
                              
                              RaftPeer peer 
                                  = JacksonUtils.toObj(response.getResponseBody()
                                                       , RaftPeer.class);
                              
                              //判断选举结果
                              peers.decideLeader(peer);
                              
                              return 0;
                          }
                      });
                  } catch (Exception e) {
                      Loggers.RAFT.warn();
                  }
              }
          }
      
      //选举leader，超过半数
      public RaftPeer decideLeader(RaftPeer candidate) {
          peers.put(candidate.ip, candidate);
          
          SortedBag ips = new TreeBag();
          //最大选票数
          int maxApproveCount = 0;
          //最大选票节点
          String maxApprovePeer = null;
          for (RaftPeer peer : peers.values()) {
              //还没有投票的节点
              if (StringUtils.isEmpty(peer.voteFor)) {
                  continue;
              }
              //添加被选举的节点
              ips.add(peer.voteFor);
              //判断被选举节点的数量是否大于最大数
              if (ips.getCount(peer.voteFor) > maxApproveCount) {
                  maxApproveCount = ips.getCount(peer.voteFor);
                  maxApprovePeer = peer.voteFor;
              }
          }
          
          //最大选举数要大于半数
          if (maxApproveCount >= majorityCount()) {
              //选举成功，节点状态修改为leader
              RaftPeer peer = peers.get(maxApprovePeer);
              peer.state = RaftPeer.State.LEADER;
              
              if (!Objects.equals(leader, peer)) {
                  leader = peer;
                  //发送一个leader选举结束事件
                  ApplicationUtils.publishEvent(
                      new LeaderElectFinishedEvent(this, leader, local()));
              }
          }
          
          return leader;
      }
  }
  ```

  选举过程

  ```java
  public class RaftController {
  	
      @PostMapping("/vote")
      public JsonNode vote(HttpServletRequest request, HttpServletResponse response) throws Exception {
          
          //收到投票请求
          RaftPeer peer = raftCore
              .receivedVote(JacksonUtils.toObj(WebUtils.required(request, "vote")
                                               , RaftPeer.class));
          
          return JacksonUtils.transferToJsonNode(peer);
      }
      
  }
  
  public class RaftCore {
      
      public synchronized RaftPeer receivedVote(RaftPeer remote) {
          
          if (!peers.contains(remote)) {
              //不包含当前选举的候选者节点
              throw new IllegalStateException("can not find peer: " + remote.ip);
          }
          
          RaftPeer local = peers.get(NetUtils.localServer());
          
          if (remote.term.get() <= local.term.get()) {
        		//当前的选届大于等于候选者的选届
              if (StringUtils.isEmpty(local.voteFor)) {
                  //没有选举，选举自己（否则为默认选举）
                  local.voteFor = local.ip;
              }
              return local;
          }
          //选届比较大
          
          //重置选举时间
          local.resetLeaderDue();
          
          //当前节点成为FOLLOWER，并为候选者投票，选届也更新为候选者的term
          local.state = RaftPeer.State.FOLLOWER;
          local.voteFor = remote.ip;
          local.term.set(remote.term.get());
         
          
          return local;
      }
      
  }
  ```

- **日志的复制：**

  1. 

- 

### **服务注册与发现**（naming模块）

- **实例参数**

  ```java
  @JsonInclude(Include.NON_NULL)
  public class Instance {
  	
      private String instanceId;//实例唯一id
      
      private String ip;//实例ip地址
      
      private int port;//实例端口号
      
      private double weight = 1.0D;//权重
      
      private boolean healthy = true;//健康状态
      
      private boolean enabled = true;//实例是否能接受请求
      
      private boolean ephemeral = true;//实例是否持久化(true CP模式，false AP模式)
      
      private String clusterName; //集群名称
      
      private String serviceName;//实例服务名
      
      private Map<String, String> metadata = new HashMap<String, String>();//自定义元数据
  }
  ```

- **服务注册:**

  客户端启动注册服务

  ```java
  public interface NamingService {
  
      //注册一个实例给服务
      void registerInstance(String serviceName, String groupName, String ip, int port)   
      void registerInstance(String serviceName, String ip, int port, String clusterName)
      void registerInstance(String serviceName, String groupName, String ip
                            , int port, String clusterName)
      void registerInstance(String serviceName, Instance instance)
      void registerInstance(String serviceName, String groupName, Instance instance)    
      
      //注销一个实例在服务
      void deregisterInstance(String serviceName, String ip, int port)
      void deregisterInstance(String serviceName, String groupName, String ip, int port)
      void deregisterInstance(String serviceName, String ip,int port,String clusterName)     void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName)
      void deregisterInstance(String serviceName, Instance instance) 
      void deregisterInstance(String serviceName, String groupName, Instance instance)
          
      //获取一个服务的所有实例
      List<Instance> getAllInstances(String serviceName)
      List<Instance> getAllInstances(String serviceName, String groupName)
      List<Instance> getAllInstances(String serviceName, boolean subscribe)
      List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe) 
      List<Instance> getAllInstances(String serviceName, List<String> clusters)
          
  }
  
  public class NacosNamingService implements NamingService {
      
      @Override
      public void registerInstance(String serviceName, String groupName,
                                   Instance instance) throws NacosException {
          //获取groupName
          String groupedServiceName 
              = NamingUtils.getGroupedName(serviceName, groupName);
          //判断是AP还是CP
          if (instance.isEphemeral()) {
              //创建一个节点信息
              BeatInfo beatInfo 
                  = beatReactor.buildBeatInfo(groupedServiceName, instance);
              beatReactor.addBeatInfo(groupedServiceName, beatInfo);
          }
          //注册服务
          serverProxy.registerService(groupedServiceName, groupName, instance);
      }
  }
  ```

  服务端接受注册请求

  ```java
  @Component
  public class ServiceManager implements RecordListener<Service> {
  	
      public void registerInstance(String namespaceId, String serviceName,
                                   Instance instance) throws NacosException {
          
          createEmptyService(namespaceId, serviceName, instance.isEphemeral());
          //获取服务(Map(namespace, Map(group::serviceName, Service)).)
          Service service = getService(namespaceId, serviceName);
          
          if (service == null) {
              throw new NacosException(NacosException.INVALID_PARAM);
          }
          //注册	
          addInstance(namespaceId, serviceName, instance.isEphemeral(), instance);
      }
      
      //添加注册
      public void addInstance(String namespaceId, String serviceName,
                              boolean ephemeral, Instance... ips)
              throws NacosException {
          
          String key 
              = KeyBuilder.buildInstanceListKey(namespaceId, serviceName, ephemeral);
          
          Service service = getService(namespaceId, serviceName);
          
          synchronized (service) {
              //获取该服务的所有实例
              List<Instance> instanceList = addIpAddresses(service, ephemeral, ips);
              
              Instances instances = new Instances();
              instances.setInstanceList(instanceList);
              
              consistencyService.put(key, instances);
          }
      }
  }
  
  public class RaftConsistencyServiceImpl implements PersistentConsistencyService {
      
      @Override
      public void put(String key, Record value) throws NacosException {
          try {
              //触发发布服务信号
              raftCore.signalPublish(key, value);
          } catch (Exception e) {
              throw new NacosException(NacosException.SERVER_ERROR);
          }
      }
      
  }
  ```

- **客户端心跳:**

  ```java
  public class BeatReactor implements Closeable {
  
      class BeatTask implements Runnable {
          
          @Override
          public void run() {
              if (beatInfo.isStopped()) {
                  return;
              }
              long nextTime = beatInfo.getPeriod();
              try {
                  //发送心跳请求
                  JsonNode result 
                      = serverProxy
                      .sendBeat(beatInfo, BeatReactor.this.lightBeatEnabled);
                 //返回的响应的结果
                 int code = result.get(CommonParams.CODE).asInt();
                 
                  if (code == NamingResponseCode.RESOURCE_NOT_FOUND) {
                      //资源未发现，注册服务
                       serverProxy.registerService(beatInfo.getServiceName(),
                                  NamingUtils.getGroupName(beatInfo.getServiceName())
                                                   , instance);
                  }
              }
          }
          
      }
      
  }
  
  @CanDistro
  @PutMapping("/beat")
  @Secured(parser = NamingResourceParser.class, action = ActionTypes.WRITE)
  public ObjectNode beat(HttpServletRequest request) throws Exception {
      
      //根据namespace和serviceName获取service,当前ip的元信息
      Instance instance 
          = serviceManager.getInstance(namespaceId, serviceName, clusterName, ip, port);
      if (instance == null) {
           //如果没有注册一个
           serviceManager.registerInstance(namespaceId, serviceName, instance);
      }
      Service service = serviceManager.getService(namespaceId, serviceName);
      //处理心跳
      service.processClientBeat(clientBeat);
  }
  
  public void processClientBeat(final RsInfo rsInfo) {
      ClientBeatProcessor clientBeatProcessor = new ClientBeatProcessor();
      clientBeatProcessor.setService(this);
      clientBeatProcessor.setRsInfo(rsInfo);
      //立即执行
      HealthCheckReactor.scheduleNow(clientBeatProcessor);
  }
  
  public class ClientBeatProcessor implements Runnable {
      @Override
      public void run() {
          Service service = this.service;
   
          String ip = rsInfo.getIp();
          String clusterName = rsInfo.getCluster();
          int port = rsInfo.getPort();
          Cluster cluster = service.getClusterMap().get(clusterName);
          List<Instance> instances = cluster.allIPs(true);
          
          for (Instance instance : instances) {
              if (instance.getIp().equals(ip) && instance.getPort() == port) {
                 	 //更新上次心跳时间
                  instance.setLastBeat(System.currentTimeMillis());
                  if (!instance.isMarked()) {
                      if (!instance.isHealthy()) {
                          instance.setHealthy(true);
                          getPushService().serviceChanged(service);
                      }
                  }
              }
          }
      }
  }
  ```

- **实例信息拉取**

  1. 如果有订阅（没有列表的时候会立即去获取，已有列表会wait 5s，然后由一个延迟1s的线程池去获取）
  2. 没有订阅，直接请求获取

  ```java
  public class NacosNamingService implements NamingService {
      
      @Override
      public List<Instance> getAllInstances(String serviceName,
                                            String groupName, List<String> clusters,
              boolean subscribe) throws NacosException {
          
          ServiceInfo serviceInfo;
          //是否订阅
          if (subscribe) {
              // 从缓存或注册中心获取服务信息
              serviceInfo 
                  = hostReactor.getServiceInfo(
                  NamingUtils.getGroupedName(serviceName, groupName),
                      StringUtils.join(clusters, ","));
          } else {
              //直接从注册中心获取
              serviceInfo 
                  = hostReactor
                      .getServiceInfoDirectlyFromServer(
                  NamingUtils.getGroupedName(serviceName, groupName),
                              StringUtils.join(clusters, ","));
          }
          List<Instance> list;
          if (serviceInfo == null 
              || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
              return new ArrayList<Instance>();
          }
          return list;
      }
      
  }
  ```

- **推送服务:**

  ```java
  public class PushReceiver implements Runnable, Closeable {
  
      @Override
      public void run() {
          while (!closed) {
              try {
                  
                	//开启UDP
                  byte[] buffer = new byte[UDP_MSS];
                  DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                  //接受udp消息
                  udpSocket.receive(packet);
                  //解压数据并序列化
                  String json 
                      = new String(IoUtils
                                   .tryDecompress(packet.getData()), UTF_8).trim();
          
                  
                  PushPacket pushPacket = JacksonUtils.toObj(json, PushPacket.class);
                  String ack;
                  if ("dom".equals(pushPacket.type) 
                      || "service".equals(pushPacket.type)) {
                      //处理数据同步到其他节点（保存service信息）
                      hostReactor.processServiceJson(pushPacket.data);
                  } else if ("dump".equals(pushPacket.type)) {
                      // dump数据
                  } else {
                      //仅仅ack响应
                  }
                  //udp发送消息ack
                  udpSocket.send(new DatagramPacket(ack.getBytes(UTF_8),
                                                    ack.getBytes(UTF_8).length,
                          						  packet.getSocketAddress()));
              } catch (Exception e) {
                  NAMING_LOGGER.error("[NA] error while receiving push data", e);
              }
          }
      }
      
  }
  ```

## 2.鉴权中心

### 2-1.outh2

- **OAuth2的协议握手流程**

  - `client`: 第三方应用
  - `resource owner`: 拥有被访问资源的用户
  - `Authorization server`: 认证服务器，用来进行用户认证并颁发token
  - `Resource server`：资源服务器，拥有被访问资源的服务器，需要通过token来确定是否有权限访问

  ```mermaid
  sequenceDiagram
  	Client->>resource owner:Authorization Request
  	resource owner->>Client:Authorization Grant
  	Client->>Authorization server: Authorization Grant
  	Authorization server->>Client:Access Token
  	Client->>Resource server: Access Token
  	Resource server->>Client: Protected Resource
  ```

- **客户端属性配置**

  - `clientId`: 第三方账号，唯一id

  - **`clientSecret`**：安全凭据

  - **`scope`**：指定客户端的权限访问（read，write，trust）

  - **`resourceIds`**：客户端可以访问的资源

  - **`authorizedGrantTypes`**：客户端可以使用的授权类型

    浏览器：authorization_code,refresh_token

    移动端：password,refresh_token

  - **`registeredRedirectUris`**：客户端重定向uri

  - **`autoApproveScopes`**：用户是否自动Approval (true/false/read/write)

  - **`accessTokenValiditySeconds`**: 客户端access_token的有效时间（默认12h）

  - **`refreshTokenValiditySeconds`**：客户端refresh_token的有效时间（默认30天）

  - **`additionalInformation`**： 预留字段

- **获取令牌的类型**

  ```
  authorizedGrantTypes:
  
  Authorization code（授权码模式）:根据auth code获取token
  Resource Owner Password Credentials（密码模式）
  Implicit Grant（隐式模式）：浏览器使用,比授权码模式少了code的环节，回调url携带token
  Client Credentials（客户端模式）: 根据client的id和密钥获取token
  ```

- **TokenStore**

  ```
Outh2提供的令牌存储策略:
  InMemoryTokenStore:内存存储
  JdbcTokenStore：JDBC
  JwkTokenStore: JWK
  JwtTokenStore: JWT
  RedisTokenStore: Redis
  ```
  
- **createAccessToken(创建令牌)**

  对于一个client和username

  1.如果存在token,则更新用户信息，复用原来的accessToken
  
  2.如果存在token,刚好过期,则重新生成token，复用refresh token，所有时间重新计算
  
  3.如果不存在token,则新生成token/refresh token , 时间也是新的;
  
  ```java
  public class DefaultTokenServices 
      implements AuthorizationServerTokenServices, ResourceServerTokenServices,
  		ConsumerTokenServices, InitializingBean {
       
       @Transactional
  	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
  		//根据TokenStore获取令牌
          //(以redis为例，用username/client/scope MD5做key)
  		OAuth2AccessToken existingAccessToken 
              = tokenStore.getAccessToken(authentication);
  		OAuth2RefreshToken refreshToken = null;
  		if (existingAccessToken != null) {
              //判断令牌是否过期
  			if (existingAccessToken.isExpired()) {
                  //令牌过期，删除令牌
  				if (existingAccessToken.getRefreshToken() != null) {
  					refreshToken = existingAccessToken.getRefreshToken();
  					tokenStore.removeRefreshToken(refreshToken);
  				}
  				tokenStore.removeAccessToken(existingAccessToken);
  			}
  			else {
  				//令牌正常，更新用户信息
  				tokenStore.storeAccessToken(existingAccessToken, authentication);
  				return existingAccessToken;
  			}
  		}
  
  		//刷新令牌不存在时创建一个刷新令牌
  		if (refreshToken == null) {
  			refreshToken = createRefreshToken(authentication);
  		}else if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
              //本身令牌过期,需要重新发放
  			ExpiringOAuth2RefreshToken expiring 
                  = (ExpiringOAuth2RefreshToken) refreshToken;
  			if (System.currentTimeMillis() > expiring.getExpiration().getTime()) {
  				refreshToken = createRefreshToken(authentication);
  			}
  		}
  		//创建accessToken
  		OAuth2AccessToken accessToken
              = createAccessToken(authentication, refreshToken);
          //存储访问令牌
  		tokenStore.storeAccessToken(accessToken, authentication);
  		//刷新令牌
  		refreshToken = accessToken.getRefreshToken();
  		if (refreshToken != null) {
              //存储刷新令牌
  			tokenStore.storeRefreshToken(refreshToken, authentication);
  		}
          //返回访问令牌
  		return accessToken;
  	}
              
  }
  ```
  
- **refreshAccessToken(刷新令牌)**

  根据RefreshToken刷新AccessToken

  1.如果不存在或者过期refresh token,抛出异常/客户端返回403
  
  2.如果存在refresh token, 删除所有旧的相关token, 生成新的token(新时间)
  
  3.判断是否复用refresh token, 复用则不重新生成refresh token; 不复用, 则新生成refresh_token, 时间也刷新
  
  ```java
  //是否拒绝刷新RefreshToken
  private boolean reuseRefreshToken = true;
  public OAuth2AccessToken refreshAccessToken(String refreshTokenValue,
                                              TokenRequest tokenRequest)
  			throws AuthenticationException {
  		
      	//判断是否支持刷新令牌
  		if (!supportRefreshToken) {
  			throw new InvalidGrantException("Invalid refresh token: ");
  		}
  		//读取refreshToken
  		OAuth2RefreshToken refreshToken 
              = tokenStore.readRefreshToken(refreshTokenValue);
      	//refreshToken不存在抛异常
  		if (refreshToken == null) {
  			throw new InvalidGrantException("Invalid refresh token: ");
  		}
  		//获取原来认证信息
  		OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
  		if (this.authenticationManager != null && !authentication.isClientOnly())
              //重新认证身份
  			Authentication user = new PreAuthenticatedAuthenticationToken(
              authentication.getUserAuthentication(), 
              "", authentication.getAuthorities());
  			user = authenticationManager.authenticate(user);
  			Object details = authentication.getDetails();
  			authentication = 
                  new OAuth2Authentication(authentication.getOAuth2Request(), user);
  			authentication.setDetails(details);
  		}
  		//判断cientId
  		String clientId = authentication.getOAuth2Request().getClientId();
  		if (clientId == null || !clientId.equals(tokenRequest.getClientId())) {
  			throw new InvalidGrantException("Wrong client for this refresh token: ");
  		}
  		//清除refreshToken相关的AccessToken
  		tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);
  		
  		if (isExpired(refreshToken)) {
              //若refreshToken已过期,删除refreshToken
  			tokenStore.removeRefreshToken(refreshToken);
  			throw new InvalidTokenException("Invalid refresh token (expired): ");
  		}
  
  		//创建一个刷新认证
  		authentication = createRefreshedAuthentication(authentication, tokenRequest);
  
  		if (!reuseRefreshToken) {
              //刷新RefreshToken,删除原来的RefreshToken，生成新的RefreshToken
  			tokenStore.removeRefreshToken(refreshToken);
  			refreshToken = createRefreshToken(authentication);
  		}
  
  		//生成新的accessToken
  		OAuth2AccessToken accessToken = 
              createAccessToken(authentication, refreshToken);
  		//存储新的accessToken
  		tokenStore.storeAccessToken(accessToken, authentication);
  		if (!reuseRefreshToken) {
              //更新RefreshToken
  			tokenStore.storeRefreshToken(
                  accessToken.getRefreshToken(), authentication);
  		}
  		return accessToken;
  	}
  ```
  
-  **ResourceServerTokenServices**

  ```
ResourceServerTokenServices:
  	DefaultTokenServices:使用随机UUID值令牌访问令牌服务，并刷新标记值
  	RemoteTokenServices:查询/ check_token端点，以获得访问令牌的内容。
  	UserInfoTokenServices: 使用用户信息REST服务。
  ```
  
- Endpoint**

  获取accessToken

  ```java
  @FrameworkEndpoint
  public class TokenEndpoint extends AbstractEndpoint {
  	
      @RequestMapping(value = "/oauth/token", method=RequestMethod.POST)
  	public ResponseEntity<OAuth2AccessToken> postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) 
          throws HttpRequestMethodNotSupportedException {
  		
          //principal只能是Authentication
  		if (!(principal instanceof Authentication)) {
  			throw new InsufficientAuthenticationException();
  		}
  
          //获取clientId
  		String clientId = getClientId(principal);
          //根据clientId获取ClientDetails
  		ClientDetails authenticatedClient 
              = getClientDetailsService().loadClientByClientId(clientId);
  		
          //创建一个新的TokenRequest
  		TokenRequest tokenRequest = getOAuth2RequestFactory()
              .createTokenRequest(parameters, authenticatedClient);
  
  		if (clientId != null && !clientId.equals("")) {
              //判断clientid是否一致
  			if (!clientId.equals(tokenRequest.getClientId())) {
  				throw new InvalidClientException();
  			}
  		}
  		if (authenticatedClient != null) {
              //校验客户端的作用域
  			oAuth2RequestValidator.validateScope(tokenRequest, authenticatedClient);
  		}
          //grantType不能为空
  		if (!StringUtils.hasText(tokenRequest.getGrantType())) {
  			throw new InvalidRequestException("Missing grant type");
  		}
          //implicit隐式不能访问
  		if (tokenRequest.getGrantType().equals("implicit")) {
  			throw new InvalidGrantException("Implicit grant type not");
  		}
  
  		if (isAuthCodeRequest(parameters)) {
              //若是Authorization code类型，scope不能为空
  			if (!tokenRequest.getScope().isEmpty()) {
  				tokenRequest.setScope(Collections.<String> emptySet());
  			}
  		}
  
  		if (isRefreshTokenRequest(parameters)) {
  			tokenRequest.setScope(OAuth2Utils
                			.parseParameterList(parameters.get(OAuth2Utils.SCOPE)));
  		}
  
          //获取认证
  		OAuth2AccessToken token = getTokenGranter()
              .grant(tokenRequest.getGrantType(), tokenRequest);
  		if (token == null) {
  			throw new UnsupportedGrantTypeException("Unsupported grant type: ");
  		}
  		//返回认证信息
  		return getResponse(token);
  
  	}
      
  }
  ```

  获取授权码，并重定向到指定uri:

  1. 根据clientId获取ClientDetails的信息
  2. 重定向地址解析，并在请求头中设置重定向地址
  3. 检查当前用户clientid是否认证授权。
  4. 验证完成自动授权，若是code(授权码模式)跳转到重定向页面，若是token(隐性模式)回调url中的地址。

  ```java
  public class AuthorizationEndpoint extends AbstractEndpoint {
  	
      @RequestMapping(value = "/oauth/authorize", method = RequestMethod.POST, params = OAuth2Utils.USER_OAUTH_APPROVAL)
  	public View approveOrDeny(@RequestParam Map<String, String> approvalParameters, 							  Map<String, ?> model,
  							  SessionStatus sessionStatus,
                                Principal principal) {
  
  		if (!(principal instanceof Authentication)) {
  			sessionStatus.setComplete();
  			throw new InsufficientAuthenticationException(");
  		}
  		
          //获取AuthorizationRequest
  		AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.get(AUTHORIZATION_REQUEST_ATTR_NAME);
  
  		if (authorizationRequest == null) {
  			sessionStatus.setComplete();
  			throw new InvalidRequestException("");
  		}
  
  		//检查以确保在用户批准步骤中未修改授权请求
  		Map<String, Object> originalAuthorizationRequest = (Map<String, Object>) model.get(ORIGINAL_AUTHORIZATION_REQUEST_ATTR_NAME);
  		if (isAuthorizationRequestModified(authorizationRequest, originalAuthorizationRequest)) {
  			throw new InvalidRequestException("Changes were detected");
  		}
  
  		try {
              //解析请求响应类型的初始化（由OAuth2RequestFactory）与最初请求响应类型
  			Set<String> responseTypes = authorizationRequest.getResponseTypes();
  
  			authorizationRequest.setApprovalParameters(approvalParameters);
              //更新Approval
  			authorizationRequest = userApprovalHandler
                  .updateAfterApproval(authorizationRequest,
  					(Authentication) principal);
  
  			boolean approved = userApprovalHandler
                  .isApproved(authorizationRequest, (Authentication) principal);
  			authorizationRequest.setApproved(approved);
  			
              //没有重定向地址
  			if (authorizationRequest.getRedirectUri() == null) {
  				sessionStatus.setComplete();
  				throw new InvalidRequestException("Canno");
  			}
  
              //是否批准授权
  			if (!authorizationRequest.isApproved()) {
                  //没批准，重定向到授权页面
  				return new RedirectView(getUnsuccessfulRedirect(authorizationRequest,
  						new UserDeniedAuthorizationException("User denied access"), responseTypes.contains("token")),
  						false, true, false);
  			}
  
              //隐式跳转到uri上的地址
  			if (responseTypes.contains("token")) {
  				return getImplicitGrantResponse(authorizationRequest).getView();
  			}
  			//AuthorizationCode
  			return getAuthorizationCodeResponse(authorizationRequest, 
                                                  (Authentication) principal);
  		}
  		finally {
              //更新session状态
  			sessionStatus.setComplete();
  		}
  
  	}
  }
  ```

  检查access_token

  ```java
  @FrameworkEndpoint
  public class CheckTokenEndpoint {
  
      @RequestMapping(value = "/oauth/check_token")
  	@ResponseBody
  	public Map<String, ?> checkToken(@RequestParam("token") String value) {
  
          //根据token获取OAuth2AccessToken
  		OAuth2AccessToken token = resourceServerTokenServices.readAccessToken(value);
  		if (token == null) {
  			throw new InvalidTokenException("Token was not recognised");
  		}
  
          //token过期
  		if (token.isExpired()) {
  			throw new InvalidTokenException("Token has expired");
  		}
  
          //根据token获取OAuth2Authentication
  		OAuth2Authentication authentication = resourceServerTokenServices
              .loadAuthentication(token.getValue());
  
          //将token转化为对象
  		Map<String, Object> response = (Map<String, Object>)accessTokenConverter
              .convertAccessToken(token, authentication);
  
  		response.put("active", true);
  
  		return response;
  	}
      
  }
  ```

-  **ClientDetailsServiceConfigurer**

  ```java
  public class JdbcClientDetailsService implements ClientDetailsService, ClientRegistrationService {
  	
      //sql:select client_id,client_secret, resource_ids, scope,
  	//    authorized_grant_types, web_server_redirect_uri, 
      //    access_token_validity,  authorities,refresh_token_validity,
  	//	  additional_information, autoapprove
      //    from oauth_client_details  where client_id = ?
      
      //根据cientId查询ClientDetails
      public ClientDetails loadClientByClientId(String clientId){
  		ClientDetails details;
  		try {
  			details = jdbcTemplate
                  .queryForObject(selectClientDetailsSql
                                  , new ClientDetailsRowMapper(), clientId);
  		}
  		catch (EmptyResultDataAccessException e) {
  			throw new NoSuchClientException("No client with requested id: ");
  		}
  		return details;
  	}
      
  }
  ```

  

- **请求资源服务**

  ```java
  public class OAuth2AuthenticationProcessingFilter 
      implements Filter, InitializingBean {
      
      public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,ServletException {
           
          //请求信息
          final HttpServletRequest request = (HttpServletRequest) req;
          //响应信息
  		final HttpServletResponse response = (HttpServletResponse) res;
          try {
             //从请求头(Authorization)中获取token,构建出来的认证请求
             Authentication authentication = tokenExtractor.extract(request);
             if (authentication == null) {
                 //认证信息不存在，清除当前线程的上下文
  			   SecurityContextHolder.clearContext();
  			}else{
                 //设置要传递的token值
                 request.setAttribute(
                     OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, authentication
                     .getPrincipal());
                 if (authentication instanceof AbstractAuthenticationToken) {
                     //资源认证
  					AbstractAuthenticationToken needsDetails 
                          = (AbstractAuthenticationToken) authentication;
  					needsDetails.setDetails(authenticationDetailsSource
                                              .buildDetails(request));
  				}
                 //授权认证
                 Authentication authResult = authenticationManager
                     .authenticate(authentication);
                 //给当前线程设置认证信息
                 SecurityContextHolder.getContext().setAuthentication(authResult);
             } 
          }catch (OAuth2Exception failed) {
              //授权失败时
  			SecurityContextHolder.clearContext();
              //端点异常处理器
  			authenticationEntryPoint.commence(request, response,
  					new InsufficientAuthenticationException(
                          failed.getMessage(), failed));
  			return;
  		}
          //过滤流继续执行
          chain.doFilter(request, response);
     }
  }
  
  //异常处理
  public class OAuth2AuthenticationEntryPoint 
      extends AbstractOAuth2SecurityExceptionHandler implements
  		AuthenticationEntryPoint {
      
      public void commence(HttpServletRequest request, 
                           HttpServletResponse response, 
                           AuthenticationException authException)
  			throws IOException, ServletException {
  		doHandle(request, response, authException);
  	}
      
      protected final void doHandle(HttpServletRequest request, HttpServletResponse response, Exception authException)
  			throws IOException, ServletException {
  		try {
              //解析异常
  			ResponseEntity<?> result = exceptionTranslator.translate(authException);
              //扩展respone的属性和内容
  			result = enhanceResponse(result, authException);
              //respone 刷新缓存直接返回
  			exceptionRenderer.handleHttpEntityResponse(result, new ServletWebRequest(
                  request, response));
  			response.flushBuffer();
  		}
  		catch (ServletException e) {
  			if (handlerExceptionResolver
                  .resolveException(request, response, this, e) == null) {
  				throw e;
  			}
  		}
  		catch (IOException e) {
  			throw e;
  		}
  		catch (RuntimeException e) {
  			throw e;
  		}
  		catch (Exception e) {	
  			throw new RuntimeException(e);
  		}
  	}
  }
  
  public abstract class WebSecurityConfigurerAdapter implements
  		WebSecurityConfigurer<WebSecurity> {
      
      //认证
      public Authentication authenticate(Authentication authentication)
  				throws AuthenticationException {
  			if (delegate != null) {
  				return delegate.authenticate(authentication);
  			}
  
          	//创建delegate
  			synchronized (delegateMonitor) {
  				if (delegate == null) {
  					delegate = this.delegateBuilder.getObject();
  					this.delegateBuilder = null;
  				}
  			}
  
  			return delegate.authenticate(authentication);
  		}
      
  }
  
  public class OAuth2AuthenticationManager 
      implements AuthenticationManager, InitializingBean {
      
      public Authentication authenticate(Authentication authentication) 
          throws AuthenticationException {
  
  		if (authentication == null) {
  			throw new InvalidTokenException("Invalid token (token not found)");
  		}
          //获取access_token
  		String token = (String) authentication.getPrincipal();
          //根据token获取OAuth2Authentication
  		OAuth2Authentication auth = tokenServices.loadAuthentication(token);
  		if (auth == null) {
  			throw new InvalidTokenException("Invalid token: " + token);
  		}
  		//获取可访问资源
  		Collection<String> resourceIds = auth.getOAuth2Request().getResourceIds();
  		if (resourceId != null && resourceIds != null
              && !resourceIds.isEmpty() && !resourceIds.contains(resourceId)) {
  			throw new OAuth2AccessDeniedException("Invalid token does not)");
  		}
  
          //检查clientId与权限
  		checkClientDetails(auth);
  
  		if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
  			OAuth2AuthenticationDetails details 
                  = (OAuth2AuthenticationDetails) authentication.getDetails();
  			if (!details.equals(auth.getDetails())) {
  				details.setDecodedDetails(auth.getDetails());
  			}
  		}
  		auth.setDetails(authentication.getDetails());
  		auth.setAuthenticated(true);
  		return auth;
  
  	}
      
  }
  
  public class DefaultTokenServices 
      implements AuthorizationServerTokenServices, ResourceServerTokenServices,
  		ConsumerTokenServices, InitializingBean {
        
      public OAuth2Authentication loadAuthentication(String accessTokenValue) 
          throws AuthenticationException,InvalidTokenException {
          //根据accessToken获取OAuth2AccessToken
  		OAuth2AccessToken accessToken = tokenStore.readAccessToken(accessTokenValue);
  		if (accessToken == null) {
  			throw new InvalidTokenException("Invalid access token: ");
  		}else if (accessToken.isExpired()) {
              //过期移除
  			tokenStore.removeAccessToken(accessToken);
  			throw new InvalidTokenException("Access token expired: ");
  		}
  
          //读取认证信息
  		OAuth2Authentication result = tokenStore.readAuthentication(accessToken);
  		if (result == null) {
  			throw new InvalidTokenException("Invalid access token: ");
  		}
          //判断clientId的合法性
  		if (clientDetailsService != null) {
  			String clientId = result.getOAuth2Request().getClientId();
  			try {
  				clientDetailsService.loadClientByClientId(clientId);
  			}
  			catch (ClientRegistrationException e) {
  				throw new InvalidTokenException("Client not valid: " + clientId, e);
  			}
  		}
  		return result;
  	}        
      
  }
  ```

  

- **FilterChain**

  - **WebAsyncManagerIntegrationFilter**：异步方式
  - **SecurityContextPersistenceFilter**：同步方式
  - ***HeaderWriterFilter***：用来给http响应添加一些Header
  - ***CsrfFilter***：默认开启，用于防止csrf攻击的过滤器
  - ***LogoutFilter***：处理注销的过滤器
  - ***UsernamePasswordAuthenticationFilter***：调用的AuthenticationManager.authenticate()方法
  - **DefaultLoginPageGeneratingFilter**：默认登录页面
  - **DefaultLogoutPageGeneratingFilter**：默认登出页面
  - ***BasicAuthenticationFilter***：默认HTTP authorization
  - ***RequestCacheAwareFilter***：内部维护了一个RequestCache，用于缓存request请求
  - ***SecurityContextHolderAwareRequestFilter***：使得request具有更加丰富的API
  - ***AnonymousAuthenticationFilter***：匿名身份过滤器，spring security为了兼容未登录的访问
  - ***SessionManagementFilter***：session相关的过滤器
  - ***ExceptionTranslationFilter***：异常转换过滤器
  - ***FilterSecurityInterceptor***：访问特定路径应该具备的权限

  

## 3.服务间调用

### 3-1.Feign/Ribbon/Hystrix

#### Feign的启动：

```
实现 ImportBeanDefinitionRegistrar，spring启动时加载
1.注入相关的配置bean 
2.scan包下下所有配置@FeignClint的类（FeignClientFactoryBean）并注入
```

```java
class FeignClientsRegistrar{   

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata,
                BeanDefinitionRegistry registry) {    
            //注册配置
            registerDefaultConfiguration(metadata, registry);
            //注入@FeignClient(FeignClientFactoryBean)实例 
            //通过FeignClientFactoryBean实现了FactoryBean
            //在spring注入调用的是getObject方法返回对象
            registerFeignClients(metadata, registry);
        }              

}
```

#### Feign的流程：

1. @FeignClient远程调用接口
2. 通过动态代理JAVA Proxy，实现远程接口调用（ReflectiveFeign）,使用Hystrix(HystrixFeign.build())
3. 根据API的方法实例，进行MethodHandler方法处理调用（InvocationHandler/HystrixInvocationHandler）
4. MethodHandler处理(SynchronousMethodHandler)
5. 构造RestTemplate （ReflectiveFeign.create）
6. Encode 
7. Interceptors/logger,请求和返回的拦截处理与日志记录
8. feign.Client,基于负载均衡/重试/不同HTTP框架发送请求
9. Decode

#### @FeignClient：

```java
@FeignClient(name = "user", path = "/user")
public interface UserApi {

    @GetMapping("/{userId}")
    ResultBean<AuthUser> getUserById(@PathVariable Integer userId);

}
```

#### 动态代理@FeignClient：

```java
public class ReflectiveFeign extends Feign {

    public <T> T newInstance(Target<T> target) {
    Map<String, MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
    Map<Method, MethodHandler> methodToHandler 
        = new LinkedHashMap<Method, MethodHandler>();
    List<DefaultMethodHandler> defaultMethodHandlers 
        = new LinkedList<DefaultMethodHandler>();

    for (Method method : target.type().getMethods()) {
      if (method.getDeclaringClass() == Object.class) {
        continue;
      } else if (Util.isDefault(method)) {
        DefaultMethodHandler handler = new DefaultMethodHandler(method);
        defaultMethodHandlers.add(handler);
        methodToHandler.put(method, handler);
      } else {
        methodToHandler.put(method, 
                            nameToHandler.get(Feign.configKey(target.type(), method)));
      }
    }
    //创建InvocationHandler
    InvocationHandler handler = factory.create(target, methodToHandler);
    //Proxy.newProxyInstance():java动态代理
    //loader:一个ClassLoader对象，定义了由哪个ClassLoader对象来对生成的代理对象进行加载
    //interfaces:一个Interface对象的数组，表示的是我将要给我需要代理的对象提供一组什么接口
    //h: 动态代理对象在调用方法的时候，会关联到哪一个InvocationHandler对象上
    T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(),
        new Class<?>[] {target.type()}, handler);

    for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
      defaultMethodHandler.bindTo(proxy);
    }
    return proxy;
  }
}

//Hystrix
Feign build(final FallbackFactory<?> nullableFallbackFactory) {
      super.invocationHandlerFactory(new InvocationHandlerFactory() {
        @Override
        public InvocationHandler create(Target target,
                                        Map<Method, MethodHandler> dispatch) {
          return new HystrixInvocationHandler(target, dispatch, setterFactory,
              nullableFallbackFactory);
        }
      });
      super.contract(new HystrixDelegatingContract(contract));
      return super.build();
}
```

#### MethodHandler处理：

```java
static class FeignInvocationHandler implements InvocationHandler {
	//存储了哪个方法对应的MethodHandler(方法实例对象-方法处理器)
    private final Map<Method, MethodHandler> dispatch;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if ("equals".equals(method.getName())) {
        try {
          Object otherHandler =
              args.length > 0 && args[0] != null ? 
              Proxy.getInvocationHandler(args[0]) : null;
            
          return equals(otherHandler);
        } catch (IllegalArgumentException e) {
          return false;
        }
      } else if ("hashCode".equals(method.getName())) {
        return hashCode();
      } else if ("toString".equals(method.getName())) {
        return toString();
      }
	  //动态代理请求
      return dispatch.get(method).invoke(args);
    }
    
}

//hystrix处理器
final class HystrixInvocationHandler implements InvocationHandler {
    
    @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {
    //equals,hashCode,toString方法不拦截
    if ("equals".equals(method.getName())) {
      try {
        Object otherHandler =
            args.length > 0 && args[0] != null ? 
            Proxy.getInvocationHandler(args[0]) : null;
        return equals(otherHandler);
      } catch (IllegalArgumentException e) {
        return false;
      }
    } else if ("hashCode".equals(method.getName())) {
      return hashCode();
    } else if ("toString".equals(method.getName())) {
      return toString();
    }

    HystrixCommand<Object> hystrixCommand =
        new HystrixCommand<Object>(setterMethodMap.get(method)) {
          @Override
          protected Object run() throws Exception {
            try {
              //动态代理请求
              return HystrixInvocationHandler.this.dispatch.get(method).invoke(args);
            } catch (Exception e) {
              throw e;
            } catch (Throwable t) {
              throw (Error) t;
            }
          }

          @Override
          protected Object getFallback() {
            if (fallbackFactory == null) {
              return super.getFallback();
            }
            try {
              Object fallback = fallbackFactory.create(getExecutionException());
              Object result = fallbackMethodMap.get(method).invoke(fallback, args);
              if (isReturnsHystrixCommand(method)) {
                return ((HystrixCommand) result).execute();
              } else if (isReturnsObservable(method)) {
                // Create a cold Observable
                return ((Observable) result).toBlocking().first();
              } else if (isReturnsSingle(method)) {
                // Create a cold Observable as a Single
                return ((Single) result).toObservable().toBlocking().first();
              } else if (isReturnsCompletable(method)) {
                ((Completable) result).await();
                return null;
              } else if (isReturnsCompletableFuture(method)) {
                return ((Future) result).get();
              } else {
                return result;
              }
            } catch (IllegalAccessException e) {
              // shouldn't happen as method is public due to being an interface
              throw new AssertionError(e);
            } catch (InvocationTargetException | ExecutionException e) {
              // Exceptions on fallback are tossed by Hystrix
              throw new AssertionError(e.getCause());
            } catch (InterruptedException e) {
              // Exceptions on fallback are tossed by Hystrix
              Thread.currentThread().interrupt();
              throw new AssertionError(e.getCause());
            }
          }
        };

    //根据返回类型返回，可以返回异步结果，或者自己订阅
    if (Util.isDefault(method)) {
      return hystrixCommand.execute();
    } else if (isReturnsHystrixCommand(method)) {
      return hystrixCommand;
    } else if (isReturnsObservable(method)) {
      return hystrixCommand.toObservable();
    } else if (isReturnsSingle(method)) {
      return hystrixCommand.toObservable().toSingle();
    } else if (isReturnsCompletable(method)) {
      return hystrixCommand.toObservable().toCompletable();
    } else if (isReturnsCompletableFuture(method)) {
      return new ObservableCompletableFuture<>(hystrixCommand);
    }
    return hystrixCommand.execute();
  }
}
```

#### 编码/构造RequestTemplate

```java
//默认处理器
final class SynchronousMethodHandler implements MethodHandler {
    //构造RequestTemplate,编码
    RequestTemplate template = buildTemplateFromArgs.create(argv);
    Options options = findOptions(argv);
    //重试次数
    Retryer retryer = this.retryer.clone();
    while (true) {
      try {
        //执行并解码
        return executeAndDecode(template, options);
      } catch (RetryableException e) {
        try {
          retryer.continueOrPropagate(e);
        } catch (RetryableException th) {
          Throwable cause = th.getCause();
          if (propagationPolicy == UNWRAP && cause != null) {
            throw cause;
          } else {
            throw th;
          }
        }
        if (logLevel != Logger.Level.NONE) {
          logger.logRetry(metadata.configKey(), logLevel);
        }
        continue;
      }
    }
    
}

private static class BuildEncodedTemplateFromArgs extends BuildTemplateByResolvingArgs {

    private final Encoder encoder;

    @Override
    protected RequestTemplate resolve(Object[] argv,
                                      RequestTemplate mutable,
                                      Map<String, Object> variables) {
      Object body = argv[metadata.bodyIndex()];
      try {
        encoder.encode(body, metadata.bodyType(), mutable);
      }
      return super.resolve(argv, mutable, variables);
    }
  }
```

#### 请求拦截/日志处理/解码：

```java
//执行请求并解码
Object executeAndDecode(RequestTemplate template, Options options) throws Throwable {
	//拦截处理
    Request request = targetRequest(template);
    //执行请求
    response = client.execute(request, options);
    if (response.status() >= 200 && response.status() < 300) {
        if (void.class == metadata.returnType()) {
          return null;
        } else {
           //返回成功并且不是void解码
          Object result = decode(response);
          shouldClose = closeAfterDecode;
          return result;
        }
      }
}

//拦截处理
Request targetRequest(RequestTemplate template) {
    for (RequestInterceptor interceptor : requestInterceptors) {
      interceptor.apply(template);
    }
    //默认判一下url
    return target.apply(template);
}

//解码处理
Object decode(Response response) throws Throwable {
    try {
      return decoder.decode(response, metadata.returnType());
    }
 }
```

#### feign.Client：

**client类型：**

- **Client.Default：**默认的feign.Client 客户端实现类，内部使用HttpURLConnnection 完成URL请求处理
- **ApacheHttpClient：**使用 Apache httpclient 开源组件完成URL请求处理
- **OkHttpClient:**内部使用 OkHttp3 开源组件完成URL请求处理
- **LoadBalancerFeignClient ：**内部使用 Ribben 负载均衡技术完成URL请求处理的feign.Client 客户端实现类

```java
public interface Client {
	Response execute(Request request, Options options) throws IOException;
}
```

### Hystrix的实现

feign调用时会指定使用HystrixInvocationHandler，会构造一个HystrixCommand

```java
public abstract class HystrixCommand<R> extends AbstractCommand<R> implements HystrixExecutable<R>, HystrixInvokableInfo<R>, HystrixObservable<R> {
	
     public Future<R>  () {
         //阻塞获取结果
         final Future<R> delegate = toObservable().toBlocking().toFuture();
         
         @Override
         public R get() throws InterruptedException, ExecutionException {
            return delegate.get();
         }
     }
  	
    //Request缓存
    protected final HystrixRequestCache requestCache;
    
    //主要方法
    public Observable<R> toObservable() {
        return Observable.defer(()->{
            //CAS操作(讲初始的未开始状态设置成为订阅链创建状态)
            if (!commandState.compareAndSet(
                CommandState.NOT_STARTED, CommandState.OBSERVABLE_CHAIN_CREATED)) {
               //.... 
            }
            //是否启用缓存
            final boolean requestCacheEnabled = isRequestCachingEnabled();
            final String cacheKey = getCacheKey();
           	//尝试从缓存中获取
            if (requestCacheEnabled) {
         		//从缓存中获取Response
                HystrixCommandResponseFromCache<R> fromCache 
                        = requestCache.get(cacheKey);
                if (fromCache != null) {
                     //返回结果
                     isResponseFromCache = true;
                     return handleRequestCacheHitAndEmitValues(fromCache, _cmd);
                }	
            }
            
            //请求调用链
            Observable<R> hystrixObservable =
                        Observable.defer(applyHystrixSemantics)
                                .map(wrapWithAllOnNextHooks);
            
            if (requestCacheEnabled && cacheKey != null) {
                //若设置了开启缓存，但是缓存中没有取到Response
                //执行
                HystrixCachedObservable<R> toCache 
                     = HystrixCachedObservable.from(hystrixObservable, _cmd);
                //存入缓存Map
                HystrixCommandResponseFromCache<R> fromCache 
                        = requestCache.putIfAbsent(cacheKey, toCache);
                if (fromCache != null) {
                     //其他线程先完成了，取消订阅，不执行了,并直接返回结果
                     toCache.unsubscribe();
                     isResponseFromCache = true;
                     return handleRequestCacheHitAndEmitValues(fromCache, _cmd);
                } else {
 					 //直接调用请求链
                     afterCache = toCache.toObservable();
                }
            }else {
                //直接调用请求链
                afterCache = hystrixObservable;
            }
        });
    }
}

//开始执行调用链
final Func0<Observable<R>> applyHystrixSemantics = new Func0<Observable<R>>() {
            @Override
            public Observable<R> call() {
                //当前已经是OBSERVABLE_CHAIN_CREATED状态了
                if (commandState.get().equals(CommandState.UNSUBSCRIBED)) {
                    return Observable.never();
                }
                return applyHystrixSemantics(_cmd);
            }
 };

private Observable<R> applyHystrixSemantics(final AbstractCommand<R> _cmd) {
      	//开启钩子
        executionHook.onStart(_cmd);

        //判断是否熔断
        if (circuitBreaker.allowRequest()) {
            //获取信号用
            final TryableSemaphore executionSemaphore = getExecutionSemaphore();
            final AtomicBoolean semaphoreHasBeenReleased = new AtomicBoolean(false);
            //超时处理
            final Action0 singleSemaphoreRelease = new Action0() {
                @Override
                public void call() {
                    //释放信号量
                    if (semaphoreHasBeenReleased.compareAndSet(false, true)) {
                        executionSemaphore.release();
                    }
                }
            };
			//异常处理
            final Action1<Throwable> markExceptionThrown = new Action1<Throwable>() {
                @Override
                public void call(Throwable t) {
                    eventNotifier.markEvent(
                        HystrixEventType.EXCEPTION_THROWN, commandKey);
                }
            };
			//尝试获取信号量
            if (executionSemaphore.tryAcquire()) {
                try {
                    executionResult = executionResult
                        .setInvocationStartTime(System.currentTimeMillis());
                    //开始执行订阅cmd，真正的调用SynchronousMethodHandler请求
                    return executeCommandAndObserve(_cmd)
                            .doOnError(markExceptionThrown)
                        	//超时降级或者抛异常，发送信息给熔断器统计
                            .doOnTerminate(singleSemaphoreRelease)
                            .doOnUnsubscribe(singleSemaphoreRelease);
                } catch (RuntimeException e) {
                    return Observable.error(e);
                }
            } else {
                //信用量满了，降级或者抛异常，发送信息给熔断器统计
                return handleSemaphoreRejectionViaFallback();
            }
        } else {
            //已经熔断，降级或者抛异常
            return handleShortCircuitViaFallback();
        }
    }
```

#### 调用 fallback 降级机制：

- 断路器处于打开状态
- 线程池/队列/semaphore满了
- command 执行超时
- run() 或者 construct() 抛出异常

#### Hystrix流程：

1. **修改Command状态:**（NOT_STARTED->OBSERVABLE_CHAIN_CREATED）

2. **判断是否开始了RequestCache: ** hystrix.requestCache.enabled (默认开启)
   2-1. **若开启了缓存，重写了getCacheKey()的规则**，requestCache中没有则，再进行一次判断
          ，判断是否有其他线程先执行，若有取消订阅直接返回结果，没有直接添加请求调用链

   2-2.**没有开启缓存**,直接添加调用链（hystrixObservable）

3. **判断开启熔断：**若已经熔断，直接降级处理

4. **尝试获取信号量：**获取到信号量执行线程，线程中会执行请求

#### Hystrix隔离策略：

- **信号量隔离:**   TryableSemaphoreActual  (记录当前请求信号量的线程数 和 初始化最大信号量 比较)
- **线程池隔离(默认):**  executeCommandWithSpecifiedIsolation方法 (通过指定rxjava  Observable的线程进行指定 HystrixThreadPool 的线程池)

```java
@Override
public boolean isOpen() {
    if (circuitOpen.get()) {
        //熔断已经启动
        return true;
    }

    // 获取健康检查
    HealthCounts health = metrics.getHealthCounts();

    //总请求数与最小跳匝（不够频繁）
    if (health.getTotalRequests() < properties
        .circuitBreakerRequestVolumeThreshold().get()) {
        return false;
    }

    //错误次数
    if (health.getErrorPercentage() < properties
        .circuitBreakerErrorThresholdPercentage().get()) {
        return false;
    } else {
       	//错误率太高
        if (circuitOpen.compareAndSet(false, true)) {
            circuitOpenedOrLastTestedTime.set(System.currentTimeMillis());
            return true;
        } else {
            return true;
        }
    }
}		
```

### Ribbon的实现

```java
public class LoadBalancerFeignClient implements Client {

    @Override
	public Response execute(Request request, Request.Options options) 
        throws IOException {
		try {
			URI asUri = URI.create(request.url());
			String clientName = asUri.getHost();
			URI uriWithoutHost = cleanUrl(request.url(), clientName);
            //构造请求，这里ribbonRequest：GET http:///sayHello/wangmeng HTTP/1.1 
			FeignLoadBalancer.RibbonRequest ribbonRequest
                = new FeignLoadBalancer.RibbonRequest(
					this.delegate, request, uriWithoutHost);

            //这里面config只有两个超时时间，一个是connectTimeout：5000，一个是readTimeout：5000
			IClientConfig requestConfig = getClientConfig(options, clientName);
            //真正执行负载均衡的地方
			return lbClient(clientName)
					.executeWithLoadBalancer(ribbonRequest, requestConfig).toResponse();
		}
		catch (ClientException e) {
			IOException io = findIOException(e);
			if (io != null) {
				throw io;
			}
			throw new RuntimeException(e);
		}
	}
    
    //负载均衡
    public FeignLoadBalancer create(String clientName) {
        //cache缓存
		FeignLoadBalancer client = this.cache.get(clientName);
		if (client != null) {
			return client;
		}
		IClientConfig config = this.factory.getClientConfig(clientName);
        //lb里包含所有节点的信息
		ILoadBalancer lb = this.factory.getLoadBalancer(clientName);
		ServerIntrospector serverIntrospector = this.factory.getInstance(clientName,
				ServerIntrospector.class);
		client = this.loadBalancedRetryFactory != null
            	//可重试
				? new RetryableFeignLoadBalancer(lb, config, serverIntrospector,
						this.loadBalancedRetryFactory)
            	//正常的
				: new FeignLoadBalancer(lb, config, serverIntrospector);
		this.cache.put(clientName, client);
		return client;
	}
    
    public T executeWithLoadBalancer(
        final S request, final IClientConfig requestConfig) 
        throws ClientException {
        
        LoadBalancerCommand<T> command 
            = buildLoadBalancerCommand(request, requestConfig);

        try {
            return command.submit(
                new ServerOperation<T>() {
                    @Override
                    public Observable<T> call(Server server) {
                        URI finalUri 
                            = reconstructURIWithServer(server, request.getUri());
                        S requestForServer = (S) request.replaceUri(finalUri);
                        try {
                            return Observable
                                .just(AbstractLoadBalancerAwareClient
                                      .this.execute(requestForServer, requestConfig));
                        } 
                        catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                })
                .toBlocking()
                .single();
        } catch (Exception e) {
            Throwable t = e.getCause();
            if (t instanceof ClientException) {
                throw (ClientException) t;
            } else {
                throw new ClientException(e);
            }
        }
        
    }
    
}

public class LoadBalancerCommand<T> {
    
    public Observable<T> submit(final ServerOperation<T> operation) {
        
        Observable<T> o = 
            	//选择实例
                (server == null ? selectServer() : Observable.just(server));
        
    }
    
    private Observable<Server> selectServer() {
        return Observable.create(new OnSubscribe<Server>() {
            @Override
            public void call(Subscriber<? super Server> next) {
                try {
                    Server server
                        = loadBalancerContext
                        .getServerFromLoadBalancer(loadBalancerURI, loadBalancerKey);   
                    next.onNext(server);
                    next.onCompleted();
                } catch (Exception e) {
                    next.onError(e);
                }
            }
        });
    }
    
    public Server getServerFromLoadBalancer(@Nullable URI original, @Nullable Object loadBalancerKey) throws ClientException {
        ILoadBalancer lb = getLoadBalancer();
        if (host == null) {
            if (lb != null){
                //选择实例
                Server svc = lb.chooseServer(loadBalancerKey);
                if (svc == null){
                    throw new ClientException(ClientException.ErrorType.GENERAL);
                }
                host = svc.getHost();
                if (host == null){
                    throw new ClientException(ClientException.ErrorType.GENERAL;
                }
                return svc;
            }
    }
}
```

####    负载Rule

- **RetryRule：**

- **RoundRobinRule：**基本的负载均衡策略，即循环赛规则

-  **WeightedResponseTimeRule：**加权轮循

   



## 4. 链路追踪

### 1.Zipkin+Sleuth

#### Span

```java
public final class Span implements Serializable {
	 final String traceId; //每一个链路的唯一标识 (追踪request-> response的全过程)
     final String parentId; //本次调用的发起者
     final String id; //当前spanId，每一个span都有一个唯一id标识请求到某一个服务组件。
     String name; //span名称,一般是方法名
     final Kind kind; //跨度类型(CLIENT()/SERVER/PRODUCER/CONSUMER)
     final long timestamp, duration;//span的开始时间和结束时间
     final Endpoint localEndpoint, remoteEndpoint;//记录当前span的服务，和目标服务
     ArrayList<Annotation> annotations; //事件与时间戳
     TreeMap<String, String> tags; //span的上下文信息，比如：http.method、http.path
}
```

#### Annotation（v1）:

​	用于记录事件与时间戳

- **cs：** Client Send，表示客户端发起请求.
- **sr:**    Server Receive，表示服务端收到请求。
- **ss:**   Server Send，表示服务端完成处理，并将结果发送给客户端。
- **cr:**   Client Received，表示客户端获取到服务端返回信息。

#### **Kind** （V2）：

- **CLIENT：** cs
- **SERVER：** sr
- **PRODUCER：** ss
- **CONSUMER：** cr

#### Sleuth追踪原理

1.  **Scheduled定时任务:**

   拦截@Scheduled注解的定时任务

   ```java
   @Aspect
   public class TraceSchedulingAspect {
   
       @Around("execution (@org.springframework.scheduling.annotation.Scheduled 
               * *.*(..))")
   	public Object traceBackgroundThread(final ProceedingJoinPoint pjp) 
           throws Throwable {
           
       }
       
   }
   ```

2.  **Feign调用：**

   实现了Feign.Client，Feign是通过Client接口的execute去调用其他的服务。相当于调用前记录。

   ```java
   public class TraceLoadBalancerFeignClient extends LoadBalancerFeignClient {
   
       @Override
   	public Response execute(Request request, Request.Options options) 
           throws IOException {
           
           Response response = null;
   		Span fallbackSpan = tracer().nextSpan().start();
           try {
   			response = super.execute(request, options);
   			return response;
   		}finally {
   			fallbackSpan.abandon();
   		}
       }
       
   }
   ```

   

3.  **Hystrix/Rxjava：**

   通过**HystrixPlugins**自定义SleuthHystrixConcurrencyStrategy ,在执行回调时，生成span，再执行其他回调。

   ```java
   public class SleuthHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {
   	
       @Override
   	public <T> Callable<T> wrapCallable(Callable<T> callable) {
           
           if (passthrough) {
   			return this.tracing.currentTraceContext().wrap(callable);
   		}
   		else {
   			return new TraceCallable<>(this.tracing, this.spanNamer, wrappedCallable,
   					HYSTRIX_COMPONENT);
   		}
       }
       
       public class TraceCallable<V> implements Callable<V> {
           
           @Override
           public V call() throws Exception {
               ScopedSpan span = this.tracer.startScopedSpanWithParent(this.spanName,
                       this.parent);
               try {
                   return this.delegate.call();
               }
               catch (Exception | Error ex) {
                   span.error(ex);
                   throw ex;
               }
               finally {
                   span.finish();
               }
           }
           
       }
   }
   ```

4.  **Async(异步)/线程池:**

   拦截@Async

   ```java
   @Aspect
   public class TraceAsyncAspect {
       
   	@Around("execution (@org.springframework.scheduling.annotation.Async  * *.*(..))")
   	public Object traceBackgroundThread(final ProceedingJoinPoint pjp) 
           throws Throwable {
           
           String spanName = name(pjp);
   		Span span = this.tracer.currentSpan();
   		if (span == null) {
   			span = this.tracer.nextSpan();
   		}
   		span = span.name(spanName);
   		try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(span.start())) {
   			span.tag(CLASS_KEY, pjp.getTarget().getClass().getSimpleName());
   			span.tag(METHOD_KEY, pjp.getSignature().getName());
   			return pjp.proceed();
   		}
   		finally {
   			span.finish();
   		}
           
       }
   }
   ```

   通过包装线程池，若非同一个上下文，会创建

   ```java
   public class TraceableExecutorService implements ExecutorService {
   
   	public TraceableExecutorService(BeanFactory beanFactory,
   			final ExecutorService delegate, String spanName) {
   		this.delegate = delegate;
   		this.beanFactory = beanFactory;
   		this.spanName = spanName;
   	}
   	
   	@Override
   	public void execute(Runnable command) {
   		this.delegate.execute(ContextUtil.isContextInCreation(this.beanFactory) 
   		? command
   				: new TraceRunnable(tracing(), spanNamer(), command, this.spanName));
   	}
   }
   ```

5. **web:**

   拦截器TraceWebFilter，请求拦截。也提供@RestController/@Controller/@Callable注解的AOP拦截

   ```java
   public final class TraceWebFilter implements WebFilter, Ordered {
   
   	@Override
   	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
   		if (tracer().currentSpan() != null) {
   			tracer().withSpanInScope(null);
   		}
   		String uri = exchange.getRequest().getPath().pathWithinApplication().value();
   		return new MonoWebFilterTrace(chain.filter(exchange), exchange, this);
   	}
   
   }
   ```

6. **rpc/gpc/zuul**

### Zipkin的实现

- **collector：** 信息收集器，是一个**守护进程**，对客户端传输的span，进行**验证**、**存储**以及**创建查询索引**。
- **storage:**  存储组件,默认InMemoryStorage(本地内存)，此外支持使用Cassandra、ElasticSearch 和 Mysql。
- **search:** 查询进程,提供简单的JSON API来供外部调用查询。
- **web UI ：** zipkin的服务端展示平台，调用search提供的接口，用图表将链路信息展示出来。

#### HttpTrace 收集（支持rest，thrift，protobuf）

1. 客户端通过接口上传Trace信息
2. 解码spans
3.  Collector(对span进行验证、存储并设置索引），添加本地监控spans数量，并且为了防止高峰出现问题，对没超过边界的traceId记录日志处理。
4. 日志记录处理

```java
public class ZipkinHttpCollector {
	
  @Post("/api/v2/spans")
  public HttpResponse uploadSpans(byte[] serializedSpans) {
    //解码并存储spans
    return validateAndStoreSpans(SpanBytesDecoder.JSON_V2, serializedSpans);
  }
    
}

public class Collector {
    
    public void accept(List<Span> spans, Callback<Void> callback) {
    if (spans.isEmpty()) {
      callback.onSuccess(null);
      return;
    }
    metrics.incrementSpans(spans.size());

    List<Span> sampled = sample(spans);
    if (sampled.isEmpty()) {
      callback.onSuccess(null);
      return;
    }

    try {
      //记录日志
      record(sampled, acceptSpansCallback(sampled));
      callback.onSuccess(null);
    } catch (RuntimeException e) {
      callback.onError(errorStoringSpans(sampled, e));
      return;
    }
  }
    
  void record(List<Span> sampled, Callback<Void> callback) {
    storage.spanConsumer().accept(sampled).enqueue(callback);
  }
}
```

#### Brave

#### Tracing（链路追踪组件）:

```java
public class Tracer {
	String localServiceName = "unknown"; //当前服务名称
    String localIp; //当前服务ip地址
    int localPort; // 服务端口
    Reporter<zipkin2.Span> spanReporter;//reporter，用于处理链路信息
    Clock clock; //用于计时
    Sampler sampler = Sampler.ALWAYS_SAMPLE;//采样器，用于定义采样规则，默认全样采集
    //用于获取当前 TraceContext ，默认使用了 InheritableThreadLocal，支持复制到异步线程
    CurrentTraceContext currentTraceContext = CurrentTraceContext.Default.inheritable();
    //顾名思义，traceId是否128bit，是否支持Join一个跨度
    boolean traceId128Bit = false, supportsJoin = true, alwaysReportSpans = false;
    boolean trackOrphans = false;
    //传播工厂，用于定义传播规则，如何注入与提取等
    Propagation.Factory propagationFactory = B3Propagation.FACTORY;
    //错误处理器
    ErrorParser errorParser = new ErrorParser();
    //span结束回调器
    Set<FinishedSpanHandler> finishedSpanHandlers = new LinkedHashSet<>();
}
```

#### span的创建:

```java
public class Tracer {

  //创建一个新的链路
  public Span newTrace() {
    return _toSpan(newRootContext(0));
  }
   
  //返回一个子跨度
  public Span nextSpan() {
    //获取当前链路内容,若不存在创建一个新的链路，若存在创建子跨度。
    TraceContext parent = currentTraceContext.get();
    return parent != null ? newChild(parent) : newTrace();
  }  
    
  Span _toSpan(TraceContext decorated) {
    if (isNoop(decorated)) return new NoopSpan(decorated);
    // 获取或者创建一个挂起的跨度
    //这里多了一个新建的对象叫 PendingSpan ，用于收集一条trace上暂时被挂起的未完成的span
    PendingSpan pendingSpan = pendingSpans.getOrCreate(decorated, false);
    //新建一个跨度(RealSpan是Span的一个实现)
    return new RealSpan(decorated, pendingSpans, 
                        pendingSpan.state(), pendingSpan.clock(),
        finishedSpanHandler);
  }
    
  //同一个链路的跨度span合并
  public final Span joinSpan(TraceContext context) {
    if (context == null) throw new NullPointerException("context == null");
    long parentId = context.parentIdAsLong(), spanId = context.spanId();
    if (!supportsJoin) {
      parentId = context.spanId();
      spanId = 0L;
    }
    return _toSpan(decorateContext(context, parentId, spanId));
  }
}
```

### 2.SkyWalking 

- **探针:**   通过字节码增强无侵入式的收集，并通过 HTTP 或者 gRPC 方式发送数据到平台后端。
- **平台后端：** 用于数据聚合、数据分析以及驱动数据流从探针到用户界面的流程。
- **存储：**  ElasticSearch、H2 或 MySQL 集群（Sharding-Sphere 管理）
- **用户界面：** SkyWalking的可视化界面