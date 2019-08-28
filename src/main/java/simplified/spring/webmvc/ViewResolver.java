package simplified.spring.webmvc;

import lombok.Getter;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

/**
 * 完成模板名称和模板解析引擎的匹配
 * 1.将一个静态文件变为一个动态文件
 * 2.根据用户传送不同的参数，产生不同的结果
 * 3.最终输出字符串，交给 Response 输出
 *
 * @author leishiguang
 * @since v1.0
 */
@Getter
public class ViewResolver {

	private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

	private File templateRootDir;
	private String viewName;

	public ViewResolver(String templateRoot){
		String templateRootPath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(templateRoot))
				.getFile();
		this.templateRootDir = new File(templateRootPath);
		this.viewName = this.templateRootDir.getName();
	}

	public View resolveViewName(String viewName, Locale locale){
		this.viewName = viewName;
		if(viewName == null || "".equals(viewName)){
			return null;
		}
		viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)? viewName : viewName + DEFAULT_TEMPLATE_SUFFIX;
		File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
		if(templateFile.exists()){
			return new View(templateFile);
		}
		return null;
	}
}
