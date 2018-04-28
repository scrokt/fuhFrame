package cn.ymcd.ods.util;


/**
 * 
 * @projectName:pine-ods
 * @author:fuh
 * @date:2018年1月3日 下午2:22:08
 * @version 1.0
 */
public class IPv4Util {

    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2 - position1 - 1));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3 - position2 - 1));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        // 进行左移位处理
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder();
        // 直接右移24位
        sb.append(ip >> 24);
        sb.append(".");
        // a高8位置0，然后右移16
        sb.append((ip & 0x00FFFFFF) >> 16);
        sb.append(".");
        // a高16位置0，然后右移8位
        sb.append((ip & 0x0000FFFF) >> 8);
        sb.append(".");
        // 将高24位置0
        sb.append((ip & 0x000000FF));
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(IPv4Util.longToIp(3232235790l));
    }

}
