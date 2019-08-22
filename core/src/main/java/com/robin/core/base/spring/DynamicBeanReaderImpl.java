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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class DynamicBeanReaderImpl implements DynamicBeanReader,ApplicationContextAware, InitializingBean {
	 private static Log logger = LogFactory.getLog(DynamicBeanReaderImpl.class);
	private XmlBeanDefinitionReader beanDefinitionReader;  
    
    private ConfigurableApplicationContext applicationContext = null;

    @Override
    public void afterPropertiesSet() {
        beanDefinitionReader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)
                applicationContext.getBeanFactory());
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(applicationContext));
    }


	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.applicationContext=(ConfigurableApplicationContext) context;
		
	}
	public void loadBean(DynamicBean dynamicBean){
        long startTime = System.currentTimeMillis();  
        String beanName = dynamicBean.getBeanName();  
        if(applicationContext.containsBean(beanName)){  
            logger.warn("bean Name"+beanName+"already Exist!");  
            return;  
        }  
        beanDefinitionReader.loadBeanDefinitions(new DynamicResource(dynamicBean));

        logger.info("load bean"+dynamicBean.getBeanName()+" cost time "+(System.currentTimeMillis()-startTime)+"Second.");  
    }   
	

}
