package com.robin.core.web.international;


import com.google.common.collect.Lists;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
public class CustomLocaleResolver extends AcceptHeaderLocaleResolver {
    List<Locale> LOCALES= Lists.newArrayList(new Locale("zh"),new Locale("en"));

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLang = request.getHeader("Accept-Language");
        return headerLang == null || headerLang.isEmpty()
                ? Locale.getDefault()
                : Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES);
    }
}
