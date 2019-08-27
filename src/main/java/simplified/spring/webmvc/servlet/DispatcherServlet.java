package simplified.spring.webmvc.servlet;

import lombok.extern.slf4j.Slf4j;
import simplified.spring.annotation.Controller;
import simplified.spring.annotation.RequestMapping;
import simplified.spring.context.ApplicationContext;
import simplified.spring.webmvc.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet 作为 MVC 的一个启动类
 *
 * @author leishiguang
 * @since v1.0
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

	private final String LOCATION = "contextConfigLocation";

	/**
	 * HandlerMapping 最核心的设计，也是最经典的
	 */
	List<HandlerMapping> handlerMappings = new ArrayList<>();

	private Map<HandlerMapping, HandlerAdapter> handlerAdapterMap = new HashMap<>(6);

	private List<ViewResolver> viewResolvers = new ArrayList<>();

	private ApplicationContext context;


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
			resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	public void init(ServletConfig config) {
		//相当于把 IoC 容器初始化了
		context = new ApplicationContext(config.getInitParameter(LOCATION));
		initStrategies(context);
		System.out.println("Simplified Spring Framework is init.");
	}

	/**
	 * 九种策略，即传说中的九大组件
	 * 针对每个用户请求，都会经过一些处理策略处理，最终才能有结果输出
	 * 每种策略可以自定义干预，但是最终结果都一致
	 */
	private void initStrategies(ApplicationContext context) {
		//文件上传解析，如果请求类型是 multipart，将通过 MultipartResolver 进行文件上传解析
		initMultipartResolver(context);
		//本地化解析
		initLocaleResolver(context);
		//主题解析
		initThemeResolver(context);
		//HandlerMapping 用来保存 Controller 中配置的 RequestMapping 和 Method 的对应关系
		initHandlerMapping(context);
		//HandlerAdapters 用来匹配 Method 参数，包括类转换、动态赋值
		initHandlerAdapters(context);
		//如果执行过程中遇到异常，则交给 HandlerExceptionResolver 来解析
		initHandlerExceptionResolvers(context);
		//直接将请求解析到视图名
		initRequestToViewNameTranslator(context);
		//通过 ViewResolver 实现动态模板的解析
		initViewResolver(context);
		//Flash 映射管理器
		initFlashMapManager(context);
	}

	/**
	 * 将 Controller 中配置的 RequestMapping 和 Method 进行一一对应
	 */
	private void initHandlerMapping(ApplicationContext context) {
		//首先从容器中获取所有的 beanName
		String[] beanNames = context.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			Object controller = context.getBean(beanName);
			Class<?> clazz = controller.getClass();
			if (!clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}
			//保存在类上面的 RequestMapping 和类上面的 url 配置
			String baseUrl = "";
			if (clazz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
				baseUrl = requestMapping.value();
			}
			//默认获取所有的 public 类型的方法，以及 Method 的 url 配置
			for (Method method : clazz.getMethods()) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}
				//映射 url
				RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
				String regex = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
				Pattern pattern = Pattern.compile(regex);
				this.handlerMappings.add(new HandlerMapping(controller, method, pattern));
				log.info("Mapping: " + regex + "," + method);
			}
		}
	}


	private void initHandlerAdapters(ApplicationContext context) {
		//在初始化阶段，我们能做的就是，将这些参数的名字或者类型按一定的顺序保存下来，留给后面反射调用
		for (HandlerMapping handlerMapping : this.handlerMappings) {
			this.handlerAdapterMap.put(handlerMapping, new HandlerAdapter());
		}
	}

	private void initViewResolver(ApplicationContext context) {
		//在页面输入 http://localhost/first.html
		//解决页面名字和模板文件关联的问题
		String templateRoot = context.getConfig().getProperty("templateRoot");
		if (null == templateRoot || "".equals(templateRoot)) {
			throw new NullPointerException("无法获取 templateRoot 配置");
		}
		URL resource = this.getClass().getClassLoader().getResource(templateRoot);
		if (resource == null) {
			throw new NullPointerException("无法获取位置：" + templateRoot);
		}
		String templateRootPath = resource.getFile();
		File templateRootDir = new File(templateRootPath);
		for (File template : Objects.requireNonNull(templateRootDir.listFiles())) {
			this.viewResolvers.add(new ViewResolver(template.getName()));
		}
	}

	private void initMultipartResolver(ApplicationContext context) {
	}

	private void initLocaleResolver(ApplicationContext context) {
	}

	private void initThemeResolver(ApplicationContext context) {
	}


	private void initHandlerExceptionResolvers(ApplicationContext context) {
	}

	private void initRequestToViewNameTranslator(ApplicationContext context) {
	}


	private void initFlashMapManager(ApplicationContext context) {
	}


	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws
			IOException {

		HandlerMapping handler = getHandler(req);
		if (handler == null) {
			processDispatchResult(req, resp, new ModelAndView("404"));
			return;
		}
		HandlerAdapter ha = getHandlerAdapter(handler);

		//调用方法得到返回值
		assert ha != null;
		ModelAndView mv = ha.handle(req, resp, handler);

		//执行返回
		assert mv != null;
		processDispatchResult(req, resp, mv);
	}


	/**
	 * 执行 view 转换
	 */
	private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) {
		if (mv == null) {
			throw new NullPointerException("model不能为空");
		}
		if (viewResolvers.isEmpty()) {
			throw new NullPointerException("viewResolvers不能为空");
		}
		for (ViewResolver viewResolver : viewResolvers) {
			View view = viewResolver.resolveViewName(mv.getViewName(), null);
			if (view != null) {
				view.render(mv.getModel(), req, resp);
				break;
			}
		}
	}

	private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
		if (this.handlerAdapterMap.isEmpty()) {
			throw new NullPointerException("handlerAdapterMap不能为空");
		}
		HandlerAdapter ha = handlerAdapterMap.get(handler);
		if (ha.supports(handler)) {
			return ha;
		}
		return null;
	}

	/**
	 * 获取对应 Handler
	 */
	private HandlerMapping getHandler(HttpServletRequest req) {
		assert handlerMappings.size() > 0;
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		for (HandlerMapping handler : handlerMappings) {
			Matcher matcher = handler.getPattern().matcher(url);
			if (matcher.matches()) {
				return handler;
			}
		}
		return null;
	}


}