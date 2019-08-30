package simplified.spring.annotation;

import java.lang.annotation.*;

/**
 * 切面注解
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {
}
