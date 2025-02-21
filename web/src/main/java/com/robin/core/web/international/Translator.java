package com.robin.core.web.international;

import com.robin.core.base.spring.SpringContextHolder;
import org.springframework.context.MessageSource;
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

    public static String toLocale(String msgCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return SpringContextHolder.getBean("messageSource",MessageSource.class).getMessage(msgCode, null, locale);
    }
}
