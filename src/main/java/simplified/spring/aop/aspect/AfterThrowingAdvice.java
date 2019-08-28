package simplified.spring.aop.aspect;

import lombok.Data;
import lombok.EqualsAndHashCode;
import simplified.spring.aop.intercept.MethodInterceptor;
import simplified.spring.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 异常通知具体实现
 *
 * @author leishiguang
 * @since v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AfterThrowingAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

	private String throwingName;
	private MethodInvocation mi;

	public AfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
		super(aspectMethod, aspectTarget);
	}

	@Override
	public Object invoke(MethodInvocation mi){
		try{
			return mi.proceed();
		}catch (Throwable ex){
			invokeAdviceMethod(mi,null,ex.getCause());
			throw ex;
		}
	}
}
