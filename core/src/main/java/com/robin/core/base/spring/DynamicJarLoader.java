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

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.context.DefaultContextLoader;


public class DynamicJarLoader {
	private JarClassLoader jcl=null;

	public GenericApplicationContext loadJar(InputStream jarFile,InputStream config){
		GenericApplicationContext ctx=new GenericApplicationContext();
		/*ClassPathBeanDefinitionScanner scanner=new ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) SpringContextHolder.getApplicationContext().getAutowireCapableBeanFactory());
		DefaultListableBeanFactory factory=(DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
		factory.setBeanClassLoader(getClass().getClassLoader());*/
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(ctx);
		jcl=new JarClassLoader();
		try{
			 jcl.add(jarFile);
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			classBeanDefinitionReader.setEntityResolver(new ResourceEntityResolver(ctx));
			classBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			classBeanDefinitionReader.loadBeanDefinitions(new InputStreamResource(config));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ctx;
	}
	public GenericApplicationContext loadJar(List<InputStream> jarFiles,List<InputStream> configFiles){
		GenericApplicationContext ctx=new GenericApplicationContext();
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(ctx);
		jcl=new JarClassLoader();
		
		try{
			for (InputStream stream:jarFiles) {
				 jcl.add(stream);
			}
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			classBeanDefinitionReader.setEntityResolver(new ResourceEntityResolver(ctx));
			classBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			for (InputStream stream:configFiles) {
				classBeanDefinitionReader.loadBeanDefinitions(new InputStreamResource(stream));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			try{
			for (InputStream stream:jarFiles) {
				 if(stream!=null) {
                     stream.close();
                 }
			}
			for (InputStream stream:configFiles) {
				 if(stream!=null) {
                     stream.close();
                 }
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return ctx;
	}
	public GenericApplicationContext loadJar(List<InputStream> jarFiles,String classpathConfig){
		GenericApplicationContext ctx=new GenericApplicationContext();
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(ctx);
		jcl=new JarClassLoader();
		try{
			for (InputStream stream:jarFiles) {
				 jcl.add(stream);
			}
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			classBeanDefinitionReader.setResourceLoader(new PathMatchingResourcePatternResolver(jcl));
			classBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource(classpathConfig, jcl));
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
			for (InputStream stream:jarFiles) {
				 if(stream!=null) {
                     stream.close();
                 }
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return ctx;
	}
	public GenericApplicationContext loadJarWithConfig(List<InputStream> jarFiles,String jarName,List<String> configFiles){
		GenericApplicationContext ctx=new GenericApplicationContext();
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(ctx);
		jcl=new JarClassLoader();
		try{
			for (InputStream stream:jarFiles) {
				 jcl.add(stream);
			}
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			classBeanDefinitionReader.setEntityResolver(new ResourceEntityResolver(ctx));
			for (int i = 0; i < configFiles.size(); i++) {
				classBeanDefinitionReader.loadBeanDefinitions(new ByteArrayResource(configFiles.get(i).getBytes()));
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
			for (InputStream stream:jarFiles) {
				 if(stream!=null) {
                     stream.close();
                 }
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return ctx;
	}
	public GenericApplicationContext loadJarWithInnerConfig(List<InputStream> jarFiles,String jarName,List<String> configFiles){
		GenericApplicationContext ctx=new GenericApplicationContext();
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(ctx);
		jcl=new JarClassLoader();
		try{
			for (InputStream stream:jarFiles) {
				 jcl.add(stream);
			}
			 jcl.getSystemLoader().setOrder(1); // Look in system class loader first
			 jcl.getParentLoader().setOrder(2); // if not found look in parent class loader
			  jcl.getLocalLoader().setOrder(3); // if not found look in local class loader
			  jcl.getThreadLoader().setOrder(4); // if not found look in thread context class loader
			  jcl.getCurrentLoader().setOrder(5); 
			  DefaultContextLoader context=new DefaultContextLoader(jcl);
			  context.loadContext();
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			//classBeanDefinitionReader.setResourceLoader(new PathMatchingResourcePatternResolver(jcl));
			for (String config:configFiles) {
				classBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource(config, jcl));
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
			for (InputStream stream:jarFiles) {
				 if(stream!=null) {
                     stream.close();
                 }
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return ctx;
	}
	public GenericApplicationContext loadJarWithInnerConfig(List<String> jarFiles,List<String> configFiles){
		GenericApplicationContext ctx=new GenericApplicationContext();
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(ctx);
		jcl=new JarClassLoader();
		try{
			for (String filename:jarFiles) {
				 jcl.add(filename);
			}
			 DefaultContextLoader context=new DefaultContextLoader(jcl);
			  context.loadContext();
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			//classBeanDefinitionReader.setResourceLoader(new PathMatchingResourcePatternResolver(jcl));
			for (String config:configFiles) {
				classBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource(config, jcl));
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ctx;
	}
	public void addConfiguration(GenericApplicationContext context,InputStream configfile){
		XmlBeanDefinitionReader classBeanDefinitionReader=new XmlBeanDefinitionReader(context);
		try{
			classBeanDefinitionReader.setBeanClassLoader(jcl);
			classBeanDefinitionReader.setEntityResolver(new ResourceEntityResolver(context));
			classBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			classBeanDefinitionReader.loadBeanDefinitions(new InputStreamResource(configfile));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public byte[] getClassloadResource(String name){
		return jcl.getLoadedResources().get(name);
	}

}
