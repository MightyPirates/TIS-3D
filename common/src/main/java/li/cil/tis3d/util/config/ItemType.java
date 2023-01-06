package li.cil.tis3d.util.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ItemType {
    Class<?> value();

    CustomSerializer valueSerializer() default @CustomSerializer();
}
