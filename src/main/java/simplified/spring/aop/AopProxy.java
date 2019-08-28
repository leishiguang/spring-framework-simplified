package simplified.spring.aop;

/**
 * 代理工厂的顶层接口
 * 提供获取代理对象的顶层入口
 * 默认就用 JDK 动态代理
 *
 * @author leishiguang
 * @since v1.0
 */
public interface AopProxy {
	/**
	 * 获得一个代理对象
	 * @return 代理对象
	 */
	Object getProxy();

	/**
	 * 通过自定义类加载器获得一个代理对象
	 * @param classLoader 类加载器
	 * @return 代理对象
	 */
	Object getProxy(ClassLoader classLoader);
}
