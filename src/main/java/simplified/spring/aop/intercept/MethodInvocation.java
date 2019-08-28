package simplified.spring.aop.intercept;

import lombok.Data;
import simplified.spring.aop.JointPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行拦截器链，相当于 Spring 中 ReflectiveMethodInvocation 的功能
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class MethodInvocation implements JointPoint {

	/**
	 * 代理对象
	 */
	private Object proxy;

	/**
	 * 代理的目标对象
	 */
	private Object target;

	/**
	 * 代理的目标方法
	 */
	private Method method;

	/**
	 * 所代理方法的实参列表
	 */
	private Object[] arguments;

	/**
	 * 代理的目标类
	 */
	private Class<?> targetClass;

	/**
	 * 回调方法链
	 */
	private List<Object> interceptorsAndDynamicMethodMatchers;

	private Map<String ,Object> userAttributes;

	private int currentInterceptorIndex = -1;

	public MethodInvocation(Object proxy, Object target, Method method, Object[] args
			, Class targetClass, List<Object> interceptorsAdnDynamicMethodMatchers) {
		this.proxy = proxy;
		this.target = target;
		this.method = method;
		this.arguments = args;
		this.targetClass = targetClass;
		this.interceptorsAndDynamicMethodMatchers = interceptorsAdnDynamicMethodMatchers;
		this.userAttributes = new HashMap<>(6);
	}


	/**
	 * 该方法所属的实例对象
	 *
	 * @return Object
	 */
	@Override
	public Object getThis() {
		return this.target;
	}

	/**
	 * 在 JointPoint 中添加自定义属性
	 *
	 * @param key   key
	 * @param value value
	 */
	@Override
	public void setUserAttribute(String key, Object value) {
		if(value != null){
			this.userAttributes.put(key,value);
		}else{
			this.userAttributes.remove(key);
		}
	}

	/**
	 * 从已添加的自定义属性中，获取值
	 *
	 * @param key key
	 * @return value
	 */
	@Override
	public Object getUserAttribute(String key) {
		return this.userAttributes.get(key);
	}

	public Object proceed(){
		//如果 Interceptor 执行完了，则执行 jointPoint
		if(this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1){
			try {
				return this.method.invoke(this.target,this.arguments);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("反射调用方法失败",e);
			}
		}
		Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++ this.currentInterceptorIndex);
		//如果要动态匹配 jointPoint
		if(interceptorOrInterceptionAdvice instanceof MethodInterceptor){
			MethodInterceptor mi = (MethodInterceptor) interceptorOrInterceptionAdvice;
			return mi.invoke(this);
		}
		return proceed();
	}
}
