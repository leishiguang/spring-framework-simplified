package simplified.spring.beans.support;

import simplified.spring.beans.config.BeanDefinition;
import simplified.spring.context.support.AbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 众多 IoC 容器子类的典型代表，存储注册信息的 BeanDefinition
 *
 * @author leishiguang
 * @since v1.0
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext {

	/**
	 * 存储注册信息的 BeanDefinition
	 */
	protected final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(6);

}
