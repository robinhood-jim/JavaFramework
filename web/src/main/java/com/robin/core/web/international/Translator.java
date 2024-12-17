package com.robin.core.web.international;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.example.international</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月05日</p>
 * <p>
 * <p>Company: </p>
 *
 * @author robinjim
 * @version 1.0
 */
public class Translator {

    private static ResourceBundleMessageSource messageSource;

    public Translator(ResourceBundleMessageSource messageSource){
        this.messageSource=messageSource;
    }
    public static String toLocale(String msgCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, null, locale);
    }
}
