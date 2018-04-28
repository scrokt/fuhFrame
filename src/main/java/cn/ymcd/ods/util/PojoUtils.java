package cn.ymcd.ods.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.ymcd.ods.db.base.annotation.Column;
import cn.ymcd.ods.db.base.annotation.Table;
import cn.ymcd.ods.db.base.annotation.TempField;
import cn.ymcd.ods.db.base.annotation.UpdateKey;
import cn.ymcd.ods.db.base.enums.NameValueEnum;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * pojo工具类，主要针对dto类进行处理，依赖ods注解和枚举基类，依赖Gson jar做json转换；
 * 
 * @author fuh 2015年10月28日
 */
public class PojoUtils {

    private static ThreadLocal<String> currentUser = new ThreadLocal<String>();
    private static final String CREATE_USER = "createUser";
    private static final String CREATOR = "creator";
    private static final String UPDATOR = "updator";
    private static final String TIMELINE = "timeline";
    private static final String UPDATE_TIME = "updateTime";
    private static final String CREATE_TIME = "createTime";
    private static final String STATUS = "status";
    public static final int ONLY_CREATE = 1;
    public static final int ONLY_UPDATE = 2;
    public static final int BOTH = 0;
    private static Logger LOG = Logger.getLogger(PojoUtils.class);

    /**
     * 设置当前用户的线程变量
     * 
     * @param user
     * @author:fuh
     * @createTime:2017年8月9日 上午10:59:10
     */
    public static void setCurrentUser(String user) {
        currentUser.set(user);
    }

    /**
     * 获取当前用户
     * 
     * @param user
     * @author:fuh
     * @createTime:2017年8月9日 上午11:05:33
     */
    public static String getCurrentUser() {
        return currentUser.get();
    }

    /**
     * 移除当前用户；线程结束后移除，防止线程池时造成错乱；
     * 
     * @param user
     * @author:fuh
     * @createTime:2017年8月9日 上午11:05:53
     */
    public static void removeCurrentUser() {
        currentUser.remove();
    }

    /**
     * 注入创建时间和更新时间;
     * 
     * @param pojo
     * @param type
     *            1：只注入创建时间 2：只注入更新时间 0:一起注入
     * @throws PojoInjectException
     */
    public static void setCreateUpdateTime(Object pojo, int type) {
        String setCtMethodName = "setCreateTime";
        String setUtMethodName = "setUpdateTime";
        Class<? extends Object> cls = pojo.getClass();
        try {
            Method setCtMethod = cls.getMethod(setCtMethodName, Date.class);
            Method setUtMethod = cls.getMethod(setUtMethodName, Date.class);
            Date now = new Date();
            if (type != 2) {
                setCtMethod.invoke(pojo, now);
            }
            if (type != 1) {
                setUtMethod.invoke(pojo, now);
            }
        } catch (Exception e) {
            throw new RuntimeException("注入创建时间或更新时间失败，请检查方法是否为setCreateTime|setUpdateTime", e);
        }
    }

    /**
     * 注入创建时间和更新时间，用户,时间戳;用户尝试从线程变量中取值，有的话就插入；
     * 
     * @param type
     *            1:插入 2:更新
     * @param pojo
     */
    public static void checkAndSetTime(Object pojo, int type) {

        if (pojo == null) {
            return;
        }
        Field[] declaredFields = pojo.getClass().getDeclaredFields();// 获取字段列表
        for (Field field : declaredFields) {
            String name = field.getName();
            if (type == 1) {
                if (CREATE_TIME.equals(name)) {
                    Object createTime = getPropertyValue(pojo, CREATE_TIME);
                    if (createTime == null) {
                        setPropertyValue(pojo, CREATE_TIME, new Date());
                    }
                } else if (UPDATE_TIME.equals(name)) {
                    Object updateTime = getPropertyValue(pojo, UPDATE_TIME);
                    if (updateTime == null) {
                        setPropertyValue(pojo, UPDATE_TIME, new Date());
                    }
                } else if (TIMELINE.equals(name)) {
                    Object timeline = getPropertyValue(pojo, TIMELINE);
                    if (timeline == null) {
                        setPropertyValue(pojo, TIMELINE, new Date());
                    }
                } else if (CREATE_USER.equals(name)) {
                    Object createUser = getPropertyValue(pojo, CREATE_USER);
                    if (createUser == null) {
                        setPropertyValue(pojo, CREATE_USER, currentUser.get());
                    }
                } else if (CREATOR.equals(name)) {
                    Object createUser = getPropertyValue(pojo, CREATOR);
                    if (createUser == null) {
                        setPropertyValue(pojo, CREATOR, currentUser.get());
                    }
                } else if (UPDATOR.equals(name)) {
                    Object updateUser = getPropertyValue(pojo, UPDATOR);
                    if (updateUser == null) {
                        setPropertyValue(pojo, UPDATOR, currentUser.get());
                    }
                } else if (STATUS.equals(name)) {
                    Object status = getPropertyValue(pojo, STATUS);
                    if (status == null) {
                        try {
                            setPropertyValue(pojo, STATUS, 1);
                        } catch (Exception e) {
                            LOG.warn("->设置status值1失败，将尝试设置String(1)", e);
                            try {
                                setPropertyValue(pojo, STATUS, "1");
                            } catch (Exception ex) {
                                LOG.warn("->设置status值String(1)失败", e);
                            }
                        }
                    }
                }
            } else {
                if (TIMELINE.equals(name)) {
                    Object timeline = getPropertyValue(pojo, TIMELINE);
                    if (timeline == null) {
                        setPropertyValue(pojo, TIMELINE, new Date());
                    }
                } else if (UPDATE_TIME.equals(name)) {
                    Object updateTime = getPropertyValue(pojo, UPDATE_TIME);
                    if (updateTime == null) {
                        setPropertyValue(pojo, UPDATE_TIME, new Date());
                    }
                } else if (UPDATOR.equals(name)) {
                    Object updateUser = getPropertyValue(pojo, UPDATOR);
                    if (updateUser == null) {
                        setPropertyValue(pojo, UPDATOR, currentUser.get());
                    }
                }
            }
        }
    }

    /**
     * 检查对象及属性是否会为空，通过反射取值;
     * 
     * @param pojo
     * @param properties
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午5:23:01
     */
    public static boolean checkPojoAndProperties(Object pojo, String...properties) {
        boolean result = true;
        if (pojo == null) {
            result = false;
        } else {
            for (String property : properties) {
                Object value = getPropertyValue(pojo, property);
                if (value == null) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取属性的get方法，pojo必须提供；如果没有方法，抛出异常；
     * 
     * @param cls
     * @param property
     * @return 非null
     * @author:fuh
     * @createTime:2017年8月7日 下午5:28:46
     */
    public static Method getGetMethod(Class<?> cls, String property) {
        String getMethodName = null;

        if (property.length() == 1) {// 只有1位
            getMethodName = "get" + property.toUpperCase();
        } else {
            char second = property.charAt(1);
            if (second >= 'A' && second <= 'Z') {// 判断第二个字母是否为大写
                getMethodName = "get" + property;
            } else {
                getMethodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
            }
        }

        try {
            Method getMethod = cls.getMethod(getMethodName);
            return getMethod;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("获取get方法失败:" + getMethodName, e);
        }
    }

    /**
     * 获取set方法，需要指定参数类型；如果没有方法，抛出异常；
     * 
     * @param cls
     * @param property
     * @param paramType
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午5:32:42
     */
    public static Method getSetMethod(Class<?> cls, String property, Class<?> paramType) {
        String setMethodName = null;

        if (property.length() == 1) {
            setMethodName = "set" + property.toUpperCase();
        } else {
            char second = property.charAt(1);
            if (second >= 'A' && second <= 'Z') {// 判断第二个字母是否为大写
                setMethodName = "set" + property;
            } else {
                setMethodName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
            }
        }
        try {
            Method getMethod = cls.getMethod(setMethodName, paramType);
            return getMethod;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("获取set方法失败:" + setMethodName, e);
        }
    }

    /**
     * 通过get方法，获取指定属性值；获取失败抛出异常；
     * 
     * @param pojo
     * @param property
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午5:34:39
     */
    public static Object getPropertyValue(Object pojo, String property) {
        Method getMethod = getGetMethod(pojo.getClass(), property);// 此处不可能为null
        try {
            Object value = getMethod.invoke(pojo);
            return value;
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException("使用get方法获取属性值失败:" + property, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("get方法内部错误:" + property, e);
        }
    }

    public static boolean hasProperty(Object pojo, String property) {
        Class<? extends Object> clz = pojo.getClass();
        try {
            clz.getDeclaredField(property);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * 执行一个无参方法；
     * 
     * @param pojo
     * @param methodName
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午5:41:28
     */
    public static Object excuteMethod(Object pojo, String methodName) {
        Class<? extends Object> cls = pojo.getClass();
        try {
            Method getMethod = cls.getMethod(methodName);
            if (getMethod == null) {
                return null;
            }
            Object value = getMethod.invoke(pojo);
            return value;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("执行方法失败，找不到:" + methodName, e);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException("执行方法失败，权限或参数错误:" + methodName, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("执行方法失败，方法内部错误:" + methodName, e);
        }
    }

    /**
     * 获取属性值，强制转换后返回；
     * 
     * @param pojo
     * @param property
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午5:49:37
     */
    public static <T> T getCastPropertyValue(Object pojo, String property) {
        Object propertyValue = getPropertyValue(pojo, property);
        return (T)propertyValue;
    }

    /**
     * 设置属性值，调用set方法；pojo的set方法不返回值；
     * 
     * @param pojo
     * @param property
     * @param value
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午5:52:28
     */
    public static void setPropertyValue(Object pojo, String property, Object value) {
        if (value == null) {
            return;
        }
        Class<?> paramType = value.getClass();
        if (value instanceof List) {
            paramType = List.class;
        } else if (value instanceof Map) {
            paramType = Map.class;
        } else if (value instanceof Set) {
            paramType = Set.class;
        }
        Method setMethod = getSetMethod(pojo.getClass(), property, paramType);// 不会为空
        try {
            setMethod.invoke(pojo, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException("使用set方法设置属性值失败:" + property, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("使用set方法设置属性值失败，方法内部错误:" + property, e);
        }
    }

    public static void setPropertyNull(Object pojo, String property) {

        Class<? extends Object> clz = pojo.getClass();
        try {
            Field field = clz.getDeclaredField(property);
            field.setAccessible(true);
            field.set(pojo, null);
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拷贝对象，转换；属性名相同，调用get方法取值，set方法设置值；
     * 
     * @param pojo1
     * @param pojo2
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午6:04:37
     */
    public static <T> T transfer(T pojo1, Object pojo2) {
        if (pojo1 == null) {
            return null;
        }
        Class<?> class1 = pojo1.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        for (Field field : declaredFields) {
            String name = field.getName();
            if (name.equals("serialVersionUID")) {
                continue;
            }
            Object fieldValue = getPropertyValue(pojo2, name);
            if (fieldValue == null) {
                continue;
            }
            setPropertyValue(pojo1, name, fieldValue);
        }
        return pojo1;
    }

    public static <T> T transferEx(T pojo1, Object pojo2) {
        if (pojo1 == null) {
            return null;
        }
        Class<?> class1 = pojo1.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        Class<? extends Object> class2 = pojo2.getClass();
        for (Field field : declaredFields) {
            String name = field.getName();
            if (name.equals("serialVersionUID")) {
                continue;
            }
            Field field2 = null;
            try {
                field2 = class2.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (field2 == null) {
                continue;
            }
            field2.setAccessible(true);

            Object value = null;
            try {
                value = field2.get(pojo2);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value == null) {
                continue;
            }

            Object setValue = value;
            if (value instanceof Date && field.getType().equals(String.class)) {
                setValue = DateUtil.format((Date)value, DateUtil.DATE_TIME_PATTERN);
            }
            if (value instanceof String && field.getType().equals(Date.class)) {
                setValue = DateUtil.parse((String)value, DateUtil.DATE_TIME_PATTERN);
            }
            try {
                field.setAccessible(true);
                field.set(pojo1, setValue);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return pojo1;
    }

    /**
     * 对象转换为map;不支持复杂属性；
     * 
     * @param pojo
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午8:53:32
     */
    public static Map<String, String> transferToMap(Object pojo) {
        if (pojo == null) {
            return null;
        }
        Class<?> class1 = pojo.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        Map<String, String> map = new HashMap<String, String>(declaredFields.length);
        for (Field field : declaredFields) {
            String name = field.getName();
            Object fieldValue = getPropertyValue(pojo, name);
            if (fieldValue == null) {
                continue;
            }
            map.put(name, fieldValue.toString());
        }
        return map;
    }

    /**
     * 对象转换为请求参数；格式a=xx&b=zz&c=yy;null返回null；
     * 
     * @param pojo
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午8:54:50
     */
    public static String transferToForm(Object pojo) {
        if (pojo == null) {
            return null;
        }
        Class<?> class1 = pojo.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        for (Field field : declaredFields) {
            String name = field.getName();
            Object fieldValue = getPropertyValue(pojo, name);
            if (fieldValue == null) {
                continue;
            }
            if (fieldValue instanceof Date) {
                sb.append(name).append("=").append(DateUtil.defaultFormat((Date)fieldValue)).append("&");
            } else {
                sb.append(name).append("=").append(fieldValue.toString()).append("&");
            }
        }
        if (sb.length() > 1) {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
        return sb.toString();
    }

    /**
     * 转换为Map<String,Object>，不通过get方法；
     * 
     * @param pojo
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午8:59:27
     */
    public static Map<String, Object> transferToMap2(Object pojo) {
        if (pojo == null) {
            return null;
        }
        Class<?> class1 = pojo.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        Map<String, Object> map = new HashMap<String, Object>();
        for (Field field : declaredFields) {
            String name = field.getName();
            field.setAccessible(true);
            try {
                map.put(name, field.get(pojo));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOG.warn("获取属性的值失败：" + name, e);
            }
        }
        return map;
    }

    /**
     * 从Map中转换对象出来；
     * 
     * @param pojo
     * @param map
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:02:44
     */
    public static <T> T transferFromMap(T pojo, Map<String, String> map) {
        if (map == null) {
            return null;
        }
        Class<?> class1 = pojo.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        for (Field field : declaredFields) {
            String name = field.getName();
            setPropertyValue(pojo, name, map.get(name));
        }
        return pojo;
    }

    /**
     * 安静的转换，忽略异常；
     * 
     * @param pojo1
     * @param pojo2
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:10:09
     */
    public static <T> T transferQuiet(T pojo1, Object pojo2) {
        if (pojo1 == null) {
            return null;
        }
        Class<?> class1 = pojo1.getClass();
        Field[] declaredFields = class1.getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                String name = field.getName();
                Object fieldValue = getPropertyValue(pojo2, name);
                if (fieldValue == null) {
                    continue;
                }
                field.setAccessible(true);
                field.set(pojo1, fieldValue);
                // setPropertyValue(pojo1,name,fieldValue);
            } catch (Exception e) {

            }
        }
        return pojo1;
    }

    /**
     * 转换List;
     * 
     * @param cls
     * @param pojoList
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:11:15
     */
    public static <T> List<T> transfer(Class<T> cls, List<?> pojoList) {
        if (pojoList == null) {
            return null;
        }
        try {

            List<T> list = new ArrayList<T>();
            for (Object pojo : pojoList) {
                T newInstance = cls.newInstance();
                transfer(newInstance, pojo);
                list.add(newInstance);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("转换时new对象失败");
        }
    }

    /**
     * 获取dto的Table;
     * 
     * @param entityClass
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:11:30
     */
    public static String getTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("dto类没有Table注解");
        }
        return table.name();
    }

    /**
     * 获取redis keyTableName
     * 
     * @param entityClass
     * @return
     * @author:fuh
     * @createTime:2017年9月15日 上午10:53:59
     */
    public static String getRedisKeyTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("dto类没有Table注解");
        }
        return table.name() + "_key";
    }

    /**
     * 获取dto的Insert语句；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:11:41
     */
    public static <T> String getInsertSql(T entity) {
        return getInsertSql(entity, getTableName(entity.getClass()));
    }

    public static <T> String getInsertSql(T entity, String tableName) {
        Class<?> entityClass = entity.getClass();
        StringBuffer sql = new StringBuffer();
        Field[] fields = entityClass.getDeclaredFields();
        sql.append(" INSERT INTO " + tableName);
        sql.append("(");
        int num = 0;
        for (int i = 0; fields != null && i < fields.length; i++) {
            String column = getColumn(fields[i]);
            Object value = getPropertyValue(entity, column);
            TempField tempField = fields[i].getAnnotation(TempField.class);
            if (value != null && tempField == null) {
                sql.append(column).append(",");
                num++;
            }
        }
        sql = sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");
        for (int i = 0; i < num; i++) {
            sql.append("?,");
        }
        sql = sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    /**
     * 获取field对应的字段
     * 
     * @param field
     * @return
     * @author:fuh
     * @createTime:2017年8月25日 下午3:41:59
     */
    private static String getColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotEmpty(column.value())) {
            return column.value();
        }
        return field.getName();
    }

    /**
     * 获取dtoClass的Insert语句；
     * 
     * @param entityClass
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:11:56
     */
    public static String getInsertSql(Class<?> entityClass) {
        return getInsertSql(entityClass, getTableName(entityClass));
    }

    public static String getInsertSql(Class<?> entityClass, String tableName) {
        StringBuffer sql = new StringBuffer();
        Field[] fields = entityClass.getDeclaredFields();
        sql.append(" INSERT INTO " + tableName);
        sql.append("(");
        int num = 0;
        for (int i = 0; fields != null && i < fields.length; i++) {
            String column = getColumn(fields[i]);
            TempField tempField = fields[i].getAnnotation(TempField.class);
            if (tempField == null) {
                sql.append(column).append(",");
                num++;
            }
        }
        sql = sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");
        for (int i = 0; i < num; i++) {
            sql.append("?,");
        }
        sql = sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    /**
     * 按对象属性顺序，设置ps参数值；
     * 
     * @param ps
     * @param dto
     * @throws SQLException
     * @author:fuh
     * @createTime:2017年8月8日 上午9:12:20
     */
    public static void setSqlArg(PreparedStatement ps, Object dto) throws SQLException {
        Class<? extends Object> entityClass = dto.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        for (int i = 1, j = 0; fields != null && j < fields.length; j++) {
            String column = fields[j].getName();
            TempField tempField = fields[j].getAnnotation(TempField.class);
            if (tempField == null) {
                Object propertyValue = getPropertyValue(dto, column);
                Class<?> class1 = fields[j].getType();

                if (String.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.VARCHAR);
                    } else {
                        ps.setString(i, (String)propertyValue);
                    }
                } else if (Integer.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.INTEGER);
                    } else {
                        ps.setInt(i, (int)propertyValue);
                    }
                } else if (Long.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.INTEGER);
                    } else {
                        ps.setLong(i, (long)propertyValue);
                    }
                } else if (Date.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.DATE);
                    } else {
                        ps.setTimestamp(i, new java.sql.Timestamp(((Date)propertyValue).getTime()));
                    }
                } else if (Timestamp.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.TIMESTAMP);
                    } else {
                        ps.setTimestamp(i, (Timestamp)propertyValue);
                    }
                } else if (Float.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.FLOAT);
                    } else {
                        ps.setFloat(i, (float)propertyValue);
                    }
                } else if (Double.class.equals(class1)) {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.DOUBLE);
                    } else {
                        ps.setDouble(i, (double)propertyValue);
                    }
                } else {
                    if (propertyValue == null) {
                        ps.setNull(i, Types.VARCHAR);
                    } else {
                        ps.setString(i, propertyValue.toString());
                    }
                }
                i++;

            }
        }
    }

    /**
     * 获取dto的update语句；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:13:36
     */
    public static String getUpdateSql(Object entity) {
        Class<?> entityClass = entity.getClass();
        UpdateKey updateKey = entityClass.getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("update失败,dto未定义updateKey");
        }
        String keys = updateKey.keys();
        Field[] fields = entityClass.getDeclaredFields();
        StringBuffer sql = new StringBuffer();
        sql.append(" UPDATE " + getTableName(entityClass) + " SET ");
        for (int i = 0; fields != null && i < fields.length; i++) {
            String column = getColumn(fields[i]);
            if (isKey(keys, column)) { // id 代表主键
                continue;
            }
            Object value = getPropertyValue(entity, column);
            TempField tempField = fields[i].getAnnotation(TempField.class);
            if (value != null && tempField == null) {
                sql.append(column).append("=").append("?,");
            }
        }
        sql = sql.deleteCharAt(sql.length() - 1);
        int i = 0;
        sql.append(" WHERE ");
        for (Field field : fields) {
            String name = field.getName();
            if (isKey(keys, name)) { // id 代表主键
                if (i == 0) {
                    sql.append(name.toUpperCase()).append("=").append("?");
                } else {
                    sql.append(" and ").append(name.toUpperCase()).append("=").append("?");
                }
                i++;
            }
        }
        return sql.toString();
    }

    /**
     * 获取dto的Update语句；setNull指定为null的属性是否要更新，true表示更新；
     * 
     * @param entity
     * @param setNull
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:13:53
     */
    public static String getUpdateSql(Object entity, boolean setNull) {
        return getUpdateSql(entity, getTableName(entity.getClass()), setNull);
    }

    public static String getUpdateSql(Object entity, String tableName, boolean setNull) {
        Class<?> entityClass = entity.getClass();
        UpdateKey updateKey = entityClass.getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("update失败,dto未定义updateKey");
        }
        String keys = updateKey.keys();
        Field[] fields = entityClass.getDeclaredFields();
        StringBuffer sql = new StringBuffer();
        sql.append(" UPDATE " + tableName + " SET ");
        for (int i = 0; fields != null && i < fields.length; i++) {
            String column = getColumn(fields[i]);
            if (isKey(keys, column)) { // id 代表主键
                continue;
            }
            Object value = getPropertyValue(entity, column);
            TempField tempField = fields[i].getAnnotation(TempField.class);
            if (value != null && tempField == null) {
                sql.append(column).append("=").append("?,");
            } else if (value == null && tempField == null) {
                if (setNull) {
                    sql.append(column).append("=null,");
                }
            }
        }
        sql = sql.deleteCharAt(sql.length() - 1);
        int i = 0;
        sql.append(" WHERE ");
        for (Field field : fields) {
            String name = field.getName();
            if (isKey(keys, name)) { // id 代表主键
                if (i == 0) {
                    sql.append(name.toUpperCase()).append("=").append("?");
                } else {
                    sql.append(" and ").append(name.toUpperCase()).append("=").append("?");
                }
                i++;
            }
        }
        return sql.toString();
    }

    /**
     * 私有方法，判断属性名是否是主键；不区分大小写；
     * 
     * @param keys
     * @param name
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:14:53
     */
    private static boolean isKey(String keys, String name) {
        String[] split = keys.split(",");
        for (int i = 0; i < split.length; i++) {
            if (split[i].toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取dto的delete语句；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:15:32
     */
    public static String getDeleteSql(Object entity) {
        Class<?> entityClass = entity.getClass();
        UpdateKey updateKey = entityClass.getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("delete失败,dto未定义updateKey");
        }
        String keys = updateKey.keys();
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM " + getTableName(entityClass));
        sql.append(" WHERE ");
        Field[] fields = entityClass.getDeclaredFields();
        int i = 0;
        for (Field field : fields) {
            String name = field.getName();
            if (isKey(keys, name)) { // id 代表主键
                if (i == 0) {
                    sql.append(name.toUpperCase()).append("=").append("?");
                } else {
                    sql.append(" and ").append(name.toUpperCase()).append("=").append("?");
                }
                i++;
            }
        }
        return sql.toString();
    }

    /**
     * 获取dto的insert语句的参数对象数组；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:16:03
     */
    public static Object[] getInsertArgs(Object entity) {
        Class<?> entityClass = entity.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        Object[] args = new Object[fields.length];
        int i = 0;
        for (Field field : fields) {
            String name = field.getName();
            Object value = getPropertyValue(entity, name);
            if (value instanceof NameValueEnum) {
                value = ((NameValueEnum<?>)value).getValue();
            }
            TempField tempField = field.getAnnotation(TempField.class);
            if (value != null && tempField == null) {
                args[i++] = value;
            }
        }
        Object[] copyOfRange = Arrays.copyOfRange(args, 0, i);
        return copyOfRange;
    }

    /**
     * 获取dto对象的update语句的参数对象数组；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:16:27
     */
    public static Object[] getUpdateArgs(Object entity) {
        Class<?> entityClass = entity.getClass();
        UpdateKey updateKey = entityClass.getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("update失败,dto未定义updateKey");
        }
        String keys = updateKey.keys();
        Field[] fields = entityClass.getDeclaredFields();
        Object[] args = new Object[fields.length];
        List<Field> updateField = new ArrayList<>();
        int i = 0;
        for (Field field : fields) {
            String name = field.getName();
            if (isKey(keys, name)) { // id 代表主键
                updateField.add(field);
                continue;
            }
            Object value = getPropertyValue(entity, name);
            if (value instanceof NameValueEnum) {
                value = ((NameValueEnum<?>)value).getValue();
            }
            TempField tempField = field.getAnnotation(TempField.class);
            if (value != null && tempField == null) {
                args[i++] = value;
            }
        }
        for (Field field : updateField) {
            String name = field.getName();
            Object value = getPropertyValue(entity, name);
            if (value != null) {
                args[i++] = value;
            }
        }
        Object[] copyOfRange = Arrays.copyOfRange(args, 0, i);
        return copyOfRange;
    }

    /**
     * 获取dto对象delete语句的参数对象数组；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:16:44
     */
    public static Object[] getDeleteArgs(Object entity) {
        Class<?> entityClass = entity.getClass();
        UpdateKey updateKey = entityClass.getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("delete失败,dto未定义updateKey");
        }
        String keys = updateKey.keys();
        Field[] fields = entityClass.getDeclaredFields();
        Object[] args = new Object[fields.length];
        int i = 0;
        for (Field field : fields) {
            String name = field.getName();
            if (isKey(keys, name)) { // id 代表主键
                Object value = getPropertyValue(entity, name);
                args[i++] = value;
            }
        }
        Object[] copyOfRange = Arrays.copyOfRange(args, 0, i);
        return copyOfRange;
    }

    /**
     * 填充sql字符串中的?字符，用于打印；
     * 
     * @param sql
     * @param parameters
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:17:14
     */
    public static String fillSQL(String sql, Object...parameters) {
        if (parameters == null || parameters.length < 1) {
            return sql;
        }
        String ret = "";
        char[] ss = sql.toCharArray();
        int i = 0;
        for (char s : ss) {
            if (s == '?') {
                if (parameters[i] instanceof Date) {
                    ret = ret + "to_date('" + DateUtil.defaultFormat((Date)parameters[i])
                            + "','yyyy-mm-dd hh24:mi:ss')";
                } else if (parameters[i] instanceof Number) {
                    ret = ret + parameters[i];
                } else if (parameters[i] == null) {
                    ret = ret + "null";
                } else {
                    ret = ret + "'" + parameters[i] + "'";
                }
                i++;
                continue;
            }
            ret = ret + s;
        }
        return ret;
    }

    /**
     * 根据dto对象获取redis对象存储的key；格式为key1|key2|key3；
     * 
     * @param entity
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:18:13
     */
    public static String getRedisMapKey(Object entity) {
        Class<?> entityClass = entity.getClass();
        UpdateKey updateKey = entityClass.getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("->update失败,dto未定义updateKey");
        }
        String keys = updateKey.keys();
        String[] split = keys.split(",");
        String mapKey = "";
        int i = 0;
        for (String key : split) {
            if (i++ > 0) {
                mapKey += "|";
            }
            try {
                Field field = entityClass.getDeclaredField(key);
                field.setAccessible(true);
                Object value = field.get(entity);
                mapKey += value.toString();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("->获取field失败：" + key, e);
            } catch (SecurityException e) {
                throw new RuntimeException("->获取field失败：" + key, e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("->获取值失败：" + key, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("->获取值失败：" + key, e);
            }
        }
        return mapKey;
    }

    /**
     * json字符串转换成对象；使用Gson lib；
     * 
     * @param json
     * @param clz
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:20:07
     */
    public static <T> T jsonToObject(String json, Class<T> clz) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        return jsonToObject(jsonObject, clz);
    }

    /**
     * jsonObject转换成对象；使用Gson lib；
     * 
     * @param jsonObject
     * @param clz
     * @return
     * @author:fuh
     * @createTime:2017年8月8日 上午9:23:13
     */
    public static <T> T jsonToObject(JsonObject jsonObject, Class<T> clz) {
        Field[] declaredFields = clz.getDeclaredFields();// 获取字段
        T obj;
        try {
            obj = clz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("->反射new失败：" + clz, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("->反射new失败：" + clz, e);
        }
        for (Field field : declaredFields) {
            field.setAccessible(true);
            String name = field.getName();
            if (field.getType().equals(String.class)) {// String类型转换

                String val = jsonObject.get(name) == null ? null : jsonObject.get(name).getAsString();

                try {
                    field.set(obj, val);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("->反射set失败：" + name, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("->反射set失败：" + name, e);
                }
            } else if (field.getType().equals(Integer.class)) {// Integer类型转换

                Integer val = jsonObject.get(name) == null ? null : jsonObject.get(name).getAsInt();
                try {
                    field.set(obj, val);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("->反射set失败：" + name, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("->反射set失败：" + name, e);
                }
            } else if (field.getType().equals(Date.class)) {// Integer类型转换

                Date val = jsonObject.get(name) == null ? null : DateUtil.parse(jsonObject.get(name).getAsString(),
                        DateUtil.DATE_TIME_PATTERN);
                try {
                    field.set(obj, val);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("->反射set失败：" + name, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("->反射set失败：" + name, e);
                }
            }

        }
        return obj;
    }
}
