package simplified.spring.aop;

import lombok.Data;
import simplified.spring.aop.intercept.MethodInvocation;
import simplified.spring.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 使用 JDK Proxy API 生成代理类
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

	private AdvisedSupport config;

	public JdkDynamicAopProxy(AdvisedSupport config) {
		this.config = config;
	}

	/**
	 * 执行代理方法的关键
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args){
		//将每一个 JoinPoint 也就是被代理的业务方法（Method）封装成一个拦截器，组合成一个拦截器链
		List<Object> interceptorsAdnDynamicMethodMatchers =
				config.getInterceptorsAndDynamicInterceptionAdvice(method, this.config.getTargetClass());
		//交给拦截器链 MethodInvocation 的 proceed() 方法去执行
		MethodInvocation invocation = new MethodInvocation(proxy,this.config.getTarget(),
				method,args,this.config.getTargetClass(),interceptorsAdnDynamicMethodMatchers);
		return invocation.proceed();
	}

	/**
	 * 获得一个代理对象
	 *
	 * @return 代理对象
	 */
	@Override
	public Object getProxy() {
		return getProxy(this.config.getTargetClass().getClassLoader());
	}

	/**
	 * 通过自定义类加载器获得一个代理对象
	 *
	 * @param classLoader 类加载器
	 * @return 代理对象
	 */
	@Override
	public Object getProxy(ClassLoader classLoader) {
		return Proxy.newProxyInstance(classLoader, this.config.getTargetClass().getInterfaces(), this);
	}

}
