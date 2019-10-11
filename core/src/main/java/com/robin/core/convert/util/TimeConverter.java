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

import java.sql.Time;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TimeConverter implements Converter {
	private static Log log = LogFactory.getLog(TimeConverter.class);
	
	public TimeConverter() {
		super();
	}
	
    @Override
    public Object convert(Class type, Object value) {
    	log.debug("convert type:" + type + " value:" + value) ;
    	
        if (value == null) {
            return null;
        } else if (type == Time.class) {
            return Time.valueOf((String)value) ;
        } else if (type == String.class) {
            return value.toString() ;
        }

        throw new ConversionException("Could not convert " +
                                      value.getClass().getName() + " to " +
                                      type.getName());
    }
    
   
}
