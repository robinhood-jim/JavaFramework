package com.robin.core.base.util;

import org.springframework.util.ObjectUtils;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatHolder {
    private static ThreadLocal<DateTimeFormatter> timestampFormatter=new ThreadLocal<>();
    private static ThreadLocal<DateTimeFormatter> yearFormatter=new ThreadLocal<>();
    private static ThreadLocal<DateTimeFormatter> ymdFormatter=new ThreadLocal<>();
    private static  ThreadLocal<String> timestampPattern=new ThreadLocal<>();
    private static ThreadLocal<String> ymdPattern=new ThreadLocal<>();
    private static  ThreadLocal<String> yearPattern=new ThreadLocal<>();
    private DateTimeFormatHolder(){

    }
    public static void setTimestampFormatter(DateTimeFormatter tsFormatter){
        timestampFormatter.set(tsFormatter);
    }
    public static void setYearFormatterFormatter(DateTimeFormatter tsFormatter){
        yearFormatter.set(tsFormatter);
    }
    public static void setYmdFormatterFormatter(DateTimeFormatter tsFormatter){
        ymdFormatter.set(tsFormatter);
    }

    public static void setTimestampFormatter(String tsFormatter){
        timestampFormatter.set(DateTimeFormatter.ofPattern(tsFormatter));
        timestampPattern.set(tsFormatter);
    }
    public static void setYearFormatterFormatter(String tsFormatter){
        yearFormatter.set(DateTimeFormatter.ofPattern(tsFormatter));
        yearPattern.set(tsFormatter);
    }
    public static void setYmdFormatterFormatter(String tsFormatter){
        ymdFormatter.set(DateTimeFormatter.ofPattern(tsFormatter));
        ymdPattern.set(tsFormatter);
    }
    public static DateTimeFormatter getTimestampFormatter(){
        if(ObjectUtils.isEmpty(timestampFormatter.get())){
            timestampFormatter.set(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            timestampPattern.set("yyyy-MM-dd HH:mm:ss");
        }
        return timestampFormatter.get();
    }
    public static String getTimestampPattern(){
        if(ObjectUtils.isEmpty(timestampPattern)){
            timestampPattern.set("yyyy-MM-dd HH:mm:ss");
        }
        return timestampPattern.get();
    }
    public static DateTimeFormatter getYmdFormatter(){
        if(ObjectUtils.isEmpty(ymdFormatter.get())){
            ymdFormatter.set(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ymdPattern.set("yyyy-MM-dd");
        }
        return ymdFormatter.get();
    }
    public static String getYmdPattern(){
        if(ObjectUtils.isEmpty(ymdPattern)){
            ymdPattern.set("yyyy-MM-dd");
        }
        return ymdPattern.get();
    }

    public static DateTimeFormatter getYearFormatter(){
        if(ObjectUtils.isEmpty(yearFormatter.get())){
            yearFormatter.set(DateTimeFormatter.ofPattern("yyyy"));
        }
        return yearFormatter.get();
    }

}
