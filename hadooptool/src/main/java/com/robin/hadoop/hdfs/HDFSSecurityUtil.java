package com.robin.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class HDFSSecurityUtil {
	private static Logger logger= LoggerFactory.getLogger(HDFSSecurityUtil.class);

	static Object executeHdfsMethodWithSecurity(final Configuration config,final String methodName, final Object[] param) {
		List<Class> list = new ArrayList<Class>();
		for (int i = 0; i < param.length; i++) {
			list.add(param[i].getClass());
		}
		Class[] classArr = new Class[list.size()];
		for (int i = 0; i < list.size(); i++) {
			classArr[i] = list.get(i);
		}
		Object ret = null;
		try {
			final Method method = HDFSCallUtil.class.getDeclaredMethod(methodName, classArr);
			UserGroupInformation.getCurrentUser().checkTGTAndReloginFromKeytab();
			ret = UserGroupInformation.getCurrentUser().doAs((PrivilegedAction<Object>) () -> {
				try {
					return method.invoke(null, param);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			});
		} catch (Exception ex) {
			logger.error("{}",ex.getMessage());
		}
		return ret;
	}
	static Object executeHadoopMethodWithSecurity(final Configuration config,final Object obj,final String methodName, final Object[] param) {
		List<Class> list = new ArrayList<Class>();
		for (int i = 0; i < param.length; i++) {
			list.add(param[i].getClass());
		}
		Class[] classArr = new Class[list.size()];
		for (int i = 0; i < list.size(); i++) {
			classArr[i] = list.get(i);
		}
		Object ret = null;
		try {
			final Method method = obj.getClass().getMethod(methodName, classArr);
			UserGroupInformation.getCurrentUser().checkTGTAndReloginFromKeytab();
			ret = UserGroupInformation.getCurrentUser().doAs((PrivilegedAction<Object>) () -> {
				try {
					return method.invoke(obj, param);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			});
		}catch(Exception ex){
			logger.error("{}",ex.getMessage());
		}
		return ret;
	}
	static<T> T executeSecurityWithProxy(final Configuration config, final Function<Configuration,T> consumer){
		T ret = null;
		try {
			UserGroupInformation.getCurrentUser().checkTGTAndReloginFromKeytab();
			UserGroupInformation.getCurrentUser().doAs((PrivilegedAction<Object>) () -> consumer.apply(config));
			
		}catch(Exception ex){
			logger.error("{}",ex.getMessage());
		}
		return ret;
	}

}
