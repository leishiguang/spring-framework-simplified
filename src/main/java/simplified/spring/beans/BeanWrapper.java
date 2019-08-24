package simplified.spring.beans;

import lombok.Data;

/**
 * 用于封装创建后的对象实例
 * 代理对象（Proxy Object）或者原生对象（Original Object）都由 BeanWrapper 来保存
 *
 * @author leishiguang
 * @since v1.0
 */
@Data
public class BeanWrapper {

	private Object wrappedInstance;

	private Class<?> wrappedClass;

	public BeanWrapper(Object wrappedInstance) {
		this.wrappedInstance = wrappedInstance;
	}
}
