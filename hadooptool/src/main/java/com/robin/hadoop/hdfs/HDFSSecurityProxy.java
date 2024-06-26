package com.robin.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;

/**
 * <p>Project:  lmtest</p>
 *
 * <p>Description:HDFSSecurityProxy.java</p>
 *
 * <p>Copyright: Copyright (c) 2014 create at 2014-8-19</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@FunctionalInterface
public interface HDFSSecurityProxy {
	Object run(final Configuration config) throws HdfsException;
}
