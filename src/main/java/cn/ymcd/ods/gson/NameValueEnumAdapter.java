package cn.ymcd.ods.gson;

import java.io.IOException;

import cn.ymcd.ods.db.base.enums.NameValueEnum;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class NameValueEnumAdapter<T> extends TypeAdapter<T> {

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        if (value instanceof NameValueEnum) {
            NameValueEnum<?> nvEnum = (NameValueEnum<?>)value;
            Object valObj = nvEnum.getValue();
            
            out.beginObject().name("name").value(nvEnum.getName());
            if (valObj instanceof String) {
                out.name("value").value((String)valObj);
            } else if (valObj instanceof Boolean) {
                out.name("value").value((Boolean)valObj);
            } else if (valObj instanceof Long) {
                out.name("value").value((Long)valObj);
            } else if (valObj instanceof Double) {
                out.name("value").value((Double)valObj);
            } else if (valObj instanceof Number) {
                out.name("value").value((Number)valObj);
            }
            out.endObject();
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return null;
    }

}
