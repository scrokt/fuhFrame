package cn.ymcd.ods.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Gson工具类；Gson使用中会抛出许多异常，工具类将提供不抛异常的方法，使用返回null代替；
 * 
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年8月9日 下午2:05:14
 * @version 1.0
 */
public class GsonUtil {

    private static Logger LOG = Logger.getLogger(GsonUtil.class);

    /**
     * 从json字符串中解析jsonObject；失败返回Null；
     * 
     * @param json
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:07:00
     */
    public static JsonObject parseObjectFrom(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonElement parse = null;
        try {
            parse = jsonParser.parse(json);
            JsonObject jsonObject = parse.getAsJsonObject();
            return jsonObject;
        } catch (JsonParseException e) {
            LOG.warn("->json解析失败，请检查！", e);
            return null;
        } catch (IllegalStateException e) {
            LOG.warn("->element转object失败，请检查！", e);
            return null;
        }
    }

    /**
     * 从json字符串中解析jsonArray；失败返回Null；
     * 
     * @param json
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:32:47
     */
    public static JsonArray parseArrayFrom(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonElement parse = null;
        try {
            parse = jsonParser.parse(json);
            JsonArray jsonArray = parse.getAsJsonArray();
            return jsonArray;
        } catch (JsonParseException e) {
            LOG.warn("->json解析失败，请检查！", e);
            return null;
        } catch (IllegalStateException e) {
            LOG.warn("->element转array失败，请检查！", e);
            return null;
        }
    }

    /**
     * 获取String值；失败返回Null;
     * 
     * @param jsonElement
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:36:19
     */
    public static String getString(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }
        try {
            String value = jsonElement.getAsString();
            return value;
        } catch (ClassCastException | IllegalStateException e) {
            LOG.warn("->element转String失败，请检查!", e);
            return null;
        }
    }

    /**
     * 获取boolean值；失败返回Null;
     * 
     * @param jsonElement
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:26:07
     */
    public static Boolean getBooleanProperty(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }
        try {
            Boolean value = jsonElement.getAsBoolean();
            return value;
        } catch (ClassCastException | IllegalStateException e) {
            LOG.warn("->element转Boolean失败，请检查!", e);
            return null;
        }
    }

    /**
     * 获取Integer值；失败返回Null;
     * 
     * @param jsonElement
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:29:47
     */
    public static Integer getIntegerProperty(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }
        try {
            Integer value = jsonElement.getAsInt();
            return value;
        } catch (ClassCastException | IllegalStateException e) {
            LOG.warn("->element转Integer失败，请检查！", e);
            return null;
        }
    }

    /**
     * 获取Long值；失败返回Null;
     * 
     * @param jsonElement
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:39:48
     */
    public static Long getLong(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }
        try {
            Long value = jsonElement.getAsLong();
            return value;
        } catch (ClassCastException | IllegalStateException e) {
            LOG.warn("->element转Long失败，请检查！", e);
            return null;
        }
    }

    /**
     * 获取Double值；失败返回Null;
     * 
     * @param jsonElement
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午2:40:27
     */
    public static Double getDouble(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }
        try {
            Double value = jsonElement.getAsDouble();
            return value;
        } catch (ClassCastException | IllegalStateException e) {
            LOG.warn("->element转Double失败，请检查！", e);
            return null;
        }
    }

    /**
     * 将对象转换为json字符串；日期类型使用默认格式；
     * 
     * @param obj
     * @return
     * @author:fuh
     * @createTime:2017年8月10日 上午10:32:50
     */
    public static String toJson(Object obj) {
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME_PATTERN).create();
        return gson.toJson(obj);
    }

    /**
     * json转换为对象
     * 
     * @param json
     * @param classOfT
     * @return
     * @author:fuh
     * @createTime:2017年11月21日 下午4:34:52
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME_PATTERN).create();
        return gson.fromJson(json, classOfT);
    }
    /**
     * JSON字符串数组转换成指定的DTO
     * 
     * @param jsons
     * @param clazz
     * @return
     * @author:wangjf
     * @createTime:2017年5月23日 下午4:04:11
     */
    public static <T> List<T> fromGsonArray(String jsons, Class<T> clazz) {
        Assert.notNull(jsons);
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsons);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();
        Gson gson = new Gson();
        List<T> arrayList = new ArrayList<T>();
        while (iterator.hasNext()) {
            JsonElement next = iterator.next();
            JsonObject jsonObject = next.getAsJsonObject();
            arrayList.add(gson.fromJson(jsonObject, clazz));
        }
        return arrayList;
    }
    
    public static List<String> json2StrList(String jsons) {
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME_PATTERN).create();
        List<String> fromJson = gson.fromJson(jsons, new TypeToken<List<String>>(){}.getType());
        return fromJson;
    }
}
