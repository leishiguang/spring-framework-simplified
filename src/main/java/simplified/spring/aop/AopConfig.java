package simplified.spring.aop;

import lombok.Data;

/**
 * AOP 配置封装，一遍在之后的代码中相互传递
 * 与 application.properties 配置文件中的一一对应
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class AopConfig {

	/**
	 * 切面表达式
	 */
	private String pointCut;

	/**
	 * 前置通知方法名
	 */
	private String aspectBefore;

	/**
	 * 后置通知方法名
	 */
	private String aspectAfter;

	/**
	 * 要织入的切面类
	 */
	private String aspectClass;

	/**
	 * 异常通知方法名
	 */
	private String aspectAfterThrow;

	/**
	 * 需要通知的异常类型
	 */
	private String aspectAfterThrowingName;
}
