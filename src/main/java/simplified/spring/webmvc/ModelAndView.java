package simplified.spring.webmvc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 封装页面模板和页面参数的对应关系
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
@AllArgsConstructor
public class ModelAndView {

	/**
	 * 页面模板名称
	 */
	private String viewName;

	/**
	 * 往页面传送的参数
	 */
	private Map<String,?> model;

	public ModelAndView(String viewName) {
		this.viewName = viewName;
		this.model = null;
	}
}
