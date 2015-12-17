
/**
 * Copyright (c) 2005-2011 springside.org.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 
 * $Id: PropertiesLoader.java 1690 2012-02-22 13:42:00Z calvinxiu $
 */
package com.robin.core.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


public class PropertiesLoader {

	private static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

	private static ResourceLoader resourceLoader = new DefaultResourceLoader();

	private final Properties properties;

	public PropertiesLoader(String... resourcesPaths) {
		properties = loadProperties(resourcesPaths);
	}

	public Properties getProperties() {
		return properties;
	}
	

	/**
	 * 取锟斤拷Property锟斤拷
	 */
	private String getValue(String key) {
		String systemProperty = System.getProperty(key);
		if (systemProperty != null) {
			return systemProperty;
		}
		return properties.getProperty(key);
	}

	/**
	 * 取锟斤拷String锟斤拷锟酵碉拷Property,锟斤拷锟斤拷Null锟斤拷锟阶筹拷锟届常.
	 */
	public String getProperty(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return value;
	}

	/**
	 * 取锟斤拷String锟斤拷锟酵碉拷Property.锟斤拷锟斤拷Null锟絫锟斤拷锟斤拷Default值.
	 */
	public String getProperty(String key, String defaultValue) {
		String value = getValue(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * 取锟斤拷Integer锟斤拷锟酵碉拷Property.锟斤拷锟斤拷Null锟斤拷锟斤拷锟捷达拷锟斤拷锟斤拷锟阶筹拷锟届常.
	 */
	public Integer getInteger(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Integer.valueOf(value);
	}

	/**
	 * 取锟斤拷Integer锟斤拷锟酵碉拷Property.锟斤拷锟斤拷Null锟絫锟斤拷锟斤拷Default值锟斤拷锟斤拷锟斤拷锟斤拷荽锟斤拷锟斤拷锟斤拷壮锟斤拷斐?
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		String value = getValue(key);
		return value != null ? Integer.valueOf(value) : defaultValue;
	}

	/**
	 * 取锟斤拷Double锟斤拷锟酵碉拷Property.锟斤拷锟斤拷Null锟斤拷锟斤拷锟捷达拷锟斤拷锟斤拷锟阶筹拷锟届常.
	 */
	public Double getDouble(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Double.valueOf(value);
	}

	/**
	 * 取锟斤拷Double锟斤拷锟酵碉拷Property.锟斤拷锟斤拷Null锟絫锟斤拷锟斤拷Default值锟斤拷锟斤拷锟斤拷锟斤拷荽锟斤拷锟斤拷锟斤拷壮锟斤拷斐?
	 */
	public Double getDouble(String key, Integer defaultValue) {
		String value = getValue(key);
		return value != null ? Double.valueOf(value) : defaultValue;
	}

	/**
	 * 取锟斤拷Boolean锟斤拷锟酵碉拷Property.锟斤拷锟斤拷Null锟阶筹拷锟届常,锟斤拷锟斤拷锟斤拷莶锟斤拷锟絫rue/false锟津返伙拷false.
	 */
	public Boolean getBoolean(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Boolean.valueOf(value);
	}

	/**
	 * 取锟斤拷Boolean锟斤拷锟酵碉拷Propert.锟斤拷锟斤拷Null锟絫锟斤拷锟斤拷Default值,锟斤拷锟斤拷锟斤拷莶锟轿猼rue/false锟津返伙拷false.
	 */
	public Boolean getBoolean(String key, boolean defaultValue) {
		String value = getValue(key);
		return value != null ? Boolean.valueOf(value) : defaultValue;
	}

	/**
	 * 锟斤拷锟斤拷锟斤拷锟侥硷拷, 锟侥硷拷路锟斤拷使锟斤拷Spring Resource锟斤拷式.
	 */
	private Properties loadProperties(String... resourcesPaths) {
		Properties props = new Properties();

		for (String location : resourcesPaths) {

			logger.debug("Loading properties file from:" + location);

			InputStream is = null;
			try {
				Resource resource = resourceLoader.getResource(location);
				is = resource.getInputStream();
				props.load(is);
			} catch (IOException ex) {
				logger.info("Could not load properties from path:" + location + ", " + ex.getMessage());
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return props;
	}
}
