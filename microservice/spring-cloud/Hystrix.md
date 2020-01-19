Hystrix
===
服务熔断: 监控微服务服务间的调用状态，失败到阈值，开启熔断。

服务降级: 访问一个本地的伪装者,代替真实的服务

服务缓存: 提供了请求缓存、请求合并实现。支持实时监控、报警、控制

1.在Feign中的调用
---
 根据在@FeignClint设置的fallback()/fallbackFactory()去调用不同的target方法。
 ```java
public @interface FeignClient {

 Class<?> fallback() default void.class;  

 Class<?> fallbackFactory() default void.class;
}
```     
fallback()会创建一个默认的Factory

HystrixTargeter
```java
class HystrixTargeter{

    @Override
  	public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context,
  						Target.HardCodedTarget<T> target) {
  		if (!(feign instanceof feign.hystrix.HystrixFeign.Builder)) {
  			return feign.target(target);
  		}
  		feign.hystrix.HystrixFeign.Builder builder = (feign.hystrix.HystrixFeign.Builder) feign;
  		SetterFactory setterFactory = getOptional(factory.getName(), context,
  			SetterFactory.class);
  		if (setterFactory != null) {
  			builder.setterFactory(setterFactory);
  		}
  		Class<?> fallback = factory.getFallback();
  		if (fallback != void.class) {
  			return targetWithFallback(factory.getName(), context, target, builder, fallback);
  		}
  		Class<?> fallbackFactory = factory.getFallbackFactory();
  		if (fallbackFactory != void.class) {
  			return targetWithFallbackFactory(factory.getName(), context, target, builder, fallbackFactory);
  		}
  
  		return feign.target(target);
  	}

     public <T> T target(Target<T> target, T fallback) {
           return build(fallback != null ? new FallbackFactory.Default<T>(fallback) : null)
               .newInstance(target);
         }
     
     public <T> T target(Target<T> target, FallbackFactory<? extends T> fallbackFactory) {
       return build(fallbackFactory).newInstance(target);
     }
} 
```    

在Feign的build方法里初始化了一个Feign调用的invocationHandlerFactory(方法拦截工程 ),
在不使用Hystrix前使用的是FeignInvocationHandler,hystrix改为了HystrixInvocationHandler
```java
class HystrixFeign{

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

}
```  
 HystrixInvocationHandler动态代理了Feign的请求方法
 ```java
 class  HystrixInvocationHandler{
  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {
    // early exit if the invoked method is from java.lang.Object
    // code is the same as ReflectiveFeign.FeignInvocationHandler
    if ("equals".equals(method.getName())) {
      try {
        Object otherHandler =
            args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
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
              } else {
                return result;
              }
            } catch (IllegalAccessException e) {
              // shouldn't happen as method is public due to being an interface
              throw new AssertionError(e);
            } catch (InvocationTargetException e) {
              // Exceptions on fallback are tossed by Hystrix
              throw new AssertionError(e.getCause());
            }
          }
        };

    if (Util.isDefault(method)) {
      return hystrixCommand.execute();
    } else if (isReturnsHystrixCommand(method)) {
      return hystrixCommand;
    } else if (isReturnsObservable(method)) {
      // Create a cold Observable
      return hystrixCommand.toObservable();
    } else if (isReturnsSingle(method)) {
      // Create a cold Observable as a Single
      return hystrixCommand.toObservable().toSingle();
    } else if (isReturnsCompletable(method)) {
      return hystrixCommand.toObservable().toCompletable();
    }
    return hystrixCommand.execute();
  } 
}
```


