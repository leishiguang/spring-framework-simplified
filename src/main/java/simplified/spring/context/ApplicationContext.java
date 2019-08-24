package simplified.spring.context;

import simplified.spring.beans.BeanWrapper;
import simplified.spring.beans.config.BeanDefinition;
import simplified.spring.beans.support.BeanDefinitionReader;
import simplified.spring.beans.support.DefaultListableBeanFactory;
import simplified.spring.core.BeanFactory;

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
public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

	private String[] configLocations;
	private BeanDefinitionReader reader;

	/**
	 * 单例的 IoC 容器缓存
	 */
	private Map<String,Object> factoryBeanObjectCache = new ConcurrentHashMap<>(6);

	/**
	 * 通用的 IoC 容器
	 */
	private Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>(6);


	public ApplicationContext(String... configLocations){
		this.configLocations = configLocations;
		refresh();
	}

	/**
	 * 只提供给子类重写
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
	 *
	 * 装饰器模式：
	 * 1、保留原来的 OOP 关系
	 * 2、需要对它进行阔暗战、增强（为以后的 AOP 打基础）
	 *
	 * @param beanName beanName
	 * @return 实例 bean
	 */
	@Override
	public Object getBean(String beanName) {
		return null;
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

	public String[] getBeanDefinitionNames(){
		return this.beanDefinitionMap.keySet().toArray(new String[0]);
	}

	public int getBeanDefinitionCount(){
		return this.beanDefinitionMap.size();
	}

	public Properties getConfig(){
		return this.reader.getConfig();
	}


	/**
	 * 注册，把配置信息放到容器里面（伪 IoC 容器）
	 */
	private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions){
		for (BeanDefinition beanDefinition :
				beanDefinitions) {
			if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
				throw new RuntimeException("The '" + beanDefinition.getFactoryBeanName() + "' is exists!");
			}
			super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
		}
	}

	/**
	 * 初始化非延时加载的类
	 */
	private void doAutowired(){
		for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
			String beanName = beanDefinitionEntry.getKey();
			if(!beanDefinitionEntry.getValue().isLazyInit()){
				getBean(beanName);
			}
		}
	}
}
