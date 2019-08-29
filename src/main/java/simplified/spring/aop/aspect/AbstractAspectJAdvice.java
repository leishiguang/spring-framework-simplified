package simplified.spring.aop.aspect;

import lombok.AllArgsConstructor;
import simplified.spring.aop.JointPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 封装拦截器回调的通用逻辑，在此主要封装了反射动态调用方法
 *
 * @author leishiguang
 * @since v1.0
 */
@AllArgsConstructor
public class AbstractAspectJAdvice {

	private Method aspectMethod;
	private Object aspectTarget;

	/**
	 * 反射动态调用方法
	 */
	protected Object invokeAdviceMethod(JointPoint jointPoint, Object returnValue, Throwable ex) {
		Class<?>[] paramsTypes = aspectMethod.getParameterTypes();
		if (paramsTypes.length == 0) {
			try {
				return aspectMethod.invoke(aspectTarget);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("反射调用方法失败,method=" + aspectMethod.getName(), e);
			}
		}
		Object[] args = new Object[paramsTypes.length];
		for(int i = 0; i< paramsTypes.length; i++){
			if(paramsTypes[i] == JointPoint.class){
				args[i] = jointPoint;
			}else if(paramsTypes[i] == Throwable.class){
				args[i] = ex;
			}else if(paramsTypes[i] == Object.class){
				args[i] = returnValue;
			}
		}
		try {
			return this.aspectMethod.invoke(aspectTarget,args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("反射调用方法失败,method=" + aspectMethod.getName(), e);
		}

	}
}
