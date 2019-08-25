package simplified.spring.beans.config;

/**
 * 为对象初始化事件设置一种回调机制，这儿只做说明
 *
 * @author leishiguang
 * @since v1.0
 */
public class BeanPostProcessor {

	/**
	 * 为 Bean 的初始化之前提供回调入口
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName){
		return bean;
	}

	/**
	 * 为 Bean 的初始化之后提供回调入口
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName){
		return bean;
	}
}
