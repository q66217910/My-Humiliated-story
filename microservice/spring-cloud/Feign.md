Feign
===

 1.FeignClientsRegistrar启动类
 --- 
 实现 ImportBeanDefinitionRegistrar，spring启动时加载
 配置顺序 clients->value  scan包下下所有配置@FeignClint的类（FeignClientFactoryBean ）并注入
```java
class FeignClientsRegistrar{   

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata,
                BeanDefinitionRegistry registry) {    
            //注册配置
            registerDefaultConfiguration(metadata, registry);
            //注入@FeignClient(FeignClientFactoryBean)实例 
            //通过FeignClientFactoryBean实现了FactoryBean,在spring注入调用的是getObject方法返回对象
            registerFeignClients(metadata, registry);
        }              

}
``` 

2.配置类
---
自动注入配置类FeignClientsConfiguration

@ConditionalOnMissingBean作用:没有其他同名Bean时注入,有则不注入

@ConditionalOnClass:当设置类存在时注入

@ConditionalOnProperty: 配置为true才注入下·

@Scope: singleton(单例) / prototype(原型,每次取都是新的实例) 
/ request(每次HTTP请求生产一个新的Bean,当前request)/ session　(每次HTTP请求生产一个新的Bean,当前session)
```java
class FeignClientsConfiguration{ 

        @Bean
    	@ConditionalOnMissingBean
    	public Decoder feignDecoder() {
            //默认解码器
    		return new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));
    	} 
  
        @Bean
        @ConditionalOnMissingBean
        public Encoder feignEncoder() { 
            //默认编码器
            return new SpringEncoder(this.messageConverters);
        }
    
        @Bean
        @ConditionalOnMissingBean
        public Contract feignContract(ConversionService feignConversionService) {
        	//HTTP REQUEST模板解析  
            //@RequestMapping，@RequestBody，@RequestParam，@PathVariable
            return new SpringMvcContract(this.parameterProcessors, feignConversionService);
        }  
   
        @Bean
     	@ConditionalOnMissingBean(FeignLoggerFactory.class)
     	public FeignLoggerFactory feignLoggerFactory() {  
            //日志
     		return new DefaultFeignLoggerFactory(logger);
     	}

        @Bean
      	@Scope("prototype")
      	@ConditionalOnMissingBean
      	public Feign.Builder feignBuilder(Retryer retryer) {   
            //FeignBuilder 带重试
      		return Feign.builder().retryer(retryer);
      	}   

        @Bean
    	@ConditionalOnMissingBean
    	public Retryer feignRetryer() {  
            //重试
    		return Retryer.NEVER_RETRY;
    	}          

        @Configuration
      	@ConditionalOnClass({ HystrixCommand.class, HystrixFeign.class })
      	protected static class HystrixFeignConfiguration {
      		@Bean
      		@Scope("prototype")
      		@ConditionalOnMissingBean
      		@ConditionalOnProperty(name = "feign.hystrix.enabled")
      		public Feign.Builder feignHystrixBuilder() {
      			return HystrixFeign.builder();
      		}
      	}
          

}
```   

3.设置请求目标地址
---
若@FeignClient没有设置url，则根据service-name负载

FeignClientFactoryBean.target()-> Targeter(HystrixTargeter) 
-> ReflectiveFeign.newInstance()->InvocationHandlerFactory.create()


ReflectiveFeign:用于生成动态代理类

Proxy.newProxyInstance():java动态代理

    loader:一个ClassLoader对象，定义了由哪个ClassLoader对象来对生成的代理对象进行加载
    interfaces:一个Interface对象的数组，表示的是我将要给我需要代理的对象提供一组什么接口
    h: 动态代理对象在调用方法的时候，会关联到哪一个InvocationHandler对象上


InvocationHandlerFactory: 统一拦截工厂|
InvocationHandler:统一方法拦截器

HardCodedTarget: 定义目标url/service-name,接口信息(apply()方法,通过contract解析请求模板)
```java
class FeignClientFactoryBean{
    <T> T getTarget() {
    		FeignContext context = applicationContext.getBean(FeignContext.class);
    		Feign.Builder builder = feign(context);
    
    		if (!StringUtils.hasText(this.url)) {
    			if (!this.name.startsWith("http")) {
    				url = "http://" + this.name;
    			}
    			else {
    				url = this.name;
    			}
    			url += cleanPath();
    			return (T) loadBalance(builder, context, new HardCodedTarget<>(this.type,
    					this.name, url));
    		}
    		if (StringUtils.hasText(this.url) && !this.url.startsWith("http")) {
    			this.url = "http://" + this.url;
    		}
    		String url = this.url + cleanPath();
    		Client client = getOptional(context, Client.class);
    		if (client != null) {
    			if (client instanceof LoadBalancerFeignClient) {
    				// not load balancing because we have a url,
    				// but ribbon is on the classpath, so unwrap
    				client = ((LoadBalancerFeignClient)client).getDelegate();
    			}
    			builder.client(client);
    		}
    		Targeter targeter = get(context, Targeter.class);
    		return (T) targeter.target(this, builder, context, new HardCodedTarget<>(
    				this.type, this.name, url));
    	}                     

}
```   

4.请求执行
---
Feign.build()

SynchronousMethodHandler.Factory:接口方法的拦截器创建工厂
SynchronousMethodHandler:接口方法的拦截器，真正拦截的核心，这里真正发起http请求，处理返回结果

RequestInterceptor:在获取请求request对RequestTemplate进行操作(认证、请求头)
Client：请求客户端(Default/LoadBalancerFeignClient)
Options： 请求设置(连接超时时间、读取超时时间、是否允许重定向)

```java
class SynchronousMethodHandler{


   @Override
   public Object invoke(Object[] argv) throws Throwable {   
     //通过动态代理执行                                             
     //创建请求模板
     RequestTemplate template = buildTemplateFromArgs.create(argv); 
     //克隆重试对象,实现是new一个新的
     Retryer retryer = this.retryer.clone();
     while (true) {
       try {
          //执行请求并将放回参数解码
         return executeAndDecode(template);
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
  
   Object executeAndDecode(RequestTemplate template) throws Throwable { 
        //获取目标的请求对象，HardCodedTarget的apply方法
        //并在生成Qequest前,执行RequestInterceptor的apply
       Request request = targetRequest(template);
   
       if (logLevel != Logger.Level.NONE) {
         logger.logRequest(metadata.configKey(), logLevel, request);
       }
   
       Response response;
       long start = System.nanoTime();
       try {   
          //执行请求
         response = client.execute(request, options);
       } catch (IOException e) {
         if (logLevel != Logger.Level.NONE) {
           logger.logIOException(metadata.configKey(), logLevel, e, elapsedTime(start));
         }
         throw errorExecuting(request, e);
       }
       long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
   
       boolean shouldClose = true;
       try {
         if (logLevel != Logger.Level.NONE) {
           response =
               logger.logAndRebufferResponse(metadata.configKey(), logLevel, response, elapsedTime);
         }
         if (Response.class == metadata.returnType()) {
           if (response.body() == null) {
             return response;
           }
           if (response.body().length() == null ||
               response.body().length() > MAX_RESPONSE_BUFFER_SIZE) {
             shouldClose = false;
             return response;
           }
           // Ensure the response body is disconnected
           byte[] bodyData = Util.toByteArray(response.body().asInputStream());
           return response.toBuilder().body(bodyData).build();
         }
         if (response.status() >= 200 && response.status() < 300) {
           if (void.class == metadata.returnType()) {
             return null;
           } else {
             Object result = decode(response);
             shouldClose = closeAfterDecode;
             return result;
           }
         } else if (decode404 && response.status() == 404 && void.class != metadata.returnType()) {
           Object result = decode(response);
           shouldClose = closeAfterDecode;
           return result;
         } else {
           throw errorDecoder.decode(metadata.configKey(), response);
         }
       } catch (IOException e) {
         if (logLevel != Logger.Level.NONE) {
           logger.logIOException(metadata.configKey(), logLevel, e, elapsedTime);
         }
         throw errorReading(request, response, e);
       } finally {
         if (shouldClose) {
           ensureClosed(response.body());
         }
       }
     }

}
```

5.负载均衡(Ribbon)
---
LoadBalancerFeignClient

```java
class LoadBalancerFeignClient{  
    @Override
    	public Response execute(Request request, Request.Options options) throws IOException {
    		try {
    			URI asUri = URI.create(request.url());
    			String clientName = asUri.getHost();
    			URI uriWithoutHost = cleanUrl(request.url(), clientName);
    			FeignLoadBalancer.RibbonRequest ribbonRequest = new FeignLoadBalancer.RibbonRequest(
    					this.delegate, request, uriWithoutHost);
    
    			IClientConfig requestConfig = getClientConfig(options, clientName);
    			return lbClient(clientName).executeWithLoadBalancer(ribbonRequest,
    					requestConfig).toResponse();
    		}
    		catch (ClientException e) {
    			IOException io = findIOException(e);
    			if (io != null) {
    				throw io;
    			}
    			throw new RuntimeException(e);
    		}
    	}
}
```

 

