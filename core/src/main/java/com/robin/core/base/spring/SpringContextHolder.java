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
package com.robin.core.base.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHolder implements ApplicationContextAware, DisposableBean{
	private static ApplicationContext context;
	private Logger logger=LoggerFactory.getLogger(getClass());
	
	public static void injectApplicationContext(ApplicationContext appcontext){
		if (context == null){
			context = appcontext;
		}
	}
	
	public static ApplicationContext getApplicationContext(){
		return context;
	}
	
	public static Object getBean(String beanName){
		return context.getBean(beanName);
	}
	public static Object getBean(Class<?> clazzName){
		return context.getBean(clazzName);
	}
	
	public static Object getBean(String beanName, Class<?> clazz){
		return context.getBean(beanName, clazz);
	}


	public void setApplicationContext(ApplicationContext appcontext)
			throws BeansException {
		logger.info("begin to initalize context!!!");
		logger.info("context=",appcontext);
		System.err.println("appcontent="+appcontext);
		injectApplicationContext(appcontext);
	}


	public void destroy() throws Exception {
		context = null;
	}
}
