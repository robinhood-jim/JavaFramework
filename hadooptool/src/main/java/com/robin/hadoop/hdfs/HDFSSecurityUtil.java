package com.robin.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.security.PrivilegedAction;
import java.util.function.Function;


public class HDFSSecurityUtil {
	private HDFSSecurityUtil(){

	}
	private static final Logger logger= LoggerFactory.getLogger(HDFSSecurityUtil.class);

	static<T> T executeSecurityWithProxy(final Configuration config,@NonNull final Function<Configuration,T> consumer){
		try {
			UserGroupInformation.getCurrentUser().checkTGTAndReloginFromKeytab();
			return UserGroupInformation.getCurrentUser().doAs((PrivilegedAction<T>) () -> consumer.apply(config));
		}catch(Exception ex){
			logger.error("{}",ex.getMessage());
		}
		return null;
	}

}
