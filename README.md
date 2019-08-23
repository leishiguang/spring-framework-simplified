## 简化版 spring-framework

<p>
	<a target="_blank" href="https://github.com/leishiguang/spring-framework-mini/blob/master/LICENSE">
		<img src="https://img.shields.io/apm/l/vim-mode.svg?color=yellow" ></img>
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8+-green.svg" ></img>
	</a>
</p>

（本篇陆续完善中）
另有姊妹篇：300 行代码提炼 spring 核心原理 **[spring-framework-mini](https://github.com/leishiguang/spring-framework-mini)**

### spring-framework-mini 核心原理解析

重点在于 DispatcherServlet 的初始化过程，分为如下几个步骤：
1. 加载配置文件：获取扫描包的路径；
2. 扫描相关类：将添加了 @Service 或 @Controller 注解的类名，保存在 List 容器中；
3. 初始化扫描到的类：以 beanName 作为键值，在 HashMap 中保存初始化好的类对象，即放入 IoC 容器中；
4. 完成依赖注入：对增加了 @Autowired 注解的属性，注入 IoC 容器中的 bean；
5. 初始化 HandlerMapping：将 @Controller 类中，增加了 @RequestMapping 注解的方法，依据 url 配置，加入到 HandlerMapping 中；

初始化完毕，请求进来时：
1. 首先到达 doGet 或者 doPost 方法，并交由 doDispatch 方法进行处理；
2. 依据 url 从 HandlerMapping 中获取对应的 Handler；
3. 从 Handler 中取得对应方法的参数列表；
4. 按 Handler 的参数顺序，从 req 中获取到参数值；
5. 反射执行 Handler 中保存的方法，获得返回值；
6. resp 写出返回值，完成请求；

当然，真正的 Spring 要复杂许多，这儿主要是了解 Spring 的基本设计思路，以及设计模式的应用。

最后祝大家生活愉快~