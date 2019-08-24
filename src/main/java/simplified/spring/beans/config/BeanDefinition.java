package simplified.spring.beans.config;

import lombok.Data;

/**
 * 用于保存 Bean 相关配置信息
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class BeanDefinition {
	/**
	 * 原生 Bean 的全类名
	 */
	private String beanClassName;

	/**
	 * 标记是否延时加载
	 */
	private boolean lazyInit = false;

	/**
	 * 保存 beanName，在 IoC 容器中存储的 key
	 */
	private String factoryBeanName;

}
