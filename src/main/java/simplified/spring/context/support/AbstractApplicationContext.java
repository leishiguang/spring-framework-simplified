package simplified.spring.context.support;

/**
 * IoC 容器实现的顶层设计
 *
 * @author leishiguang
 * @since v1.0
 */
public abstract class AbstractApplicationContext {

	/**
	 * 初始化 IoC 容器，由子类进行重写
	 */
	public void refresh(){}
}
