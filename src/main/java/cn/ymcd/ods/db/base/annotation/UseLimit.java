package cn.ymcd.ods.db.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本注解是基于ORACLE数据库，其他类型数据库不适用
 * @projectName:pine-ods
 * @author:wangjf
 * @date:2017年12月5日 上午8:42:21
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseLimit {

    
}
