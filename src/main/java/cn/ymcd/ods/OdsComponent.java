package cn.ymcd.ods;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.ymcd.ods.db.base.dao.SimpleDAO;
import cn.ymcd.ods.util.LogUtil;

public class OdsComponent {

    public static final String DEFAULT_DB = "defaultdb";
    public static final String ELEFENCE_DB = "elefencedb";
    public static final String PICKDATA_DB = "pickdatadb";
    public static final String TM_DB = "tmdb";
    public static final String BR_DB = "brdb";
    public static final String IDENTITY_DB = "identitydb";
    public static final String MPP_DB = "mppdb";

    public static ApplicationContext applicationContext = null;

    static {
        try {
            LogUtil.INIT_LOG.info("->初始化OdsComponent...");
            applicationContext = new ClassPathXmlApplicationContext("ods-config.xml");
            LogUtil.INIT_LOG.info("<-初始化OdsComponent完成。");
        } catch (BeansException e) {
            LogUtil.INIT_LOG.error("<-初始化OdsComponent失败！", e);
        }
    }

    public OdsComponent() {
        // 初始化dataservice组件
    }

    public static <T> T getBean(Class<T> cls) {
        T bean = applicationContext.getBean(cls);
        if (bean == null) {
            throw new RuntimeException("Bean获取失败，请检查：" + cls.getCanonicalName());
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        T bean = (T)applicationContext.getBean(name);
        if (bean == null) {
            throw new RuntimeException("Bean获取失败，请检查：" + name);
        }
        return bean;
    }

    public static SimpleDAO getDefaultdb() {
        return getBean(DEFAULT_DB);
    }

    public static SimpleDAO getElefencedb() {
        return getBean(ELEFENCE_DB);
    }

    public static SimpleDAO getPickdatadb() {
        return getBean(PICKDATA_DB);
    }

    public static SimpleDAO getTmdb() {
        return getBean(TM_DB);
    }

    public static SimpleDAO getBrdb() {
        return getBean(BR_DB);
    }

    public static SimpleDAO getIdentitydb() {
        return getBean(IDENTITY_DB);
    }

    public static SimpleDAO getMppdb() {
        return getBean(MPP_DB);
    }

    public static void main(String[] args) {
        new OdsComponent();
    }
}
