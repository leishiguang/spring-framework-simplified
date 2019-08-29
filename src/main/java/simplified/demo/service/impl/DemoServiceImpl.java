package simplified.demo.service.impl;

import simplified.spring.annotation.Service;
import simplified.demo.service.DemoService;

/**
 * DemoServiceImpl
 *
 * @author leishiguang
 * @since v1.0
 */
@Service
public class DemoServiceImpl implements DemoService {
	@Override
	public String hello(String name) {
		//throw new UnsupportedOperationException("测试异常AOP");
		return "hello "+name;
	}
	
}
