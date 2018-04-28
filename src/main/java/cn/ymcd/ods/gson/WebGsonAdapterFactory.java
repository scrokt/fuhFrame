package cn.ymcd.ods.gson;

import cn.ymcd.ods.db.base.enums.NameValueEnum;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Gson针对枚举类型做的适配器；需要继承NameValueEnum;在转换成Gson时，使用name属性；
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年8月22日 下午2:18:17
 * @version 1.0
 */
public class WebGsonAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();

        if(rawType.isEnum()){
            Class<?>[] interfaces = rawType.getInterfaces();
            for(Class<?> i : interfaces){
                if(i.equals(NameValueEnum.class)){
                    return new NameValueEnumAdapter<T>();
                }
            }
        }else if(rawType == String.class){
            return (TypeAdapter<T>)new StringNullAdapter();
        }else if(rawType == Integer.class){
            return (TypeAdapter<T>)new IntegerNullAdapter();
        }
        return null;
    }

}
