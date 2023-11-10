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

import com.robin.core.version.VersionInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class SpringContextHolder implements ApplicationContextAware, DisposableBean{
	private static ApplicationContext context;
	private final Logger logger=LoggerFactory.getLogger(getClass());
	public SpringContextHolder(){
		log.info(VersionInfo.getInstance().getVersion());
	}
	
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
	public static <T> T  getBean(Class<T> clazzName){
		try {
			return context.getBean(clazzName);
		}catch (Exception ex){
			return null;
		}
	}

	public static <T> T getBean(String beanName, Class<T> clazz){
		return context.getBean(beanName, clazz);
	}


	@Override
    public void setApplicationContext(ApplicationContext appcontext)
			throws BeansException {
		if(logger.isDebugEnabled()) {
            logger.debug("begin to initalize context!!!");
        }
		injectApplicationContext(appcontext);
		if(logger.isDebugEnabled()) {
            logger.debug("end to initalize context!!!");
        }
	}

	public static ApplicationContext getContext() {
		return context;
	}

	@Override
	public void destroy() throws Exception {
		context = null;
	}
}
