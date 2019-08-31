package simplified.spring.jdbc;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.annotation.Aspect;
import simplified.spring.aop.JointPoint;
import simplified.spring.aop.annotation.After;
import simplified.spring.aop.annotation.AfterThrowing;
import simplified.spring.aop.annotation.Before;
import simplified.spring.aop.annotation.Pointcut;

import java.util.Arrays;

/**
 * 事务的切面
 *
 * @author leishiguang
 * @since v1.0
 */
@Aspect
@Slf4j
public class TransactionalAspect {

	@Pointcut("@annotation(com.supermap.annotation.CheckJmsLogAble)")
	public void pointCut() {
	}

	@Before("pointCut()")
	public void before(JointPoint jointPoint) {
		jointPoint.setUserAttribute("startTime_" + jointPoint.getMethod().getName(), System.currentTimeMillis());
		log.info("Invoker Before Method!!!" + getSimpleName(jointPoint) + "," + Arrays.toString(jointPoint.getArguments()));
	}

	@After("pointCut()")
	public void after(JointPoint jointPoint) {
		log.info("Invoker After Method!!!" + getSimpleName(jointPoint) + "," + Arrays.toString(jointPoint.getArguments()));
		long startTime = (Long) jointPoint.getUserAttribute("startTime_" + jointPoint.getMethod().getName());
		long endTime = System.currentTimeMillis();
		log.info("start Time : " + startTime + " , use time : " + (endTime - startTime));
	}

	@AfterThrowing("pointCut()")
	public void afterThrowing(JointPoint jointPoint, Throwable ex) {
		log.error("出现异常！" + getSimpleName(jointPoint) + "," + Arrays.toString(jointPoint.getArguments()) + ",Message:" + ex.getMessage());
	}

	private String getSimpleName(JointPoint jointPoint) {
		return jointPoint.getThis().getClass().getSimpleName() + "." + jointPoint.getMethod().getName();
	}

}
