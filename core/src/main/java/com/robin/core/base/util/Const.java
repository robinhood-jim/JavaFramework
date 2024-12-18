
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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Const {
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String MAX_PAGE_SIZE = "500";
    public static final String MIN_PAGE_SIZE = "2";
    //login session
    public static final String SESSION = "SESS_LOGINUSER";
    public static final String TOKEN="LOGINTOKEN";
    public static final String LOGIN_DEACTIVE = "0";
    public static final String LOGIN_ACTIVE = "1";
    public static final String LOGIN_VERFIYCODE = "verify_code";

    public static final String RESOURCE_TYPE_MENU = "1";
    public static final String RESOURCE_TYPE_BUTTON = "2";
    //field Type
    public static final int FIELD_TYPE_STRING = 0;

    public static final int FIELD_TYPE_INT = 1;

    public static final int FIELD_TYPE_INTEGER = 2;

    public static final int FIELD_TYPE_DOUBLE = 3;

    public static final int FIELD_TYPE_DATE = 4;

    public static final int FIELD_TYPE_BOOLEAN = 5;
    //datameta type
    public static final String META_TYPE_STRING = "1";
    public static final String META_TYPE_DATE = "2";
    public static final String META_TYPE_NUMERIC = "3";
    public static final String META_TYPE_INTEGER = "4";
    public static final String META_TYPE_BIGINT = "5";
    public static final String META_TYPE_DOUBLE = "6";
    public static final String META_TYPE_BOOLEAN = "7";
    public static final String META_TYPE_BINARY = "8";
    public static final String META_TYPE_TIMESTAMP = "9";
    public static final String META_TYPE_CLOB = "10";
    public static final String META_TYPE_BLOB = "11";
    public static final String META_TYPE_OBJECT = "12";
    public static final String META_TYPE_SHORT = "13";
    public static final String META_TYPE_FORMULA = "14";
    public static final String META_TYPE_FLOAT = "15";
    public static final String META_TYPE_DECIMAL = "16";
    //filter type
    public static final String FILTER_OPER_BETWEEN = "BT";
    public static final String FILTER_OPER_LIKE = "LK";
    public static final String FILTER_OPER_EQUAL = "=";
    public static final String FILTER_OPER_NOTEQUAL = "!=";
    public static final String FILTER_OPER_GT = ">";
    public static final String FILTER_OPER_GTANDEQL = ">=";
    public static final String FILTER_OPER_LT = "<";
    public static final String FILTER_OPER_LTANDEQL = "<=";
    public static final String FILTER_OPER_IN = "in";
    public static final String FILTER_OPER_HAVING = "having";

    public static final String TAG_ENABLE = "1";
    public static final String TAG_DISABLE = "0";


    public static final String SYS_CODE_YES = "1";
    public static final String SYS_CODE_NO = "0";

    public static final String DBDUMP_SHELL_PARAM = "exp";
    public static final String DBIMP_SHELL_PARAM = "imp";
    //URI protocol
    public static final String PREFIX_HDFS = "hdfs";
    public static final String PREFIX_S3 = "s3";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String OPERATOR_AND="and";
    public static final String OPERATOR_OR="or";
    public static final Integer COLUMN_VALID = 1;

    public static final Integer COLUMN_INVALID = 0;
    public static final String ITERATOR_PROCESSID="$processId";
    public static final String SUCCESS="success";


    public enum FILEFORMAT {
        TYPE_CSV("1"),       //csv
        TYPE_JSON("2"),   //json
        TYPE_XLSX("3"),     //xlsx
        TYPE_XML("4"),     //xml
        TYPE_PARQUET("5"),  //parquet
        TYPE_AVRO("6");   //avro
        private String value;

        FILEFORMAT(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
    }


    //filesuffix
    public static final String SUFFIX_ZIP = "zip";
    public static final String SUFFIX_GZIP = "gz";
    public static final String SUFFIX_BZIP2 = "bz2";
    public static final String SUFFIX_SNAPPY = "snappy";
    public static final String SUFFIX_LZO = "lzo";
    public static final String SUFFIX_LZMA = "lzma";
    public static final String SUFFIX_LZ4 = "lz4";

    //text fileType
    public static final String FILETYPE_PLAINTEXT = "1";
    public static final String FILETYPE_JSON = "2";
    public static final String FILETYPE_XML = "3";
    public static final String FILETYPE_AVRO = "4";
    public static final String FILETYPE_PARQUET = "5";
    public static final String FILETYPE_PROTOBUF = "6";
    public static final String FILETYPE_ORC = "7";

    public static final String FILESUFFIX_CSV = "csv";
    public static final String FILESUFFIX_JSON = "json";
    public static final String FILESUFFIX_XML = "xml";
    public static final String FILESUFFIX_AVRO = "avro";
    public static final String FILESUFFIX_PARQUET = "parquet";
    public static final String FILESUFFIX_PROTOBUF = "proto";
    public static final String FILESUFFIX_ORC = "orc";
    public static final String FILESUFFIX_YAML = "yml";


    public static final String FILEWRITER_PARQUET_CLASSNAME = "com.robin.comm.fileaccess.writer.ParquetFileWriter";
    public static final String FILEWRITER_AVRO_CLASSNAME = "com.robin.comm.fileaccess.writer.AvroFileWriter";
    public static final String FILEWRITER_PROTOBUF_CLASSNAME = "com.robin.comm.fileaccess.writer.ProtoBufFileWriter";
    public static final String FILEWRITER_ORC_CLASSNAME = "com.robin.comm.fileaccess.writer.OrcFileWriter";
    public static final String FILEITERATOR_PARQUET_CLASSNAME = "com.robin.comm.fileaccess.iterator.ParquetFileIterator";
    public static final String FILEITERATOR_PROTOBUF_CLASSNAME = "com.robin.comm.fileaccess.iterator.ProtoBufFileIterator";
    public static final String FILEITERATOR_ORC_CLASSNAME = "com.robin.comm.fileaccess.iterator.OrcFileIterator";

    public static final String RESOURCE_ACCESS_HDFS_CLASSNAME = "com.robin.comm.fileaccess.util.HdfsResourceAccessUtil";
    public static final String ITERATOR_AVRO_CLASSNAME = "com.robin.comm.fileaccess.iterator.AvroFileIterator";

    //InStream enum
    public static final String LOCAL_INSTREAM_CLASS = "com.robin.core.local.LocalInStream";
    public static final String FTP_INSTREAM_CLASS = "com.robin.core.remote.FtpInStream";
    public static final String HDFS_INSTREAM_CLASS = "com.robin.core.hadoop.HdfsInStream";
    //db script Const
    public static final String DB2EXECUTE_SCRIPTS = "/scripts/db/db2execute.sh";

    public static final String ORACLEEXECUTE_SCRIPTS = "/scripts/db/oracleexecute.sh";
    public static final String ORACLEIMP_SCRIPTS = "/scripts/db/oracle_import.sh";
    public static final String ORACLEEXP_SCRIPTS = "/scripts/db/oracle_export.sh";

    public static final String BOOLEAN_VALID = "1";
    public static final String BOOLEAN_INVALID = "0";
    public static final String HDFS_NAME_HADOOP1 = "fs.default.name";
    public static final String HDFS_NAME_HADOOP2 = "fs.defaultFS";

    public static final String AVRO_SCHEMA_FILE_PARAM = "schemaPath";
    public static final String AVRO_SCHEMA_CONTENT_PARAM = "schemaContent";

    public static final String VALID = "1";
    public static final String INVALID = "0";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final Pattern MATCHER_OF_PARAMETER = Pattern.compile("%\\[\\w+\\]");

    public static final String MRFRAME_YARN = "yarn";
    public static final String SQL_SELECT = "SELECT ";
    public static final String SQL_UPDATE = "UPDATE ";
    public static final String SQL_AS = " AS ";
    public static final String SQL_FROM = " FROM ";
    public static final String SQL_WHERE = " WHERE ";
    public static final String SQL_INSERTINTO = "INSERT INTO ";
    public static final String USER_DEFAULTPASSWORD = "123456";
    public static final String RESOURCE_ASSIGN_ACCESS = "1";
    public static final String RESOURCE_ASSIGN_DENIED = "2";
    public static final String LOCALE_KEY = "application.locale";

    public enum CompressType {
        COMPRESS_TYPE_GZ("gz"),
        COMPRESS_TYPE_LZO("lzo"),
        COMPRESS_TYPE_SNAPPY("snappy"),
        COMPRESS_TYPE_ZIP("zip"),
        COMPRESS_TYPE_BZ2("bz2"),
        COMPRESS_TYPE_LZMA("lzma"),
        COMPRESS_TYPE_LZ4("lz4"),
        COMPRESS_TYPE_ZSTD("zstd"),
        COMPRESS_TYPE_BROTLI("br"),
        COMPRESS_TYPE_XZ("xz"),
        COMPRESS_TYPE_NONE("none");
        private String value;

        CompressType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum RESPONSEBILITY_TYPE {
        SYS_RESP("1"),
        ORG_RESP("2");
        private String value;

        RESPONSEBILITY_TYPE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum META_TYPE {
        METATYPE_CHAR(META_TYPE_STRING, "CHAR"),
        METATYPE_VARCHAR(META_TYPE_STRING, "VARCHAR"),
        METATYPE_BIGINT(META_TYPE_BIGINT, "BIGINT"),
        METATYPE_DOUBLE(META_TYPE_DOUBLE, "DOUBLE"),
        METATYPE_NUMERIC(META_TYPE_DOUBLE, "NUMERIC"),
        METATYPE_TIMESTAMP(META_TYPE_TIMESTAMP, "TIMESTMAP"),
        METATYPE_INT(META_TYPE_INTEGER, "INT");

        private String name;
        private String id;

        META_TYPE(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return id;
        }
    }

    public enum CYCLE_TYPE {
        YEAR("8"),    //年
        QUARTER("7"), //季度
        XUN("6"),  //旬
        MONTH("5"),
        WEEK("4"),
        DAY("3"),
        HOUR("2"),
        MINUTES("1");
        private String value;

        CYCLE_TYPE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public Integer getInt() {
            return Integer.valueOf(value);
        }
    }

    public enum VFS_PROTOCOL {
        FTP("ftp"),
        SFTP("sftp"),
        FTPS("ftps"),
        WEBDAV("webdav"),
        MIME("mime"),
        SMB("smb"),
        HTTP("http"),
        HTTPS("https");

        VFS_PROTOCOL(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }
    }
    public enum OPERATOR {
        EQ("=","="),
        NE("!=","<>"),
        GE(">=",">="),
        LE("<=","<="),
        GT(">",">"),
        LT("<=","<="),
        IN("IN"," IN "),
        NOT("NOT"," NOT "),
        NOTIN("NOTIN"," NOT IN"),
        NVL("NVL","NVL"),
        NULL("NULL","NULL"),
        NOTNULL("NOTNULL","NOTNULL"),
        BETWEEN("BT","BETWEEN"),
        NBT("NBT"," NOT BETWEEN"),
        EXISTS("EXISTS"," EXISTS"),
        NOTEXIST("NOTEXIST"," NOT EXISTS"),
        LIKE("LIKE"," LIKE "),
        NOTLIKE("NOTLIKE"," NOT LIKE"),
        LLIKE("LL"," LIKE "),
        RLIKE("RL", " LIKE "),
        HAVING("HAVING"," HAVING ");

        private String value;
        private String signal;

        OPERATOR(String value,String signal) {
            this.value = value;
            this.signal=signal;
        }


        public String getValue() {
            return String.valueOf(this.value);
        }
        public String getSignal(){
            return signal;
        }
    }
    public enum LINKOPERATOR {
        LINK_AND("AND"," AND "),
        LINK_OR("OR"," OR ");
        private String value;
        private String signal;

        LINKOPERATOR(String value,String signal) {
            this.value = value;
            this.signal=signal;
        }


        public String getValue() {
            return String.valueOf(this.value);
        }
        public String getSignal(){
            return signal;
        }

    }

    public enum HTTPRESPONSECODE{
        OK(200),
        NOTFOUND(404),
        SERVERERR(400),
        ERR(500);
        private int value;
        HTTPRESPONSECODE(int value){
            this.value=value;
        }
        public int getValue(){
            return value;
        }
        public String getValueStr(){
            return String.valueOf(value);
        }
    }
    public enum FILESYSTEM{
        LOCAL("file"),
        VFS("vfs"),
        HDFS("hdfs"),
        S3("s3"),
        ALIYUN("oss"),
        TENCENT("cos"),
        QINIU("qiniu"),
        BAIDU_BOS("bos"),
        HUAWEI_OBS("obs"),
        MINIO("minio");
        private String value;
        FILESYSTEM(String value){
            this.value=value;
        }
        public String getValue(){
            return value;
        }
    }
    public enum ACCESSRESOURCE{
        JDBC("jdbc"),
        MONGO("mongo"),
        KAFAK("kafka"),
        RABBITMQ("rabbitmq"),
        ROCKETMQ("rocket"),
        ZEROQ("zero"),
        HBASE("hbase"),
        CLICKHOUSE("clickhouse"),
        CASSANDRA("cassandra");


        private String value;
        ACCESSRESOURCE(String value){
            this.value=value;
        }
        public String getValue(){
            return value;
        }
    }
    public enum FILEFORMATSTR {
        JSON("json"),
        AVRO("avro"),
        ORC("orc"),
        PARQUET("parquet"),
        PARQUETSTREAM("parquetStream"),
        XML("xml"),
        PLAIN("txt"),
        CSV("csv"),
        PROTOBUF("proto"),

        ARFF("arff");
        private String value;
        FILEFORMATSTR(String value){
            this.value=value;
        }
        public String getValue(){
            return value;
        }
    }

    //定时任务触发时间点
    public static final String TRIGGER_TIMESPAN = "triggerTimeSpan";

    public final static List<String> ESCAPE_CHARACTERS = new ArrayList<>(
            Lists.newArrayList("$", "(", ")", "*", "+", ".", "[",
                    "?", "\\", "^", "{", "|"));

    public static final String ASC = "asc";
    public static final String DESC = "desc";
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "orderField";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    public enum ACCOUNTTYPE{
        ADMIN("1"),
        NORMAL("2");
        private String value;
        ACCOUNTTYPE(String value){
            this.value=value;
        }

        public String getValue() {
            return value;
        }
    }
    public enum JOINTYPE{
        INNER("INNER"),
        LEFT("LEFT"),
        RIGHT("RIGHT"),
        OUT("OUT");

        private String value;
        JOINTYPE(String value){
            this.value=value;
        }

        public String getValue() {
            return value;
        }
    }
    public enum ROLEDEF{
        ADMIN(1L,"admin"),
        NORMAL(2L,"normal");
        ROLEDEF(Long id,String code){
            this.id=id;
            this.code=code;
        }
        private Long id;
        private String code;

        public Long getId() {
            return id;
        }
        public String getCode() {
            return code;
        }
    }
}
