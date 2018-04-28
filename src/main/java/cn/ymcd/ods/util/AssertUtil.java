package cn.ymcd.ods.util;

import org.springframework.util.Assert;

public class AssertUtil {

    /**
     * 场所编号是否正确
     * 
     * @param placecode
     * @return
     */
    public static void validPlacecode(String placecode) {
        if (placecode == null || placecode.trim().length() != 14) {
            throw new IllegalArgumentException("placecode error");
        }
    }

    /**
     * 设备编号是否正确
     * 
     * @param devicecode
     * @return
     */
    public static void validDevicecode(String devicecode) {
        if (devicecode == null || devicecode.trim().length() < 1) {
            throw new IllegalArgumentException("devicecode error");
        }
    }

    /**
     * 是否为空字符串
     * 
     * @param string
     * @return
     */
    public static void notEmpty(String string) {
        if (string == null || string.trim().length() < 1) {
            throw new IllegalArgumentException("string error");
        }
    }

    /**
     * 验证字符串是否超长，如果是null，返回false
     * 
     * @param string
     * @param maxLength
     * @return
     */
    public static void assertTooLong(String string,int maxLength) {
        Assert.notNull(string);
        if (string.trim().length() > maxLength) {
            throw new IllegalArgumentException("string too long");
        }
    }

    public static void assertChannel(String channel) {
        Assert.notNull(channel);
        if (channel.trim().length() > 3) {
            throw new IllegalArgumentException("devicecode error");
        }
    }

    /**
     * 设备状态字符是否正确
     * 
     * @param devicestate
     * @return
     */
    public static void validDeviceState(String devicestate) {
        if (devicestate == null || devicestate.trim().length() != 2) {
            throw new IllegalArgumentException("devicestate error");
        }
    }

    /**
     * 更正设备编号
     * 
     * @param mac
     * @return
     */
    public static String correctMac(String mac) {
        if (mac == null) {
            return null;
        }
        mac = mac.replaceAll("-","");
        mac = mac.replaceAll(" ","");
        mac = mac.toUpperCase();
        return mac;
    }

    /**
     * 更正第一个字符bom
     * 
     * @param string
     * @return
     */
    public static String correctFirstChar(String string) {
        if (string.indexOf('\uFEFF') == 0) {
            string = string.substring(1);
        }
        return string;
    }

    /**
     * 验证经纬度
     * 
     * @param logiOrLati
     * @return
     */
    public static boolean isLogitudeOrLatitudeValid(String logiOrLati) {
        if (logiOrLati == null || logiOrLati.trim().length() > 10) {
            return false;
        }
        return true;
    }

}
