package simplified.spring.aop;

import simplified.spring.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 使用 Cglib API 生成代理类，暂未实现
 *
 * @author leishiguang
 * @since v1.0
 */
public class CglibAopProxy implements AopProxy, InvocationHandler {

	private AdvisedSupport config;

	public CglibAopProxy(AdvisedSupport config) {
		this.config = config;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		throw new UnsupportedOperationException("暂未实现 Cglib 代理");
	}

	/**
	 * 获得一个代理对象
	 *
	 * @return 代理对象
	 */
	@Override
	public Object getProxy() {
		throw new UnsupportedOperationException("暂未实现 Cglib 代理");
	}

	/**
	 * 通过自定义类加载器获得一个代理对象
	 *
	 * @param classLoader 类加载器
	 * @return 代理对象
	 */
	@Override
	public Object getProxy(ClassLoader classLoader) {
		throw new UnsupportedOperationException("暂未实现 Cglib 代理");
	}


}
