package com.robin.core.base.util;

import com.robin.core.base.spring.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;

import java.util.Locale;

public class MessageUtils {
    private static MessageSource messageSource;
    private static Environment environment;


    static {
        messageSource = SpringContextHolder.getBean(MessageSource.class);
        environment=SpringContextHolder.getBean(Environment.class);
    }
    public static String getMessage(int errCode){
        return getMessage(errCode,new String[0]);
    }
    public static String getMessage(int errCode,String... args){
        return messageSource.getMessage(String.valueOf(errCode),args, getLocale(null));
    }
    public static String getMessage(int errCode,String language){
        return getMessage(errCode,getLocale(language));
    }
    public static String getMessage(String key){
        return getMessage(key,getLocale(null));
    }
    public static String getMessage(String key,String... args){
        return getMessage(key,getLocale(null),args);
    }
    public static String getMessage(String key,String language){
        return getMessage(key,getLocale(language));
    }
    public static String getMessage(int errCode,Locale locale1,String... params){
        return messageSource.getMessage(String.valueOf(errCode), params, locale1);
    }
    public static String getMessage(String code,Locale locale1,String... params){
        return messageSource.getMessage(code, params, locale1);
    }
    private static Locale getLocale(String languageName){
        if(languageName!=null && !StringUtils.isEmpty(languageName)){
            return Locale.forLanguageTag(languageName);
        }
        else if(environment.containsProperty(Const.LOCALE_KEY)){
            return Locale.forLanguageTag(environment.getProperty(Const.LOCALE_KEY));
        }else{
            return LocaleContextHolder.getLocale();
        }
    }
}
