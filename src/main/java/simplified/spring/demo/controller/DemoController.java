package simplified.spring.demo.controller;

import simplified.spring.annotation.Autowired;
import simplified.spring.annotation.Controller;
import simplified.spring.annotation.RequestMapping;
import simplified.spring.annotation.RequestParam;
import simplified.spring.demo.service.DemoService;
import simplified.spring.webmvc.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DemoController
 *
 * @author leishiguang
 * @since v1.0
 */
@Controller
@RequestMapping("/")
public class DemoController {

	@Autowired
	private DemoService demoService;

	@RequestMapping("/hello")
	public void hello(HttpServletRequest req, HttpServletResponse resp,
					  @RequestParam("name") String name) throws IOException {
		String result = demoService.hello(name);
		resp.getWriter().write(result);
	}

	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest req, HttpServletResponse resp,
							  @RequestParam("name") String name){
		String result = demoService.hello(name);
		Map<String,Object> model = new HashMap<>(6);
		model.put("data",result);
		model.put("name",name);
		return new ModelAndView("index.html",model);
	}

}
