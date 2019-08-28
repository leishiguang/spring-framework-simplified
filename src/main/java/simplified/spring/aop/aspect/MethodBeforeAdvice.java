package simplified.spring.aop.aspect;

import lombok.Data;
import lombok.EqualsAndHashCode;
import simplified.spring.aop.JointPoint;
import simplified.spring.aop.intercept.MethodInterceptor;
import simplified.spring.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 前置通知具体实现
 *
 * @author leishiguang
 * @since v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MethodBeforeAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

	private JointPoint jointPoint;

	public MethodBeforeAdvice(Method aspectMethod, Object aspectTarget) {
		super(aspectMethod, aspectTarget);
	}

	@Override
	public Object invoke(MethodInvocation mi){
		this.jointPoint = mi;
		this.before(mi.getMethod(),mi.getArguments(),mi.getThis());
		return mi.proceed();
	}

	public void before(Method method, Object[] args, Object target) {
		invokeAdviceMethod(this.jointPoint,null,null);
	}
}
