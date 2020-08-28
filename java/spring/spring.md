# Spring

## 1.Spring bean的生命周期

### 1-1.bean的初始化和销毁

```java
//初始化bean时执行（@PostConstruct）
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
//销毁bean时执行（@PreDestroy）
public interface DisposableBean {
	void destroy() throws Exception;
}
```

### 1-2.Aware接口

-  **BeanNameAware** ： 获得Bean在配置文件中定义的名字。
-  **ResourceLoaderAware** : 获得ResourceLoader对象，可以获得classpath中某个文件。
- **BeanClassLoaderAware** ： 获得ClassLoader对象
-  **BeanFactoryAware**:获得BeanFactory对象，可以用来检测Bean的作用域 

### 1-3.bean的作用域

-  **singleton**：单例（默认）
-  **prototype**  ：每次请求都会创建一个
-  **request**  ： 每一次HTTP请求都会产生一个新的bean ,该bean仅在当前HTTP request内有效。 
-  **session**  : 每一次HTTP请求都会产生一个新的 bean，该bean仅在当前 HTTP session 内有效。 
-  **global-session**：  全局session作用域 

### 1-4.bean的生命周期

1.  Bean容器找到配置文件中Spring Bean的定义。 
2.  Bean容器利用Java Reflection API创建一个Bean的实例 。（创建对象）
3. 设置属性值 （设置属性）
4.  如果Bean实现了BeanNameAware接口，调用setBeanName()方法，传入Bean的名字。 
5.  如果Bean实现了BeanClassLoaderAware接口，调用setBeanClassLoader()方法，传入ClassLoader对象的实例 
6.  如果Bean实现了BeanFactoryAware接口，调用setBeanClassLoader()方法，传入ClassLoader对象的实例。 
7.  如果有和加载这个Bean的Spring容器相关的BeanPostProcessor对象，执行postProcessBeforeInitialization()方法 
8.  如果Bean实现了InitializingBean接口，执行afterPropertiesSet()方法。 
9.  如果Bean在配置文件中的定义包含`init-method`属性，执行指定的方法。
10.  如果有和加载这个Bean的Spring容器相关的BeanPostProcessor对象，执行postProcessAfterInitialization()方法 （初始化完成）
11.  当要销毁Bean的时候，如果Bean实现了DisposableBean接口，执行destroy()方法。 
12.  当要销毁Bean的时候，如果Bean在配置文件中的定义包含`destroy-method`属性，执行指定的方法

### 1-5.bean的三级缓存（循环依赖）

- **一级缓存**：beanName — 初始化成功的bean
- **二级缓存**：beanName — 提前暴露的对象
- **三级缓存**：beanName — beanFactory

#### 循环依赖解决过程：

1.  创建一个新的BeanA对象( A.createBeanInstance方法)
2.  将BeanA与BeanFactory存入三级缓存
3.  注入A的属性（A.populateBean方法）
4. 属性中包含B（B.createBeanInstance）
5. 注入B的属性（B.populateBean方法）
6. B的属性中包含A，用过 getSingleton 方法，获取到三级缓存的factory
7. 调用factory的 getEarlyBeanReference（factory.getObject） 方法得到 暴露对象，并存入二级缓存
8. B的属性注入完，调用 initializeBean ，初始化B完成
9. A注入属性B，调用 initializeBean ，初始化A完成

```java
//一级缓存
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
//二级缓存
private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
//三级缓存
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {
	
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
			throws BeanCreationException {

		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
            //创建bean对象
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		final Object bean = instanceWrapper.getWrappedInstance();
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription();
				}
				mbd.postProcessed = true;
			}
		}

		//判断是否循环依赖
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
				isSingletonCurrentlyInCreation(beanName));
		if (earlySingletonExposure) {
			if (logger.isTraceEnabled()) {
				logger.trace("Eagerly caching bean '"");
			}
            //添加三级缓存
			addSingletonFactory(beanName, 
                                () -> getEarlyBeanReference(beanName, mbd, bean));
		}

		Object exposedObject = bean;
		try {
            //注入属性
			populateBean(beanName, mbd, instanceWrapper);
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException 
                && beanName.equals(((BeanCreationException) ex).getBeanName())) {
				throw (BeanCreationException) ex;
			}
			else {
				throw new BeanCreationException();
			}
		}

         //如果存在循环依赖
		if (earlySingletonExposure) {
            //获取bean
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping 
                         && hasDependentBean(beanName)) {
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans 
                        = new LinkedHashSet<>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName);
					}
				}
			}
		}

		try {
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException();
		}

		return exposedObject;
	}
    
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
		//确保此时确实解决了bean类。
		Class<?> beanClass = resolveBeanClass(mbd, beanName);

		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) 
            && !mbd.isNonPublicAccessAllowed()) {
            //非public修饰不能创建
			throw new BeanCreationException(mbd.getResourceDescription(), beanName);
		}

		Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
		if (instanceSupplier != null) {
			return obtainFromSupplier(instanceSupplier, beanName);
		}

		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		//重新创建相同bean时的快捷方式
		boolean resolved = false;
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}
      
        //是否创建过
		if (resolved) {
            //判断是否是构造器
			if (autowireNecessary) {
				return autowireConstructor(beanName, mbd, null, null);
			}
			else {
				return instantiateBean(beanName, mbd);
			}
		}

		//自动装配的候选构造函数
		Constructor<?>[] ctors 
            = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		//默认构造的首选构造函数
		ctors = mbd.getPreferredConstructors();
		if (ctors != null) {
			return autowireConstructor(beanName, mbd, ctors, null);
		}

		// No special handling: simply use no-arg constructor.
		return instantiateBean(beanName, mbd);
	}
    
     protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        //一级缓存中获取
		Object singletonObject = this.singletonObjects.get(beanName);
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			synchronized (this.singletonObjects) {
                //一级缓存中没有从二级缓存中获取
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
                    //二级缓存没有，通过三级缓存的Factory生成，并存入二级缓存
					ObjectFactory<?> singletonFactory 
                        = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						singletonObject = singletonFactory.getObject();
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return singletonObject;
	}
}
```



## 2.Spring IOC

#### 2-1. Bean的配置

- **xml配置:** XmlBeanDefinitionReader

- **Properties配置** PropertiesBeanDefinitionReader

  

#### 2-2.BeanDefinition

BeanDefinition抽象了用户对Bean的定义

```java
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
	
}
```



#### 2-3.BeanDefinitionRegistry

BeanDefinitionRegistry注册器基于id和name保存BeanDefinition(bean定义的信息)，Alias为别名。

```java
public interface BeanDefinitionRegistry extends AliasRegistry {
    
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
    
    void registerAlias(String name, String alias);
}
```



#### 2-4. BeanDefinitionReader

**加载bean的过程：**

1. 创建一个注册器(BeanDefinitionRegistry)
2. 创建bean定义读取器(BeanDefinitionReader)

```java
//创建一个简单注册器
BeanDefinitionRegistry register = new SimpleBeanDefinitionRegistry();
//创建bean定义读取器
BeanDefinitionReader reader = new XmlBeanDefinitionReader(register);
// 创建资源读取器
DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
// 获取资源
Resource xmlResource = resourceLoader.getResource("spring.xml");
// 装载Bean的定义
reader.loadBeanDefinitions(xmlResource);

```



### 3. Springboot的启动过程

1. **构建SpringApplication:**

   SpringApplication.run(Main.class, args);

   ```java
   public class SpringApplication {
       
       public static ConfigurableApplicationContext run(
           Class<?>[] primarySources, String[] args) {
   		return new SpringApplication(primarySources).run(args);
   	}
       
       public SpringApplication(
           ResourceLoader resourceLoader, Class<?>... primarySources) {
           //资源加载
   		this.resourceLoader = resourceLoader;
           //启动类
   		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
           //判断web服务类型(DispatcherServlet类不同)
   		this.webApplicationType = WebApplicationType.deduceFromClasspath();
           //初始化
   		setInitializers((Collection) getSpringFactoriesInstances(
               ApplicationContextInitializer.class));
           //监听初始化
   		setListeners((Collection) getSpringFactoriesInstances(
               ApplicationListener.class));
           //当前main方法的类型
   		this.mainApplicationClass = deduceMainApplicationClass();
   	}
   }
   
   public enum WebApplicationType {
       NONE,//不以Web应用程序运行
       SERVLET,//基于servlet的Web应用程序
       REACTIVE;//REACTIVE的web应用程序
   }
   ```

2. **初始化ApplicationContextInitializer/ApplicationListener：**

   ```java
   public class SpringApplication {
   
       private <T> Collection<T> getSpringFactoriesInstances(
           Class<T> type, Class<?>[] parameterTypes, Object... args) {
           //获取classload
   		ClassLoader classLoader = getClassLoader();
   		//classload加载ApplicationContextInitializer
   		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader
                                                   .loadFactoryNames(type, classLoader));
   		List<T> instances = createSpringFactoriesInstances
               (type, parameterTypes, classLoader, args, names);
   		AnnotationAwareOrderComparator.sort(instances);
   		return instances;
   	}
       
   }
   ```

3. **运行SpringApplication:**

   1. **StopWatch：** 记录启动时间(startTimeNanos/totalTimeNanos)

   2. **SpringApplicationRunListener**： SpringApplication初始化时的监听

      -  **starting:**  初始化开始前
      -  **environmentPrepared:** 环境初始化前
      -  **contextPrepared:** ApplicationContext初始化前
      -  **contextLoaded:**  ApplicationContext初始化后
      -  **started:**  SpringApplication开始启动
      -  **running:**  SpringApplication开始运行
      -  **failed:** SpringApplication启动失败

   3. **初始化环境:**

       **activeProfiles:**  配置文件列表

       **defaultProfiles:** 默认配置文件(default)

   4. 

   ```java
   public class SpringApplication {
   	
       public ConfigurableApplicationContext run(String... args) {
   		StopWatch stopWatch = new StopWatch();
   		stopWatch.start();
   		ConfigurableApplicationContext context = null;
   		Collection<SpringBootExceptionReporter> exceptionReporters 
               = new ArrayList<>();
           //java.awt.headless:  缺少显示设备、键盘或鼠标模式
   		configureHeadlessProperty();
           //初始化运行时间监听
   		SpringApplicationRunListeners listeners = getRunListeners(args);
   		listeners.starting();
   		try {
   			ApplicationArguments applicationArguments 
                   = new DefaultApplicationArguments(args);
               //初始化环境
   			ConfigurableEnvironment environment 
                   = prepareEnvironment(listeners, applicationArguments);
               
   			configureIgnoreBeanInfo(environment);
               //打印banner图
   			Banner printedBanner = printBanner(environment);
   			context = createApplicationContext();
   			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
   					new Class[] { ConfigurableApplicationContext.class }, context);
   			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
   			refreshContext(context);
   			afterRefresh(context, applicationArguments);
   			stopWatch.stop();
   			if (this.logStartupInfo) {
   				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
   			}
   			listeners.started(context);
   			callRunners(context, applicationArguments);
   		}
   		catch (Throwable ex) {
   			handleRunFailure(context, ex, exceptionReporters, listeners);
   			throw new IllegalStateException(ex);
   		}
   
   		try {
   			listeners.running(context);
   		}
   		catch (Throwable ex) {
   			handleRunFailure(context, ex, exceptionReporters, null);
   			throw new IllegalStateException(ex);
   		}
   		return context;
   	}
       
   }
   ```

   