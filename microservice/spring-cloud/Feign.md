Feign
===

启动
--
 实现 ImportBeanDefinitionRegistrar，spring启动时加载
 配置顺序 clients->value  scan包下下所有配置@FeignClint的类并注入
 ```$java
final Class<?>[] clients = attrs == null ? null
				: (Class<?>[]) attrs.get("clients");
```     

