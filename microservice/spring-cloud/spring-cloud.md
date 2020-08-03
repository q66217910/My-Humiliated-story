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
  
- **自动refresh token**

  对过期的token进行刷新

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
  ```
  
- **Endpoint**

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
          //grantType类型为implicit隐式不需要获取token
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

  验证Authorization

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

  

- 

