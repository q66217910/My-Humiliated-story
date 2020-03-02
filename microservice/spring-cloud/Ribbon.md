Ribbon
===

1.创建拦截器
---
```java
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept() {
        final URI originalUri = request.getURI();
        // 截取服务名称
        String serviceName = originalUri.getHost();

        // 默认注入的 RibbonAutoConfiguration.RibbonLoadBalancerClient
        return this.loadBalancer.execute(serviceName,
                // 创建请求对象
                this.requestFactory.createRequest(request, body, execution));
    }
}
``` 

2.execute执行
---
```java 
public class RibbonLoadBalancerClient implements LoadBalancerClient {
    public <T> T execute(){
        //获取具体的ILoadBalancer实现
        ILoadBalancer loadBalancer = getLoadBalancer(serviceId);

        // 调用ILoadBalancer 实现获取Server
        Server server = getServer(loadBalancer, hint);
        RibbonServer ribbonServer = new RibbonServer(serviceId, server,
                isSecure(server, serviceId),
                serverIntrospector(serviceId).getMetadata(server));

        //获取状态记录器，保存此次选取的server
        RibbonLoadBalancerContext context = this.clientFactory
                .getLoadBalancerContext(serviceId);
        RibbonStatsRecorder statsRecorder = new RibbonStatsRecorder(context, server);
        T returnVal = request.apply(serviceInstance);
        statsRecorder.recordStats(returnVal);
        return returnVal;
    }
}
```