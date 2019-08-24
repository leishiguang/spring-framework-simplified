package simplified.spring.core;

/**
 * 单例工厂的顶层设计
 *
 * @author leishiguang
 * @since v1.0
 */
public interface BeanFactory {

	/**
	 * 根据 beanName 从 IoC 容器中获得一个实例 Bean
	 * @param beanName beanName
	 * @return 实例 bean
	 */
	Object getBean(String beanName);

	/**
	 * 根据 beanClass 从 IoC 容器中获得一个实例 Bean
	 * @param beanClass beanClass
	 * @return 实例 bean
	 */
	Object getBean(Class<?> beanClass);
}
