package simplified.spring.beans.support;

import lombok.Data;
import simplified.spring.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * 完成对 application.properties 配置文件的解析
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class BeanDefinitionReader {

	private List<String> registryBeanClasses = new ArrayList<>();
	private Properties config = new Properties();

	/**
	 * 配置文件中的 key 键值
	 */
	private final String SCAN_PACKAGE = "scanPackage";

	/**
	 * 初始化的时候，即加载配置文件，扫描相应的类
	 *
	 * @param locations 扫描的类路径
	 */
	public BeanDefinitionReader(String... locations) {
		try (InputStream fis = this.getClass().getClassLoader().getResourceAsStream(
				locations[0].replace("classpath:", ""))) {
			if (fis == null) {
				throw new NullPointerException("读取配置不能为空");
			}
			config.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		doScanner(config.getProperty(SCAN_PACKAGE));
	}

	/**
	 * 把配置文件中扫描到的所有配置信息转换为 BeanDefinition 对象，以便于之后的 IoC 操作
	 *
	 * @return List<BeanDefinition>
	 */
	public List<BeanDefinition> loadBeanDefinitions() {
		List<BeanDefinition> result = new ArrayList<>();
		try {
			for (String className : registryBeanClasses) {
				Class<?> beanClass = Class.forName(className);
				if (beanClass.isInterface()) {
					continue;
				}
				result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
				Class<?>[] interfaces = beanClass.getInterfaces();
				for (Class<?> iClazz : interfaces) {
					result.add(doCreateBeanDefinition(iClazz.getName(), beanClass.getName()));
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 扫描相关类
	 *
	 * @param scanPackage 要扫描的包路径
	 */
	private void doScanner(String scanPackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		assert url != null;
		File classPath = new File(url.getFile());
		for (File file : Objects.requireNonNull(classPath.listFiles())) {
			if (file.isDirectory()) {
				doScanner(scanPackage + "." + file.getName());
			} else {
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				String className = scanPackage + "." + file.getName().replaceAll(".class", "");
				registryBeanClasses.add(className);
			}
		}
	}

	/**
	 * 把每一个配置信息解析成一个 BeanDefinition
	 */
	private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
		BeanDefinition beanDefinition = new BeanDefinition();
		beanDefinition.setBeanClassName(beanClassName);
		beanDefinition.setFactoryBeanName(factoryBeanName);
		return beanDefinition;
	}

	/**
	 * 首字母小写
	 *
	 * @param simpleName 首字母要变成小写的字符
	 * @return 首字母转为小写
	 */
	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return String.valueOf(chars);
	}
}
