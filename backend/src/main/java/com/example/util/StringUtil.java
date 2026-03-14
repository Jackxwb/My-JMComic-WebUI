package com.example.util;

import java.lang.reflect.Array;

public class StringUtil {
    public static boolean isNull(String str){
        return str == null || str.isBlank();
    }
    public static boolean notNull(String str){
        return !isNull(str);
    }
    public static boolean isNull(String[] strs){
        return strs == null || strs.length == 0;
    }
    public static boolean notNull(String[] str){
        return !isNull(str);
    }
    public static boolean isNull(Integer integer){
        return integer==null || integer==0;
    }
    public static boolean notNull(Integer integer){
        return !isNull(integer);
    }
}
