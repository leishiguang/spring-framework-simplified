package simplified.spring.context;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.annotation.Autowired;
import simplified.spring.aop.AopConfig;
import simplified.spring.aop.AopProxy;
import simplified.spring.aop.CglibAopProxy;
import simplified.spring.aop.JdkDynamicAopProxy;
import simplified.spring.aop.support.AdvisedSupport;
import simplified.spring.beans.BeanWrapper;
import simplified.spring.beans.config.BeanDefinition;
import simplified.spring.beans.config.BeanPostProcessor;
import simplified.spring.beans.support.BeanDefinitionReader;
import simplified.spring.beans.support.DefaultListableBeanFactory;
import simplified.spring.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 直接接触用户的入口，完成 IoC、DI、AOP 的衔接
 *
 * @author leishiguang
 * @since v1.0
 */
@Slf4j
public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

	/**
	 * 保存配置文件目录
	 */
	private String[] configLocations;

	/**
	 * 保存读取之后的配置信息
	 */
	private BeanDefinitionReader reader;

	/**
	 * 单例的 IoC 容器缓存
	 */
	private Map<String, Object> singletonBeanObjectCache = new ConcurrentHashMap<>(6);

	/**
	 * 通用的 IoC 容器，用来存储被代理过的对象，即 BeanWrapper 封装之后的 bean 实例化方法
	 */
	private Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>(6);


	public ApplicationContext(String... configLocations) {
		this.configLocations = configLocations;
		refresh();
	}

	/**
	 * 初始化 IoC 容器
	 */
	@Override
	public void refresh() {
		//1.定位配置文件
		reader = new BeanDefinitionReader(this.configLocations);
		//2.加载配置文件，扫描相关类，把它们封装成 BeanDefinition
		List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
		//3.注册，把配置信息放到容器里面（伪 IoC 容器）
		doRegisterBeanDefinition(beanDefinitions);
		//4.把不是延时加载的类提前初始化
		doAutowired();
	}


	/**
	 * 根据 beanName 从 IoC 容器中获得一个实例 Bean
	 * 依赖注入，从这里开始，读取 BeanDefinition 中的信息，然后通过反射机制创建一个实例并返回
	 * spring 的做法是，不会把最原始的对象放出去，会用一个 BeanWrapper 来进行一次包装；
	 * <p>
	 * 装饰器模式：
	 * 1、保留原来的 OOP 关系
	 * 2、需要对它进行扩展、增强（为以后的 AOP 打基础）
	 *
	 * @param beanName beanName
	 * @return 实例 bean
	 */
	@Override
	public Object getBean(String beanName) {
		//如果是第一次初始化
		if (this.factoryBeanInstanceCache.get(beanName) == null) {
			BeanDefinition beanDefinition = super.beanDefinitionMap.get(beanName);
			//准备好通知事件
			BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
			//获取bean构造函数
			Object instance = instantiateBean(beanDefinition);
			if (null == instance) {
				throw new NullPointerException("无法从配置信息生成bean实例:" + beanName);
			}
			//在实例化之前，调用一次回调
			beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
			//封装beanWrapper
			BeanWrapper beanWrapper = new BeanWrapper(instance);
			this.factoryBeanInstanceCache.put(beanName, beanWrapper);
			//在实例化之后，调用一次回调
			beanPostProcessor.postProcessAfterInitialization(instance, beanName);
			//执行DI注入
			populateBean(beanName, instance);
		}
		//通过这样的代理调用，相当于留有了可以操作的空间
		return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();

	}

	/**
	 * 根据 beanClass 从 IoC 容器中获得一个实例 Bean
	 *
	 * @param beanClass beanClass
	 * @return 实例 bean
	 */
	@Override
	public Object getBean(Class<?> beanClass) {
		return getBean(beanClass.getName());
	}

	public String[] getBeanDefinitionNames() {
		return this.beanDefinitionMap.keySet().toArray(new String[0]);
	}

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public Properties getConfig() {
		return this.reader.getConfig();
	}


	/**
	 * 注册，把配置信息放到容器里面（伪 IoC 容器）
	 */
	private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) {
		for (BeanDefinition beanDefinition : beanDefinitions) {
			if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
				log.error("The '" + beanDefinition.getFactoryBeanName() + "' is exists!");
			}
			super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
		}
	}

	/**
	 * 初始化非延时加载的类
	 */
	private void doAutowired() {
		for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
			String beanName = beanDefinitionEntry.getKey();
			if (!beanDefinitionEntry.getValue().isLazyInit()) {
				try {
					getBean(beanName);
				} catch (Exception e) {
					log.error("未完成 bean 的初始化:" + beanName, e);
				}
			}
		}
	}

	/**
	 * DI，为 bean 中的 Autowired 进行注入
	 *
	 * @param beanName bean 名称
	 * @param instance bean 实例
	 */
	private void populateBean(String beanName, Object instance) {
		Class clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(Autowired.class)) {
				continue;
			}
			Autowired autowired = field.getAnnotation(Autowired.class);
			String autowiredBeanName = autowired.value().trim();
			if ("".equals(autowiredBeanName)) {
				autowiredBeanName = field.getType().getName();
			}
			field.setAccessible(true);
			try {
				field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 传一个 BeanDefinition，就返回一个实例 Bean 构造函数
	 *
	 * @param beanDefinition Bean 相关配置信息
	 * @return 实例 Bean
	 */
	private Object instantiateBean(BeanDefinition beanDefinition) {
		Object instance;
		String className = beanDefinition.getBeanClassName();
		if (this.singletonBeanObjectCache.containsKey(className)) {
			instance = this.singletonBeanObjectCache.get(className);
		} else {
			Class<?> clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("无法找到类:" + className, e);
			}
			try {
				instance = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("无法实例化bean:" + className, e);
			}
			//增加AOP的支持，这儿只实现了使用配置方式，加入一个切面，暂不支持加入多个
			AdvisedSupport aopConfig = instantiateAopConfig(beanDefinition);
			aopConfig.setTarget(instance);
			aopConfig.setTargetClass(clazz);
			//如果当前bean匹配了该切面，则生成当前bean的代理
			if(aopConfig.pointCutMatch()){
				instance = createProxy(aopConfig).getProxy();
			}
			this.singletonBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);

		}
		return instance;
	}

	/**
	 * 从配置文件中获取要切入与切入方法信息
	 */
	private AdvisedSupport instantiateAopConfig(BeanDefinition beanDefinition) {
		AopConfig config = new AopConfig();
		config.setPointCut(reader.getConfig().getProperty("pointCut"));
		config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
		config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
		config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
		config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
		config.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));
		return new AdvisedSupport(config);
	}

	/**
	 * 这儿可以根据是否是接口，来创建不同的代理
	 */
	private AopProxy createProxy(AdvisedSupport aopConfig) {
		Class targetClass = aopConfig.getTargetClass();
		if(targetClass.getInterfaces().length > 0){
			return new JdkDynamicAopProxy(aopConfig);
		}
		return new CglibAopProxy(aopConfig);
	}
}
