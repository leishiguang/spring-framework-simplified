package simplified.spring.context;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.annotation.Autowired;
import simplified.spring.annotation.Controller;
import simplified.spring.annotation.Service;
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
	private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(6);

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
				throw new RuntimeException("The '" + beanDefinition.getFactoryBeanName() + "' is exists!");
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
	 * DI，注入一个 bean
	 *
	 * @param beanName bean 名称
	 * @param instance bean 实例
	 */
	private void populateBean(String beanName, Object instance) {
		Class clazz = instance.getClass();
		if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
			return;
		}
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
		if (this.factoryBeanObjectCache.containsKey(className)) {
			instance = this.factoryBeanObjectCache.get(className);
		} else {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			assert clazz != null;
			try {
				instance = clazz.newInstance();
				this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("无法实例化bean:" + className, e);
			}
		}
		return instance;
	}
}
