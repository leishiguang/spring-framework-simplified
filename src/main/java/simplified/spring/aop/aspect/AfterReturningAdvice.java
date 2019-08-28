package simplified.spring.aop.aspect;

import lombok.Data;
import lombok.EqualsAndHashCode;
import simplified.spring.aop.JointPoint;
import simplified.spring.aop.intercept.MethodInterceptor;
import simplified.spring.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 后置通知具体实现
 *
 * @author leishiguang
 * @since v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AfterReturningAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

	private JointPoint jointPoint;

	public AfterReturningAdvice(Method aspectMethod, Object aspectTarget) {
		super(aspectMethod, aspectTarget);
	}

	@Override
	public Object invoke(MethodInvocation mi){
		Object returnValue = mi.proceed();
		this.jointPoint = mi;
		this.afterReturning(returnValue,mi.getMethod(),mi.getArguments(),mi.getThis());
		return returnValue;
	}

	public void afterReturning (Object returnValue, Method method, Object[] args, Object target) {
		invokeAdviceMethod(this.jointPoint,returnValue,null);
	}
}
