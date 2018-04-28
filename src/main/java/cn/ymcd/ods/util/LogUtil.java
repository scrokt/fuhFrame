package cn.ymcd.ods.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 
 * @projectName:pine-ods
 * @author:fuh
 * @date:2017年10月18日 下午4:09:43
 * @version 1.0
 */
public class LogUtil {

    /**
     * 打印SQL
     */
    public static final Logger SQL_LOG = LoggerFactory.getLogger("sql");

    /**
     * 系统启动
     */
    public static final Logger INIT_LOG = LoggerFactory.getLogger("init");

    /**
     * 系统异常
     */
    public static final Logger ERROR_LOG = LoggerFactory.getLogger("errorLog");

    /**
     * 后台任务
     */
    public static final Logger TASK_LOG = LoggerFactory.getLogger("task");

    /**
     * 统计相关
     */
    public static final Logger COUNT_LOG = LoggerFactory.getLogger("countLog");
}
