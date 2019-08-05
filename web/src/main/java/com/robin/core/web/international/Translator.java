package com.robin.core.web.international;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.example.international</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月05日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component
public class Translator {

    private static ResourceBundleMessageSource messageSource;
    @Autowired
    Translator(ResourceBundleMessageSource messageSource){
        Translator.messageSource=messageSource;
    }
    public static String toLocale(String msgCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, null, locale);
    }
}
