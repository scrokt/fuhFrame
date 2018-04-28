package cn.ymcd.ods.db.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SearchExpr {

    public String expr();
    
    public String column() default "";
    
    public String orGroup() default "or1";
    
    public String orderBy() default "";
}
