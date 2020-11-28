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
package com.robin.core.base.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Const {
	public static final String	DEFAULT_PAGE_SIZE		= "10";
	public static final String	MAX_PAGE_SIZE			= "500";
	public static final String	MIN_PAGE_SIZE			= "2";
	//login session
	public static final String	SESSION					= "SESS_LOGINUSER";
	public static final String	LOGIN_DEACTIVE			= "0";
	public static final String	LOGIN_ACTIVE			= "1";
	public static final String	LOGIN_VERFIYCODE		= "verify_code";

	public static final String	RESOURCE_TYPE_MENU	= "1";
	public static final String	RESOURCE_TYPE_BUTTON	= "2";
	//field Type
	public static final int		FIELD_TYPE_STRING		= 0;

	public static final int		FIELD_TYPE_INT			= 1;

	public static final int		FIELD_TYPE_INTEGER	= 2;

	public static final int		FIELD_TYPE_DOUBLE		= 3;

	public static final int		FIELD_TYPE_DATE		= 4;

	public static final int		FIELD_TYPE_BOOLEAN	= 5;
	//datameta type
	public static final String	META_TYPE_STRING		= "1";
	public static final String	META_TYPE_DATE			= "2";
	public static final String	META_TYPE_NUMERIC		= "3";
	public static final String	META_TYPE_INTEGER	    = "4";
	public static final String	META_TYPE_BIGINT	    = "5";
	public static final String	META_TYPE_DOUBLE	    = "6";
	public static final String	META_TYPE_BOOLEAN	    = "7";
	public static final String	META_TYPE_BINARY	    = "8";
	public static final String	META_TYPE_TIMESTAMP		= "9";
	public static final String	META_TYPE_CLOB		= "10";
	public static final String	META_TYPE_BLOB		= "11";
	public static final String	META_TYPE_OBJECT		= "12";
	public static final String	META_TYPE_SHORT		= "13";
	//filter type
	public static final String	FILTER_OPER_BETWEEN	= "BT";
	public static final String	FILTER_OPER_LIKE		= "LK";
	public static final String	FILTER_OPER_EQUAL		= "=";
	public static final String	FILTER_OPER_NOTEQUAL	= "!=";
	public static final String	FILTER_OPER_GT			= ">";
	public static final String	FILTER_OPER_GTANDEQL	= ">=";
	public static final String	FILTER_OPER_LT			= "<";
	public static final String	FILTER_OPER_LTANDEQL	= "<=";
	public static final String	FILTER_OPER_IN			= "in";
	public static final String	FILTER_OPER_HAVING	= "having";

	public static final String 	TAG_ENABLE="1";
	public static final String 	TAG_DISABLE="0";
	
	
	public static final String    SYS_CODE_YES 			= "1";
	public static final String	SYS_CODE_NO				= "0";
	
	public static final String DBDUMP_SHELL_PARAM="exp";
	public static final String DBIMP_SHELL_PARAM="imp";
	//URI protocol
	public static final String PREFIX_FTP="ftp";
	public static final String PREFIX_SFTP="sftp";
	public static final String PREFIX_HDFS="hdfs";
	public static final String PREFIX_S3="s3";


	public enum FileFormat{
		TYPE_CSV("1"),       //csv
		TYPE_JSON("2"),   //json
		TYPE_XLSX("3"),     //xlsx
		TYPE_XML("4"),     //xml
		TYPE_PARQUET("5"),  //parquet
		TYPE_AVRO("6");   //avro
		private String value;
		FileFormat(String value){this.value=value;}
		@Override
		public String toString() {
			return String.valueOf(this.value);
		}
	}


	//filesuffix
	public static final String SUFFIX_ZIP="zip";
	public static final String SUFFIX_GZIP="gz";
	public static final String SUFFIX_BZIP2="bz2";
	public static final String SUFFIX_SNAPPY="snappy";
	public static final String SUFFIX_LZO="lzo";
	public static final String SUFFIX_LZMA="lzma";
	public static final String SUFFIX_LZ4="lz4";
	
	//text fileType
	public static final String FILETYPE_PLAINTEXT="1";
	public static final String FILETYPE_JSON="2";
	public static final String FILETYPE_XML="3";
	public static final String FILETYPE_AVRO="4";
	public static final String FILETYPE_PARQUET="5";
	public static final String FILETYPE_PROTOBUF ="6";

	public static final String FILESUFFIX_CSV="csv";
	public static final String FILESUFFIX_JSON="json";
	public static final String FILESUFFIX_XML="xml";
	public static final String FILESUFFIX_AVRO="avro";
	public static final String FILESUFFIX_PARQUET="parquet";
	public static final String FILESUFFIX_PROTOBUF="proto";


	public static final String FILEWRITER_PARQUET_CLASSNAME="com.robin.comm.fileaccess.writer.ParquetFileWriter";
	public static final String FILEWRITER_PROTOBUF_CLASSNAME="com.robin.comm.fileaccess.writer.ProtoBufFileWriter";
	public static final String FILEITERATOR_PARQUET_CLASSNAME="com.robin.comm.fileaccess.iterator.ParquetFileIterator";
	public static final String FILEITERATOR_PROTOBUF_CLASSNAME="com.robin.comm.fileaccess.iterator.ProtoBufFileIterator";

	public static final String RESOURCE_ACCESS_HDFS_CLASSNAME="com.robin.comm.fileaccess.util.HdfsResourceAccessUtil";
	public static final String ITERATOR_AVRO_CLASSNAME="com.robin.comm.fileaccess.iterator.AvroFileIterator";
	
	//InStream enum
	public static final String LOCAL_INSTREAM_CLASS="com.robin.core.local.LocalInStream";
	public static final String FTP_INSTREAM_CLASS="com.robin.core.remote.FtpInStream";
	public static final String HDFS_INSTREAM_CLASS="com.robin.core.hadoop.HdfsInStream";
	//db script Const
	public static final String DB2EXECUTE_SCRIPTS="/scripts/db/db2execute.sh";
	
	public static final String ORACLEEXECUTE_SCRIPTS="/scripts/db/oracleexecute.sh";
	public static final String ORACLEIMP_SCRIPTS="/scripts/db/oracle_import.sh";
	public static final String ORACLEEXP_SCRIPTS="/scripts/db/oracle_export.sh";
	
	public static final String BOOLEAN_VALID="1";
	public static final String BOOLEAN_INVALID="0";
	public static final String HDFS_NAME_HADOOP1 = "fs.default.name";
	public static final String HDFS_NAME_HADOOP2 = "fs.defaultFS";
	
	public static final String AVRO_SCHEMA_FILE_PARAM="schemaPath";
	public static final String AVRO_SCHEMA_CONTENT_PARAM="schemaContent";
	
	public static final String VALID="1";
	public static final String INVALID="0";
	public static final String DEFAULT_DATETIME_FORMAT="yyyy-MM-dd HH:mm:ss";

	public static final Pattern MATCHER_OF_PARAMETER= Pattern.compile("%\\[\\w+\\]");

	public static final String MRFRAME_YARN="yarn";
	public static final String SQL_SELECT="SELECT ";
	public static final String USER_DEFAULTPASSWORD="123456";
	public static final String RESOURCE_ASSIGN_ACCESS="1";
	public static final String RESOURCE_ASSIGN_DENIED="2";
	public static final String LOCALE_KEY="application.locale";

	public enum CompressType{
		COMPRESS_TYPE_GZ("gz"),
		COMPRESS_TYPE_LZO("lzo"),
		COMPRESS_TYPE_SNAPPY("snappy"),
		COMPRESS_TYPE_ZIP("zip"),
		COMPRESS_TYPE_BZ2("bz2"),
		COMPRESS_TYPE_LZMA("lzma"),
		COMPRESS_TYPE_NONE("none");
		private String value;
		CompressType(String value){
			this.value=value;
		}
		@Override
		public String toString(){
			return value;
		}
	}

	public enum RESPONSEBILITY_TYPE{
		SYS_RESP("1"),
		ORG_RESP("2");
		private String value;
		RESPONSEBILITY_TYPE(String value){
			this.value=value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
	public enum META_TYPE{
		METATYPE_CHAR(META_TYPE_STRING,"CHAR"),
		METATYPE_VARCHAR(META_TYPE_STRING,"VARCHAR"),
		METATYPE_BIGINT(META_TYPE_BIGINT,"BIGINT"),
		METATYPE_DOUBLE(META_TYPE_DOUBLE,"DOUBLE"),
		METATYPE_NUMERIC(META_TYPE_DOUBLE,"NUMERIC"),
		METATYPE_TIMESTAMP(META_TYPE_TIMESTAMP,"TIMESTMAP"),
		METATYPE_INT(META_TYPE_INTEGER,"INT");

		private String name;
		private String id;
		META_TYPE(String id,String name){
			this.id=id;
			this.name=name;
		}

		@Override
		public String toString() {
			return name;
		}
		public String getName(){
			return name;
		}
		public String getValue(){
			return id;
		}
	}
	public enum CYCLE_TYPE{
		YEAR("8"),    //年
		QUARTER("7"), //季度
		XUN("6"),  //旬
		MONTH("5"),
		WEEK("4"),
		DAY("3"),
		HOUR("2"),
		MINUTES("1");
		private String value;
		CYCLE_TYPE(String value){
			this.value=value;
		}
		@Override
		public String toString() {
			return this.value;
		}
		public Integer getInt(){
			return Integer.valueOf(value);
		}
	}
	//定时任务触发时间点
	public static final String TRIGGER_TIMESPAN="triggerTimeSpan";

	public final static List<String> ESCAPE_CHARACTERS = new ArrayList<String>(
			Arrays.asList("$", "(", ")", "*", "+", ".", "[",
					"?", "\\", "^", "{", "|"));
}
