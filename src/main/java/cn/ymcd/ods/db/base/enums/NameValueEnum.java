package cn.ymcd.ods.db.base.enums;

/**
 * 名称和值的枚举类型；基本枚举；
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年8月22日 下午2:20:11
 * @version 1.0
 */
public interface NameValueEnum<T> {

    T getValue();

    String getName();
}
