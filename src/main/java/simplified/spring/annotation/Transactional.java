package simplified.spring.annotation;

import java.lang.annotation.*;

/**
 * jdbc事务注解
 *
 * @author leishiguang
 * @since v1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {
}
