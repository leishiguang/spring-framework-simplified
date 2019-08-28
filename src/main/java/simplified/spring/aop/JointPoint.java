package simplified.spring.aop;

import java.lang.reflect.Method;

/**
 * 回调连接点，通过它可以获得被代理的业务方法中，所有的信息
 *
 * @author leishiguang
 * @since v1.0
 */
public interface JointPoint {

	/**
	 * 业务方法本身
	 * @return Method
	 */
	Method getMethod();

	/**
	 * 该方法的形参数列表
	 * @return Object[]
	 */
	Object[] getArguments();

	/**
	 * 该方法所属的实例对象
	 * @return Object
	 */
	Object getThis();

	/**
	 * 在 JointPoint 中添加自定义属性
	 * @param key key
	 * @param value value
	 */
	void setUserAttribute(String key, Object value);

	/**
	 * 从已添加的自定义属性中，获取值
	 * @param key key
	 * @return value
	 */
	Object getUserAttribute(String key);
}
