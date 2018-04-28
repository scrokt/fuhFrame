package cn.ymcd.ods.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期处理工具类，应当尽量精简，不要引用非JDK的类
 * 
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年8月7日 下午4:55:37
 * @version 1.0
 */
public class DateUtil {

    /**
     * 日期和时间格式，默认格式;yyyy-MM-dd HH:mm:ss;
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 日期和时间格式，斜杠分割日期;yyyy/MM/dd HH:mm:ss;
     */
    public static final String DATE_TIME_I_PATTERN = "yyyy/MM/dd HH:mm:ss";
    /**
     * 日期时间中文格式;yyyy年MM月dd日 HH时mm分ss秒;
     */
    public static final String DATE_CHN_PATTERN = "yyyy年MM月dd日 HH时mm分ss秒";
    /**
     * 日期和时间紧凑格式，用于消息传递;yyyyMMddHHmmss;
     */
    public static final String DATE_MSG_PATTERN = "yyyyMMddHHmmss";
    /**
     * 日期加小时格式;yyyyMMdd-HH;
     */
    public static final String DATE_HOUR_PATTERN = "yyyyMMdd-HH";
    /**
     * 日期加小时分钟格式;yyyyMMdd-HHmm;
     */
    public static final String DATE_HOUR_MIN_PATTERN = "yyyyMMdd-HHmm";
    /**
     * 日期格式;yyyyMMdd;
     */
    public static final String DATE_PATTERN = "yyyyMMdd";
    /**
     * 日期格式，使用-隔开;yyyy-MM-dd;
     */
    public static final String DATE_SPLIT_PATTERN = "yyyy-MM-dd";
    /**
     * 日期中文格式，用于打印;yyyy年MM月dd日;
     */
    public static final String DATE_TIME_CHN_PATTERN = "yyyy年MM月dd日";
    /**
     * 日期格式，使用/隔开;yyyy/MM/dd;
     */
    public static final String DATE_SPLIT_I_PATTERN = "yyyy/MM/dd";
    /**
     * 时间格式，使用:隔开;HH:mm:ss;
     */
    public static final String TIME_SPLIT_PATTERN = "HH:mm:ss";
    /**
     * 时间格式;HHmmss;
     */
    public static final String TIME_PATTERN = "HHmmss";

    /**
     * 如果日期为null，返回null；如果pattern为null，使用DATE_TIME_PATTERN
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        if (pattern == null) {
            pattern = DateUtil.DATE_TIME_PATTERN;
        }
        return getSdf(pattern).format(date);
    }

    /**
     * 使用默认格式
     * 
     * @param date
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午4:58:36
     */
    public static String defaultFormat(Date date) {
        return format(date, null);
    }

    /**
     * 根据给定的模式将字符串转换成日期，转换失败返回null
     * 
     * @param dateStr
     * @param pattern
     * @return 返回日期或者null
     * @author:fuh
     * @createTime:2017年8月7日 下午4:47:48
     */
    public static Date parse(String dateStr, String pattern) {
        try {
            return getSdf(pattern).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取当前Timestamp
     * 
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午4:48:37
     */
    public static Timestamp getNowTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 特定的转换，输入为绝对秒数的字符串
     * 
     * @param absSeconds
     *            1970至今的绝对秒数
     * @return timestamp或者null
     * @author:fuh
     * @createTime:2017年8月7日 下午4:45:36
     */
    public static Timestamp getTimeFromStr(String absSeconds) {
        if (absSeconds == null || "".equals(absSeconds) || "0".equals(absSeconds)) {
            return null;
        }
        Timestamp time = new Timestamp(Long.parseLong(absSeconds + "000"));
        return time;
    }

    public static Timestamp getTimeFromMiliSenconds(String absMiliSeconds) {
        if (absMiliSeconds == null || "".equals(absMiliSeconds) || "0".equals(absMiliSeconds)) {
            return null;
        }
        Timestamp time = new Timestamp(Long.parseLong(absMiliSeconds));
        return time;
    }
    
    public static String formatHour(Date date){
        return format(date,DATE_HOUR_PATTERN);
    }
    
    public static String formatDay(Date date){
        return format(date,DATE_PATTERN);
    }

    /**
     * 默认的字符串转日期,DateUtil.DATE_TIME_PATTERN
     * 
     * @param datetime
     * @return
     * @author:fuh
     * @createTime:2017年8月7日 下午4:45:27
     */
    public static Date fromString(String datetime) {
        return parse(datetime, DateUtil.DATE_TIME_PATTERN);
    }

    /** 锁对象 */
    private static final Object lockObj = new Object();
    /** 存放不同的日期模板格式的sdf的Map */
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    /**
     * 返回一个ThreadLocal的sdf,每个线程只会new一次sdf
     * 
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    // 只有Map中还没有这个pattern的sdf才会生成新的sdf并放入map
                    // System.out.println("put new sdf of pattern " + pattern + " to map");
                    // 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
                    tl = new ThreadLocal<SimpleDateFormat>() {

                        @Override
                        protected SimpleDateFormat initialValue() {
                            // System.out.println("thread: " + Thread.currentThread() + " init pattern: " + pattern);
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }
}
