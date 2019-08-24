package simplified.spring.context.support;

/**
 * IoC 容器实现的顶层设计
 *
 * @author leishiguang
 * @since v1.0
 */
public abstract class AbstractApplicationContext {

	/**
	 * 只提供给子类重写
	 */
	public void refresh(){}
}
