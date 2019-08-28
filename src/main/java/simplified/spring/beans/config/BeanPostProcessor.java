package simplified.spring.beans.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 为对象初始化事件设置一种回调机制，这儿只做说明
 *
 * @author leishiguang
 * @since v1.0
 */
@Slf4j
public class BeanPostProcessor {

	/**
	 * 为 Bean 的初始化之前提供回调入口
	 */
	public void postProcessBeforeInitialization(Object bean, String beanName) {
		log.info("开始加载bean [" + beanName + "] " + bean.getClass());
	}

	/**
	 * 为 Bean 的初始化之后提供回调入口
	 */
	public void postProcessAfterInitialization(Object bean, String beanName) {
		log.info("完成加载bean [" + beanName + "] " + bean.getClass());
	}
}
