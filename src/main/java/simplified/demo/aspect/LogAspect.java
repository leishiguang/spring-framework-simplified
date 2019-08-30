package simplified.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.aop.JointPoint;

import java.util.Arrays;

/**
 * 定义一个织入的切面逻辑，也就是要针对目标代理对象增强的逻辑
 * 本类主要是完成对方法调用的监控，监听目标方法每次执行所消耗的时间
 * 这个类以配置的方式进行了注册，配置见 application.properties
 *
 * @author leishiguang
 * @since v1.0
 */
@Slf4j
public class LogAspect {

	public void before(JointPoint jointPoint) {
		jointPoint.setUserAttribute("startTime_" + jointPoint.getMethod().getName(), System.currentTimeMillis());
		log.info("Invoker Before Method!!!" + getSimpleName(jointPoint) + "," + Arrays.toString(jointPoint.getArguments()));
	}

	public void after(JointPoint jointPoint) {
		log.info("Invoker After Method!!!" + getSimpleName(jointPoint) + "," + Arrays.toString(jointPoint.getArguments()));
		long startTime = (Long) jointPoint.getUserAttribute("startTime_" + jointPoint.getMethod().getName());
		long endTime = System.currentTimeMillis();
		log.info("start Time : " + startTime + " , use time : " + (endTime - startTime));
	}

	public void afterThrowing(JointPoint jointPoint, Throwable ex) {
		log.error("出现异常！" + getSimpleName(jointPoint) + "," + Arrays.toString(jointPoint.getArguments()) + ",Message:" + ex.getMessage());
	}

	private String getSimpleName(JointPoint jointPoint) {
		return jointPoint.getThis().getClass().getSimpleName() + "." + jointPoint.getMethod().getName();
	}

}
