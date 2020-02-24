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
 HystrixInvocationHandler动态代理了Feign的请求方法 ,前部分与FeignInvocationHandler没有区别,
 后面动态代理部门,使用了HystrixCommand,HystrixCommand里维护了一个线程池。
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
2.Hystrix配置
```java
@ConfigurationProperties("hystrix.metrics")
class HystrixMetricsProperties{
   private boolean enabled = true;//是否启动指标轮询,默认开启 
   private Integer pollingIntervalMs = 2000;//轮询的间隔时间
}  

class HystrixCommandProperties{   
    //
    private final HystrixProperty<Integer> circuitBreakerRequestVolumeThreshold; 
    //熔断后多少毫秒允许重试,5000ms
    private final HystrixProperty<Integer> circuitBreakerSleepWindowInMilliseconds; 
    //是否开启断路器，true
    private final HystrixProperty<Boolean> circuitBreakerEnabled;                 
    //断路器阈值百分比，50%
    private final HystrixProperty<Integer> circuitBreakerErrorThresholdPercentage;
    //强制打开断路器，false
    private final HystrixProperty<Boolean> circuitBreakerForceOpen;  
    //强制关闭断路器，即允许所有的错误,false
    private final HystrixProperty<Boolean> circuitBreakerForceClosed; 
    //HystrixCommand是否在单线程执行，ExecutionIsolationStrategy.THREAD(THREAD/SEMAPHORE)
    private final HystrixProperty<ExecutionIsolationStrategy> executionIsolationStrategy;
    //命令超时毫秒数,1000ms 
    private final HystrixProperty<Integer> executionTimeoutInMilliseconds;
    //是否启用命令超时，true 
    private final HystrixProperty<Boolean> executionTimeoutEnabled;     
    //Command运行在哪个线程池（当ExecutionIsolationStrategy.THREAD时）, null
    private final HystrixProperty<String> executionIsolationThreadPoolKeyOverride; 
    //信号量的允许数量(当ExecutionIsolationStrategy.SEMAPHORE),10
    private final HystrixProperty<Integer> executionIsolationSemaphoreMaxConcurrentRequests;
    //后备信号量数量,10
    private final HystrixProperty<Integer> fallbackIsolationSemaphoreMaxConcurrentRequests;
    //是否允许降级，true
    private final HystrixProperty<Boolean> fallbackEnabled;  
    //执行隔离线程超时中断，true (ExecutionIsolationStrategy.THREAD)
    private final HystrixProperty<Boolean> executionIsolationThreadInterruptOnTimeout;    
    //执行隔离线程取消时中断 , false
    private final HystrixProperty<Boolean> executionIsolationThreadInterruptOnFutureCancel;
    //监控指标刷新的毫秒数，
    private final HystrixProperty<Integer> metricsRollingStatisticalWindowInMilliseconds; 
    //监控指标刷新的存储童数，10
    private final HystrixProperty<Integer> metricsRollingStatisticalWindowBuckets; 
    //是否启用监控，true
    private final HystrixProperty<Boolean> metricsRollingPercentileEnabled;  
    //  监控指标刷新的百分比
    private final HystrixProperty<Integer> metricsRollingPercentileWindowInMilliseconds;
}
```

3.Hystrix执行
---
HystrixCommand

threadPool:Hystrix线程池,有个ConcurrentHashMap<String, HystrixThreadPool>保存
threadPoolKey:
```java
class  HystrixCommand extends AbstractCommand<R> 
        implements HystrixExecutable<R>, HystrixInvokableInfo<R>, HystrixObservable<R>{
           
    //HystrixCommand构造方法
    protected HystrixCommand(Setter setter) {
        this(setter.groupKey, setter.commandKey, setter.threadPoolKey, null, null, setter.commandPropertiesDefaults, setter.threadPoolPropertiesDefaults, null, null, null, null, null);
    }
         
    //实际调用了父类的构造方法
    protected AbstractCommand(HystrixCommandGroupKey group, HystrixCommandKey key, HystrixThreadPoolKey threadPoolKey, HystrixCircuitBreaker circuitBreaker, HystrixThreadPool threadPool,
                HystrixCommandProperties.Setter commandPropertiesDefaults, HystrixThreadPoolProperties.Setter threadPoolPropertiesDefaults,
                HystrixCommandMetrics metrics, TryableSemaphore fallbackSemaphore, TryableSemaphore executionSemaphore,
                HystrixPropertiesStrategy propertiesStrategy, HystrixCommandExecutionHook executionHook) {
    
            this.commandGroup = initGroupKey(group);
            this.commandKey = initCommandKey(key, getClass());
            this.properties = initCommandProperties(this.commandKey, propertiesStrategy, commandPropertiesDefaults);
            this.threadPoolKey = initThreadPoolKey(threadPoolKey, this.commandGroup, this.properties.executionIsolationThreadPoolKeyOverride().get());
            this.metrics = initMetrics(metrics, this.commandGroup, this.threadPoolKey, this.commandKey, this.properties);
            this.circuitBreaker = initCircuitBreaker(this.properties.circuitBreakerEnabled().get(), circuitBreaker, this.commandGroup, this.commandKey, this.properties, this.metrics);
            this.threadPool = initThreadPool(threadPool, this.threadPoolKey, threadPoolPropertiesDefaults);
    
            //Strategies from plugins
            this.eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            this.concurrencyStrategy = HystrixPlugins.getInstance().getConcurrencyStrategy();
            HystrixMetricsPublisherFactory.createOrRetrievePublisherForCommand(this.commandKey, this.commandGroup, this.metrics, this.circuitBreaker, this.properties);
            this.executionHook = initExecutionHook(executionHook);
    
            this.requestCache = HystrixRequestCache.getInstance(this.commandKey, this.concurrencyStrategy);
            this.currentRequestLog = initRequestLog(this.properties.requestLogEnabled().get(), this.concurrencyStrategy);
    
            /* fallback semaphore override if applicable */
            this.fallbackSemaphoreOverride = fallbackSemaphore;
    
            /* execution semaphore override if applicable */
            this.executionSemaphoreOverride = executionSemaphore;
        }    
    
}
```  

HystrixCommand执行，execute()方法，queue()方法在，从队列中获取并异步执行，返回一个Future，
并get阻塞获取。Hystrix内使用了rxjava。
```java
class HystrixCommand{ 

    public R execute() {
         try {
             return queue().get();
         } catch (Exception e) {
             throw Exceptions.sneakyThrow(decomposeException(e));
         }
     } 

    public Future<R> queue() {
                           
            //阻塞获取结果
            final Future<R> delegate = toObservable().toBlocking().toFuture();
        	
            final Future<R> f = new Future<R>() {
    
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    if (delegate.isCancelled()) {
                        return false;
                    }
    
                    if (HystrixCommand.this.getProperties().executionIsolationThreadInterruptOnFutureCancel().get()) {
        
                        interruptOnFutureCancel.compareAndSet(false, mayInterruptIfRunning);
            		}
    
                    final boolean res = delegate.cancel(interruptOnFutureCancel.get());
    
                    if (!isExecutionComplete() && interruptOnFutureCancel.get()) {
                        final Thread t = executionThread.get();
                        if (t != null && !t.equals(Thread.currentThread())) {
                            t.interrupt();
                        }
                    }
    
                    return res;
    			}
    
                @Override
                public boolean isCancelled() {
                    return delegate.isCancelled();
    			}
    
                @Override
                public boolean isDone() {
                    return delegate.isDone();
    			}
    
                @Override
                public R get() throws InterruptedException, ExecutionException {
                    return delegate.get();
                }
    
                @Override
                public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return delegate.get(timeout, unit);
                }
            	
            };
    
            //成功返回返回值，失败则异常处理
            if (f.isDone()) {
                try {
                    f.get();
                    return f;
                } catch (Exception e) {
                    Throwable t = decomposeException(e);
                    if (t instanceof HystrixBadRequestException) {
                        return f;
                    } else if (t instanceof HystrixRuntimeException) {
                        HystrixRuntimeException hre = (HystrixRuntimeException) t;
                        switch (hre.getFailureType()) {
    					case COMMAND_EXCEPTION:
    					case TIMEOUT:
    						// we don't throw these types from queue() only from queue().get() as they are execution errors
    						return f;
    					default:
    						// these are errors we throw from queue() as they as rejection type errors
    						throw hre;
    					}
                    } else {
                        throw Exceptions.sneakyThrow(t);
                    }
                }
            }
    
            return f;
        }
} 
```  

Command执行状态:  NOT_STARTED(没有开始)/OBSERVABLE_CHAIN_CREATED(创建调用链)/
                USER_CODE_EXECUTED(用户代码执行)/UNSUBSCRIBED(取消订阅)/TERMINAL(执行结束)

Command执行过程:
1.NOT_STARTED->OBSERVABLE_CHAIN_CREATED:开始创建调用链

2.(requestLogEnabled控制是否开启请求日志)、(requestCacheEnabled是否开启缓存)

3.若没有缓存,  调用链添加applyHystrixSemantics(真正进行熔断)

4.applyHystrixSemantics执行，判断是否开启熔断，若开启则直接执行失败回调。

5.尝试获取信号量，若没有获取到，也执行失败逻辑。若超时、取消订阅，重置信号量。
每个Command都有信号量用来限制并发fallback，信号量次数默认10.

6.executeCommandAndObserve->construct() ,执行请求
```java
class AbstractCommand{
   public Observable<R> toObservable() {
           final AbstractCommand<R> _cmd = this;
   
           final Action0 terminateCommandCleanup = new Action0() {
   
               @Override
               public void call() {   
                    //将OBSERVABLE_CHAIN_CREATED->TERMINAL , 代表还没有执行就结束
                   if (_cmd.commandState.compareAndSet(CommandState.OBSERVABLE_CHAIN_CREATED, CommandState.TERMINAL)) {
                        //用户代码没有执行
                        handleCommandEnd(false);
                   } else if (_cmd.commandState.compareAndSet(CommandState.USER_CODE_EXECUTED, CommandState.TERMINAL)) {
                        //USER_CODE_EXECUTED-> TERMINAL , 代表用户代码已执行并结束了
                        handleCommandEnd(true); 
                   }
               }
           };
   
           //mark the command as CANCELLED and store the latency (in addition to standard cleanup)
           final Action0 unsubscribeCommandCleanup = new Action0() {
               @Override
               public void call() {
                   if (_cmd.commandState.compareAndSet(CommandState.OBSERVABLE_CHAIN_CREATED, CommandState.UNSUBSCRIBED)) {
                       if (!_cmd.executionResult.containsTerminalEvent()) {
                           _cmd.eventNotifier.markEvent(HystrixEventType.CANCELLED, _cmd.commandKey);
                           try {
                               executionHook.onUnsubscribe(_cmd);
                           } catch (Throwable hookEx) {
                               logger.warn("Error calling HystrixCommandExecutionHook.onUnsubscribe", hookEx);
                           }
                           _cmd.executionResultAtTimeOfCancellation = _cmd.executionResult
                                   .addEvent((int) (System.currentTimeMillis() - _cmd.commandStartTimestamp), HystrixEventType.CANCELLED);
                       }
                       handleCommandEnd(false); //user code never ran
                   } else if (_cmd.commandState.compareAndSet(CommandState.USER_CODE_EXECUTED, CommandState.UNSUBSCRIBED)) {
                       if (!_cmd.executionResult.containsTerminalEvent()) {
                           _cmd.eventNotifier.markEvent(HystrixEventType.CANCELLED, _cmd.commandKey);
                           try {
                               executionHook.onUnsubscribe(_cmd);
                           } catch (Throwable hookEx) {
                               logger.warn("Error calling HystrixCommandExecutionHook.onUnsubscribe", hookEx);
                           }
                           _cmd.executionResultAtTimeOfCancellation = _cmd.executionResult
                                   .addEvent((int) (System.currentTimeMillis() - _cmd.commandStartTimestamp), HystrixEventType.CANCELLED);
                       }
                       handleCommandEnd(true); //user code did run
                   }
               }
           };
                
           //调用链开始
           final Func0<Observable<R>> applyHystrixSemantics = new Func0<Observable<R>>() {
               @Override
               public Observable<R> call() {  
                   //若状态为不订阅，则不执行
                   if (commandState.get().equals(CommandState.UNSUBSCRIBED)) {
                       return Observable.never();
                   }
                   return applyHystrixSemantics(_cmd);
               }
           };
   
           final Func1<R, R> wrapWithAllOnNextHooks = new Func1<R, R>() {
               @Override
               public R call(R r) {
                   R afterFirstApplication = r;
   
                   try {
                       afterFirstApplication = executionHook.onComplete(_cmd, r);
                   } catch (Throwable hookEx) {
                       logger.warn("Error calling HystrixCommandExecutionHook.onComplete", hookEx);
                   }
   
                   try {
                       return executionHook.onEmit(_cmd, afterFirstApplication);
                   } catch (Throwable hookEx) {
                       logger.warn("Error calling HystrixCommandExecutionHook.onEmit", hookEx);
                       return afterFirstApplication;
                   }
               }
           };
   
           final Action0 fireOnCompletedHook = new Action0() {
               @Override
               public void call() {
                   try {
                       executionHook.onSuccess(_cmd);
                   } catch (Throwable hookEx) {
                       logger.warn("Error calling HystrixCommandExecutionHook.onSuccess", hookEx);
                   }
               }
           };
   
           return Observable.defer(new Func0<Observable<R>>() {
               @Override
               public Observable<R> call() {
                   //判断是否是NOT_STARTED,是的话更新为OBSERVABLE_CHAIN_CREATED，并开始创建调用链
                   if (!commandState.compareAndSet(CommandState.NOT_STARTED, CommandState.OBSERVABLE_CHAIN_CREATED)) {
                       IllegalStateException ex = new IllegalStateException("This instance can only be executed once. Please instantiate a new instance.");
                       throw new HystrixRuntimeException(FailureType.BAD_REQUEST_EXCEPTION, _cmd.getClass(), getLogMessagePrefix() + " command executed multiple times - this is not permitted.", ex, null);
                   }
   
                   commandStartTimestamp = System.currentTimeMillis();
   
                   if (properties.requestLogEnabled().get()) {
                       //记录请求日志
                       if (currentRequestLog != null) {
                           currentRequestLog.addExecutedCommand(_cmd);
                       }
                   }
   
                   final boolean requestCacheEnabled = isRequestCachingEnabled();
                   final String cacheKey = getCacheKey();
   
                    //从缓存中获取响应结果
                   if (requestCacheEnabled) {
                       HystrixCommandResponseFromCache<R> fromCache = (HystrixCommandResponseFromCache<R>) requestCache.get(cacheKey);
                       if (fromCache != null) {
                           isResponseFromCache = true;
                           return handleRequestCacheHitAndEmitValues(fromCache, _cmd);
                       }
                   }
   
                   Observable<R> hystrixObservable =
                           Observable.defer(applyHystrixSemantics)
                                   .map(wrapWithAllOnNextHooks);
   
                   Observable<R> afterCache;
   
                   // put in cache
                   if (requestCacheEnabled && cacheKey != null) {
                       // wrap it for caching
                       HystrixCachedObservable<R> toCache = HystrixCachedObservable.from(hystrixObservable, _cmd);
                       HystrixCommandResponseFromCache<R> fromCache = (HystrixCommandResponseFromCache<R>) requestCache.putIfAbsent(cacheKey, toCache);
                       if (fromCache != null) {
                           // another thread beat us so we'll use the cached value instead
                           toCache.unsubscribe();
                           isResponseFromCache = true;
                           return handleRequestCacheHitAndEmitValues(fromCache, _cmd);
                       } else {
                           // we just created an ObservableCommand so we cast and return it
                           afterCache = toCache.toObservable();
                       }
                   } else {
                       afterCache = hystrixObservable;
                   }
   
                   return afterCache
                           .doOnTerminate(terminateCommandCleanup)     // perform cleanup once (either on normal terminal state (this line), or unsubscribe (next line))
                           .doOnUnsubscribe(unsubscribeCommandCleanup) // perform cleanup once
                           .doOnCompleted(fireOnCompletedHook);
               }
           });
       }  

       private Observable<R> applyHystrixSemantics(final AbstractCommand<R> _cmd) {
                // mark that we're starting execution on the ExecutionHook
                // if this hook throws an exception, then a fast-fail occurs with no fallback.  No state is left inconsistent
                executionHook.onStart(_cmd);
        
                //判断是否开启熔断
                if (circuitBreaker.allowRequest()) {   
                    //获取信号量实例
                    final TryableSemaphore executionSemaphore = getExecutionSemaphore();
                    final AtomicBoolean semaphoreHasBeenReleased = new AtomicBoolean(false);
                    final Action0 singleSemaphoreRelease = new Action0() {
                        @Override
                        public void call() {
                            if (semaphoreHasBeenReleased.compareAndSet(false, true)) {
                                executionSemaphore.release();
                            }
                        }
                    };
        
                    final Action1<Throwable> markExceptionThrown = new Action1<Throwable>() {
                        @Override
                        public void call(Throwable t) {
                            eventNotifier.markEvent(HystrixEventType.EXCEPTION_THROWN, commandKey);
                        }
                    };
        
                    if (executionSemaphore.tryAcquire()) {
                        try {
                            /* used to track userThreadExecutionTime */
                            executionResult = executionResult.setInvocationStartTime(System.currentTimeMillis());
                            return executeCommandAndObserve(_cmd)   
                                     //错误逻辑
                                    .doOnError(markExceptionThrown) 
                                     //超时
                                    .doOnTerminate(singleSemaphoreRelease)  
                                    //取消订阅
                                    .doOnUnsubscribe(singleSemaphoreRelease);
                        } catch (RuntimeException e) {
                            return Observable.error(e);
                        }
                    } else {
                        return handleSemaphoreRejectionViaFallback();
                    }
                } else {
                    return handleShortCircuitViaFallback();
                }
            }
}
``` 



