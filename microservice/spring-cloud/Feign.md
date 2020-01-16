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

FeignClientFactoryBean.target()--> Targeter(HystrixTargeter) 
--> ReflectiveFeign.newInstance()-->InvocationHandlerFactory.create()


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

4.build
---
Feign.build()

SynchronousMethodHandler.Factory:接口方法的拦截器创建工厂
SynchronousMethodHandler:接口方法的拦截器，真正拦截的核心，这里真正发起http请求，处理返回结果


 

