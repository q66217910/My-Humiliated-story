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

- Eureka server urls:

  ```
  Region: 地理上的分区（例如华东/华南）
  Zone: 为Region内具体分区（华东-1/华东-2）
  
  配置:
  eureka.client.region: 配置eureka region
  eureka.client.service-url.defaultZone:  使用默认ZONE地址
  ```

- 服务注册:

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
              httpResponse = eurekaTransport.registrationClient.sendHeartBeat(instanceInfo.getAppName(), instanceInfo.getId(), instanceInfo, null);
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
                                                     
  public class AbstractJerseyEurekaHttpClient implements EurekaHttpClient {
      
      @Override
      public EurekaHttpResponse<InstanceInfo> sendHeartBeat(String appName, String id, InstanceInfo info, InstanceStatus overriddenStatus) {
          //拼接自身服务地址
          String urlPath = "apps/" + appName + '/' + id;
          ClientResponse response = null;
          try {
              //
              WebResource webResource = jerseyClient.resource(serviceUrl)
                      .path(urlPath)
                      .queryParam("status", info.getStatus().toString())
                      .queryParam("lastDirtyTimestamp", info.getLastDirtyTimestamp().toString());
              if (overriddenStatus != null) {
                  webResource = webResource.queryParam("overriddenstatus", overriddenStatus.name());
              }
              Builder requestBuilder = webResource.getRequestBuilder();
              addExtraHeaders(requestBuilder);
              response = requestBuilder.put(ClientResponse.class);
              EurekaHttpResponseBuilder<InstanceInfo> eurekaResponseBuilder = anEurekaHttpResponse(response.getStatus(), InstanceInfo.class).headers(headersOf(response));
              if (response.hasEntity()) {
                  eurekaResponseBuilder.entity(response.getEntity(InstanceInfo.class));
              }
              return eurekaResponseBuilder.build();
          } finally {
              if (response != null) {
                  response.close();
              }
          }
      }
  }
  ```

  

- 