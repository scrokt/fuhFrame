package cn.ymcd.ods.util;


/**
 * 区域转换
 * 
 * @author fuh
 * 
 */
public interface AreaChanger {

    /**
     * 区域转换，如果转换表中没有，返回自己
     * 
     * @param area
     * @return
     */
    public String getChangeArea(String area);
}
