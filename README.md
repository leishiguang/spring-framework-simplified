## 简化版 spring-framework

<p>
	<a target="_blank" href="https://github.com/leishiguang/spring-framework-mini/blob/master/LICENSE">
		<img src="https://img.shields.io/apm/l/vim-mode.svg?color=yellow" ></img>
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8+-green.svg" ></img>
	</a>
</p>

目前包含：（陆续完善中）
1. Spring Context、beanFactory
2. IoC、DI
4. MVC、HandlerMapper、DispatcherServlet
5. 动态视图解析
6. AOP（配置方式）（注解方式完善中）
7. 数据库连接池（完善中）
8. 数据库事务（完善中）


另有姊妹篇：300 行代码提炼 spring framework 核心原理 **[spring-framework-mini](https://github.com/leishiguang/spring-framework-mini)**

### spring-framework-simplified 核心原理解析

#### IoC、DI

ApplicationContext 类中：
1. 从配置文件中获取扫描包的路径；
2. 执行扫描，扫描到的结果封装成 BeanDefinition 存入 List；
3. 遍历 BeanDefinition，初始化 IoC 容器，每个 beanName 对应一个 BeanDefinition；
4. **执行 bean 的初始化**，遍历 IoC 容器，对其中的 bean，依据配置信息进行初始化；

#### MVC

**servlet 初始化**
1. 首先初始化 IoC ，在获取 bean 的时候即完成 DI 注入;
2. 遍历 IoC 容器中 Controller 类，添加 handlerMapper；
3. 每个 handlerMapper 均生成一个 handlerAdapter；
4. 遍历视图文件，存入 List;
5. 初始化完毕，等待请求...

**接收到请求**
1. 在 DispatcherServlet.doDispatch 方法中执行请求；
2. 依据 url 获取 handlerMapper;
3. 以反射的方式，执行 handlerMapper 中保存的方法；
4. 依据返回值确定是否执行 view 解析；
5. 回写 response 完成请求；

### AOP

待完善...

### 写在最后

当然，真正的 spring framework 要复杂许多，这儿主要是了解 spring framework 的基本设计思路，以及设计模式的应用。

最后祝大家生活愉快~