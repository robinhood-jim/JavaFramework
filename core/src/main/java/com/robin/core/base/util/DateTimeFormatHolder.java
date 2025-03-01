package com.robin.core.base.util;

import org.springframework.util.ObjectUtils;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatHolder {
    private static ThreadLocal<DateTimeFormatter> timestampFormatter=new ThreadLocal<>();
    private static ThreadLocal<DateTimeFormatter> yearFormatter=new ThreadLocal<>();
    private static ThreadLocal<DateTimeFormatter> ymdFormatter=new ThreadLocal<>();
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
    public static DateTimeFormatter getTimestampFormatter(){
        if(ObjectUtils.isEmpty(timestampFormatter.get())){
            timestampFormatter.set(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return timestampFormatter.get();
    }
    public static DateTimeFormatter getYmdFormatter(){
        if(ObjectUtils.isEmpty(timestampFormatter.get())){
            timestampFormatter.set(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return timestampFormatter.get();
    }
    public static DateTimeFormatter getYearFormatter(){
        if(ObjectUtils.isEmpty(timestampFormatter.get())){
            timestampFormatter.set(DateTimeFormatter.ofPattern("yyyy"));
        }
        return timestampFormatter.get();
    }

}
