package simplified.spring.webmvc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 保存 url 与 Method 的对应关系
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
@AllArgsConstructor
public class HandlerMapping {

	/**
	 * 保存方法对应的实例
	 */
	private Object controller;

	/**
	 * 保存映射的方法
	 */
	private Method method;

	/**
	 * 正则表达式
	 */
	private Pattern pattern;

}
