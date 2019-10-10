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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class is converts a java.util.Date to a String
 * and a String to a java.util.Date. 
 * 
 * <p>
 * <a href="DateConverter.java.html"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class DateConverter implements Converter {
	private static final Log log = LogFactory.getLog(DateConverter.class) ;
	
    public static final String TS_FORMAT = DateUtil.getDatePattern() + " HH:mm:ss.S";

    public Object convert(Class type, Object value) {
        if (value == null) {
            return null;
        } else if (type == Timestamp.class) {
            return convertToDate(type, value, TS_FORMAT);
        } else if (type == Date.class) {
            return convertToDate(type, value, DateUtil.getDatePattern());
        } else if (type == String.class) {
            return convertToString(type, value);
        }

        throw new ConversionException("Could not convert " +
                                      value.getClass().getName() + " to " +
                                      type.getName());
    }

    protected Object convertToDate(Class type, Object value, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        if (value instanceof String) {
            try {
                if (value.toString().isEmpty()) {
                    return null;
                }

                Date date = df.parse((String) value);
                
                if (type.equals(Timestamp.class)) {
                    return new Timestamp(date.getTime());
                }
                
                return date;
                
            } catch (Exception pe) {
            	log.debug("convertToDate: value=" + value) ;
                throw new ConversionException("Error converting String to Date");
            }
        }

        throw new ConversionException("Could not convert " +
                                      value.getClass().getName() + " to " +
                                      type.getName());
    }

    protected Object convertToString(Class type, Object value) {        

        if (value instanceof Date) {
            DateFormat df = new SimpleDateFormat(DateUtil.getDatePattern());
            if (value instanceof Timestamp) {
                df = new SimpleDateFormat(TS_FORMAT);
            } 
    
            try {
                return df.format(value);
            } catch (Exception e) {
                throw new ConversionException("Error converting Date to String");
            }
        } else {
            return value.toString();
        }
    }
    public static String convertToString(String value,String orgFormat,String newFormat){
    	SimpleDateFormat format=new SimpleDateFormat(newFormat);
    	SimpleDateFormat oldformat=new SimpleDateFormat(orgFormat);
    	String retval=value;
    	try{
    		retval= format.format(oldformat.parse(value));
    	}catch (Exception e) {
			e.printStackTrace();
		}
    	return retval;
    }
}
