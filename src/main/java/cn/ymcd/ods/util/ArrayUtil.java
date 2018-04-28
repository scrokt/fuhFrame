package cn.ymcd.ods.util;

import java.util.Arrays;

public class ArrayUtil {

    public static <T> T[] add(T[] array,T obj) {
        T[] copyOf = Arrays.copyOf(array,array.length + 1);
        copyOf[array.length] = obj;
        return copyOf;
    }
    
    public static <T> T[] add(T[] array,T[] array2) {
        T[] copyOf = Arrays.copyOf(array,array.length + array2.length);
        for(int i=0;i<array2.length;i++){
            copyOf[array.length+i] = array2[i];
        }
        return copyOf;
    }
}
