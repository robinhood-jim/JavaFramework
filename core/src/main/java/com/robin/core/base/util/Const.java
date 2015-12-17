package com.robin.core.base.util;

public class Const {
	public static String	DEFAULT_PAGE_SIZE		= "10";
	public static String	MAX_PAGE_SIZE			= "500";
	public static String	MIN_PAGE_SIZE			= "2";
	//login session
	public static String	SESSION					= "SESS_LOGINUSER";
	public static String	LOGIN_DEACTIVE			= "0";
	public static String	LOGIN_ACTIVE			= "1";
	public static String	LOGIN_VERFIYCODE		= "verify_code";

	public static String	RESOURCE_TYPE_MENU	= "1";
	public static String	RESOURCE_TYPE_BUTTON	= "2";
	//field Type
	public static int		FIELD_TYPE_STRING		= 0;

	public static int		FIELD_TYPE_INT			= 1;

	public static int		FIELD_TYPE_INTEGER	= 2;

	public static int		FIELD_TYPE_DOUBLE		= 3;

	public static int		FIELD_TYPE_DATE		= 4;

	public static int		FIELD_TYPE_BOOLEAN	= 5;
	//datameta type
	public static String	META_TYPE_STRING		= "1";
	public static String	META_TYPE_DATE			= "2";
	public static String	META_TYPE_NUMERIC		= "3";
	public static String	META_TYPE_INTEGER	    = "4";
	public static String	META_TYPE_BIGINT	    = "5";
	public static String	META_TYPE_DOUBLE	    = "6";
	public static String	META_TYPE_BOOLEAN	    = "7";
	public static String	META_TYPE_BINARY	    = "8";
	public static String	META_TYPE_TIMESTAMP		= "9";
	//filter type
	public static String	FILTER_OPER_BETWEEN	= "BT";
	public static String	FILTER_OPER_LIKE		= "LK";
	public static String	FILTER_OPER_EQUAL		= "=";
	public static String	FILTER_OPER_NOTEQUAL	= "!=";
	public static String	FILTER_OPER_GT			= ">";
	public static String	FILTER_OPER_GTANDEQL	= ">=";
	public static String	FILTER_OPER_LT			= "<";
	public static String	FILTER_OPER_LTANDEQL	= "<=";
	public static String	FILTER_OPER_IN			= "in";
	public static String	FILTER_OPER_HAVING	= "having";

	public static String 	TAG_ENABLE="1";
	public static String 	TAG_DISABLE="0";
	
	
	public static String    SYS_CODE_YES 			= "1";
	public static String	SYS_CODE_NO				= "0";
	
	public static String DBDUMP_SHELL_PARAM="exp";
	public static String DBIMP_SHELL_PARAM="imp";
	//URI protocol
	public static final String PREFIX_FTP="ftp";
	public static final String PREFIX_SFTP="sftp";
	public static final String PREFIX_HDFS="hdfs";
	public static final String PREFIX_S3="s3";
	//filesuffix
	public static final String SUFFIX_ZIP="zip";
	public static final String SUFFIX_GZIP="gz";
	public static final String SUFFIX_BZIP2="bz2";
	
	
	//InStream enum
	public static final String LOCAL_INSTREAM_CLASS="com.robin.core.local.LocalInStream";
	public static final String FTP_INSTREAM_CLASS="com.robin.core.remote.FtpInStream";
	public static final String HDFS_INSTREAM_CLASS="com.robin.core.hadoop.HdfsInStream";

}
