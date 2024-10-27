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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ObjectUtils;

@Slf4j
public class SpringContextHolder extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware, DisposableBean, BeanFactoryAware {
    private static ApplicationContext context;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static BeanFactory beanFactory;

    public static void injectApplicationContext(ApplicationContext appcontext) {
        if (context == null) {
            context = appcontext;
        }
    }

    public static void injectBeanFactory(BeanFactory beanFactory1) {
        if (beanFactory == null) {
            beanFactory = beanFactory1;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        injectBeanFactory(beanFactory);
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static <T> T getBean(Class<T> clazzName) {
        T bean=null;
        try {
            try{
                bean= context.getBean(clazzName);
            }catch (BeansException ex){

            }
            if(ObjectUtils.isEmpty(bean)){
                bean=beanFactory.getBean(clazzName);
            }
        } catch (Exception ex) {
            return null;
        }
        return bean;
    }

    public static <T> T getBean(String beanName, Class<T> clazz) throws BeansException {
        T bean=null;
        try{
            bean= context.getBean(beanName, clazz);
        }catch (BeansException ex){
        }
        try {
            if (ObjectUtils.isEmpty(bean)) {
                bean = beanFactory.getBean(beanName, clazz);
            }
        }catch (BeansException ex1){

        }
        return bean;
    }


    @Override
    public void setApplicationContext(ApplicationContext appcontext)
            throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("begin to initalize context!!!");
        }
        injectApplicationContext(appcontext);
        if (logger.isDebugEnabled()) {
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

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
