/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.convert.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.ConversionException;

/**
 * This class is converts a java.util.Date to a String and a String to a
 * java.util.Date for use as a Timestamp. It is used by BeanUtils when copying
 * properties.
 * 
 * <p>
 * <a href="TimestampConverter.java.html"><i>View Source</i></a>
 * </p>
 *  
 * @author
 */
public class TimestampConverter extends DateConverter {
    public static final String TS_FORMAT = DateUtil.getDatePattern() + " HH:mm:ss.S";

    protected Object convertToDate(Class type, Object value) {
        DateFormat df = new SimpleDateFormat(TS_FORMAT);
        if (value instanceof String) {
            try {
                if (value.toString().isEmpty()) {
                    return null;
                }

                return df.parse((String) value);
            } catch (Exception pe) {
                throw new ConversionException("Error converting String to Timestamp");
            }
        }

        throw new ConversionException("Could not convert "
                + value.getClass().getName() + " to " + type.getName());
    }

    @Override
    protected Object convertToString(Class type, Object value) {
        DateFormat df = new SimpleDateFormat(TS_FORMAT);
        if (value instanceof Date) {
            try {
                return df.format(value);
            } catch (Exception e) {
                throw new ConversionException("Error converting Timestamp to String");
            }
        }

        return value.toString();
    }
}
