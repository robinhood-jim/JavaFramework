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
package com.robin.core.template.util;

import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletContext;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModelException;


public class FreeMarkerUtil {

	private  Configuration config;
	private  String templatePath="/template";
	private ServletContext context;
	public  Configuration getConfiguration(){
		if(config==null){
			config=new Configuration();
			config.setDefaultEncoding("UTF-8");
			config.setLocale(Locale.ENGLISH);
			if(context!=null) {
                config.setServletContextForTemplateLoading(context, templatePath);
            } else {
                config.setClassForTemplateLoading(FreeMarkerUtil.class, templatePath);
            }
		}
		return config;
		
	}
	public FreeMarkerUtil(ServletContext context,String templatePath){
		this.context=context;
		if(templatePath!=null && !"".equals(templatePath)) {
            this.templatePath=templatePath;
        }
		getConfiguration();
	}
	public FreeMarkerUtil(String templatePath){
		if(templatePath!=null && !templatePath.isEmpty()) {
            this.templatePath=templatePath;
        }
		getConfiguration();
	}
	public void setShareVariable(String name,Object obj) throws TemplateModelException{
		config.setSharedVariable(name, obj);
	}
	public void process(String templateName,Object rootMap,PrintWriter w) throws Exception{
		getConfiguration();
		Template template=config.getTemplate(templateName, Locale.ENGLISH, "UTF-8");
		template.process(rootMap, w);
	}
	public void process(Configuration configuration,String templateName,Object rootMap,PrintWriter w) throws Exception{
		getConfiguration();
		configuration.getTemplate(templateName).process(rootMap, w);
	}
	public String getTemplatePath() {
		return templatePath;
	}
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
	
	
}
