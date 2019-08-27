package simplified.spring.demo.service.impl;

import simplified.spring.annotation.Service;
import simplified.spring.demo.service.DemoService;

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
		return "hello "+name;
	}
	
}
