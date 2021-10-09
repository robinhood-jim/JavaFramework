package com.robin.hadoop.hdfs;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;


public class HDFSSecurityUtil {
	private static Logger logger=Logger.getLogger(HDFSSecurityUtil.class);

	public static Object executeHdfsMethodWithSecurity(final Configuration config,final String methodName, final Object[] param) {
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
			ex.printStackTrace();
			logger.error(ex);
		}
		return ret;
	}
	public static Object executeHadoopMethodWithSecurity(final Configuration config,final Object obj,final String methodName, final Object[] param) {
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
			ex.printStackTrace();
			logger.error(ex);
		}
		return ret;
	}
	public static Object executeSecurityWithProxy(final Configuration config,final HDFSSecurityProxy proxy){
		Object ret = null;
		try {
			UserGroupInformation.getCurrentUser().checkTGTAndReloginFromKeytab();
			UserGroupInformation.getCurrentUser().doAs((PrivilegedAction<Object>) () -> proxy.run(config));
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}

}
