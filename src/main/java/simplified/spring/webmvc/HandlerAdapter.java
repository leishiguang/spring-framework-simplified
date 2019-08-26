package simplified.spring.webmvc;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 完成请求传递到服务端的参数列表与 Method 实参列表的对应关系
 * 完成参数值的类型转换工作
 *
 * @author leishiguang
 * @since v1.0
 */
@Slf4j
public class HandlerAdapter {

	public boolean supports(Object handler){
		return handler instanceof HandlerMapping;
	}

	public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler){
		HandlerMapping handlerMapping = (HandlerMapping) handler;
		//每个方法都有一个参数列表，这里保存的是形参列表
		Map<String,Integer> paramIndexMapping = new HashMap<>(6);
		//给出命名参数
		Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
		for (int i = 0; i < pa.length; i++) {
			for (Annotation a : pa[i]) {
				if (a instanceof RequestParam) {
					String paramName = ((RequestParam) a).value();
					if (!"".equals(paramName.trim())) {
						paramIndexMapping.put(paramName, i);
					}
				}
			}
		}
		//根据用户请求的参数信息，跟 Method 中的参数信息进行动态匹配
		//resp 传进来的目的只有一个，将其赋值给方法参数，仅此而已

		//只有当用户传过来的 ModelAndView 为空的时候，才会新建一个默认的

		//1.要准备好这个方法的形参列表
		//方法重载时形参的决定因素：参数的个数、参数的类型、参数顺序、方法的名称
		//提取方法中的 request 和 response 参数
		Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> type = paramTypes[i];
			if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
				paramIndexMapping.put(type.getName(), i);
			}
		}

		//2.得到自定义参数的所在位置
		//用户通过 url 传过来的参数
		Map<String,String[]> reqParameterMap = req.getParameterMap();

		//3.构造实参列表
		Object[] paramValues = new Object[paramTypes.length];
		for (Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
			String value = Arrays.toString(param.getValue())
					.replaceAll("[\\[\\]]", "")
					.replaceAll("\\s", ",");
			if (!paramIndexMapping.containsKey(param.getKey())) {
				continue;
			}
			int index = paramIndexMapping.get(param.getKey());
			paramValues[index] = caseStringValue(paramTypes[index], value);
		}
		if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
			int reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
			paramValues[reqIndex] = req;
		}
		if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
			int reqIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
			paramValues[reqIndex] = resp;
		}

		//4.从 handler 中取出 Controller、Method 然后利用反射机制调用
		Object result = null;
		try {
			result = handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("无法反射调用method",e);
		}
		if(result == null){
			return null;
		}
		boolean isModeAndView = handlerMapping.getMethod().getReturnType() == ModelAndView.class;
		if(isModeAndView){
			return (ModelAndView)result;
		}else {
			return null;
		}
	}

	/**
	 * 由于 HTTP 基于字符串协议，url 传过来的参数都是 String 类型的
	 * 只需要把 String 转换为任意类型
	 */
	private Object caseStringValue(Class<?> type, String value) {
		if(String.class == type){
			return value;
		}
		if (Integer.class == type || int.class == type) {
			return Integer.valueOf(value);
		}
		// 如果还有 double 或者其它类型的参数，继续增加 if
		// 可以使用策略模式进行优化，这儿略。
		return value;
	}


}
