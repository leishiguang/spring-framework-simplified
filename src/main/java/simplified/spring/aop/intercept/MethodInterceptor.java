package simplified.spring.aop.intercept;

/**
 * 方法拦截器顶层接口
 *
 * @author leishiguang
 * @since v1.0
 */
public interface MethodInterceptor {

	Object invoke(MethodInvocation mi);
}
