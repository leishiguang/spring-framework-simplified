package simplified.spring.aop.support;

import lombok.Data;
import simplified.spring.aop.AopConfig;
import simplified.spring.aop.aspect.AfterReturningAdvice;
import simplified.spring.aop.aspect.AfterThrowingAdvice;
import simplified.spring.aop.aspect.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主要用来解析和封装 AOP 配置
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class AdvisedSupport {

	private Class targetClass;
	private Object target;
	private Pattern pointCutClassPattern;

	private String pointCut;

	/**
	 * 代理的方法缓存
	 */
	private transient Map<Method, List<Object>> methodCache;

	private AopConfig aopConfig;

	public AdvisedSupport(AopConfig aopConfig) {
		this.aopConfig = aopConfig;
	}

	/**
	 * 设置代理类，用在使用配置文件声明切面类的时候
	 * @param targetClass 目标类
	 */
	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
		init();
		parsePattern();
		parseMethods();
	}

	/**
	 * 设置代理类与代理方法，用在使用注解声明要切入的方法时
	 * @param targetClass 目标类
	 * @param targetMethod 目标方法
	 */
	public void setTargetMethod(Class targetClass, Method targetMethod){
		this.targetClass = targetClass;
		init();
		parseMethod(targetMethod);
	}

	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
		List<Object> cached = methodCache.get(method);
		//缓存未命中，进行下一步处理
		if (cached == null) {
			Method m;
			try {
				m = targetClass.getMethod(method.getName(), method.getParameterTypes());
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("无法在类中查找到方法,class=" + targetClass + ",method=" + method.getName(), e);
			}
			cached = methodCache.get(m);
			methodCache.put(m, cached);
		}
		if (cached == null) {
			throw new NullPointerException("无法获得要代理的方法,method:" + method);
		}
		return cached;
	}

	public boolean pointCutMatch() {
		return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
	}

	private void init(){
		methodCache = new HashMap<>(6);
	}
	/**
	 * 生成 Method 的匹配方式；
	 * 这儿的 pointCut 是 String字符串，用以匹配这个类的哪些方法要被 AOP
	 */
	private void parsePattern() {
		//pointCut 表达式
		String pointCut = aopConfig.getPointCut()
				.replaceAll("\\.", "\\\\.")
				.replaceAll("\\\\.\\*", ".*")
				.replaceAll("\\(", "\\\\(")
				.replaceAll("\\)", "\\\\)");
		String pointCutForClass = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
		pointCutForClass = pointCutForClass.substring(pointCutForClass.lastIndexOf(" ") + 1);
		pointCutClassPattern = Pattern.compile("class " + pointCutForClass);
		this.pointCut = pointCut;
	}

	private void parseMethods(){
		Pattern pattern = Pattern.compile(pointCut);
		//在这里得到的方法都是原生方法
		for (Method m : targetClass.getMethods()) {
			String methodString = m.toString();
			if (methodString.contains("throws")) {
				methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
			}
			Matcher matcher = pattern.matcher(methodString);
			if (matcher.matches()) {
				parseMethod(m);
			}
		}
	}

	/**
	 *
	 * @param targetMethod 要被代理的方法
	 */
	private void parseMethod(Method targetMethod){
		Class aspectClass;
		try {
			aspectClass = Class.forName(aopConfig.getAspectClass());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("无法找到Class:" + aopConfig.getAspectClass(), e);
		}
		Map<String, Method> aspectMethods;
		aspectMethods = new HashMap<>(6);
		for (Method m : aspectClass.getMethods()) {
			aspectMethods.put(m.getName(), m);
		}
		//能满足切面规则的类，添加到 AOP 配置中
		List<Object> advices = new LinkedList<>();
		//前置通知
		if (!(aopConfig.getAspectBefore() == null || "".equals(aopConfig.getAspectBefore().trim()))) {
			Object beforeObject;
			try {
				beforeObject = aspectClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("无法实例化class:" + aspectClass, e);
			}
			advices.add(new MethodBeforeAdvice(aspectMethods.get(aopConfig.getAspectBefore()), beforeObject));
		}
		//后置通知
		if (!(aopConfig.getAspectAfter() == null || "".equals(aopConfig.getAspectAfter().trim()))) {
			Object afterObject;
			try {
				afterObject = aspectClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("无法实例化class:" + aspectClass, e);
			}
			advices.add(new AfterReturningAdvice(aspectMethods.get(aopConfig.getAspectAfter()), afterObject));
		}
		//异常通知
		if (!(aopConfig.getAspectAfterThrow() == null || "".equals(aopConfig.getAspectAfterThrow().trim()))) {
			Object throwObject;
			try {
				throwObject = aspectClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("无法实例化class:" + aspectClass, e);
			}
			AfterThrowingAdvice afterThrowingAdvice = new AfterThrowingAdvice(aspectMethods.get(aopConfig.getAspectAfterThrow()), throwObject);
			afterThrowingAdvice.setThrowingName(aopConfig.getAspectAfterThrowingName());
			advices.add(afterThrowingAdvice);
		}
		methodCache.put(targetMethod, advices);
	}

}
