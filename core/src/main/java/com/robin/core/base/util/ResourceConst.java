package com.robin.core.base.util;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.base.util</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年11月07日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ResourceConst {
    public  enum ResourceType{
        TYPE_LOCALFILE("0"),  //local
        TYPE_HDFSFILE("1"),   //hdfs
        TYPE_FTPFILE("2"),     //ftp
        TYPE_SFTPFILE("3"), //sftp
        TYPE_DB("4"),  //db
        TYPE_REDIS("5"), //redis
        TYPE_MONGODB("6"); //mongodb
        private String value;

        ResourceType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
    }
    public enum FileFormat{
        TYPE_CSV("1"),       //csv
        TYPE_JSON("2"),   //json
        TYPE_XLSX("3"),     //xlsx
        TYPE_XML("4"),     //xml
        TYPE_PARQUET("5"),  //parquet
        TYPE_AVRO("6");   //avro
        private String value;
        FileFormat(String value){this.value=value;}
        public String toString() {
            return String.valueOf(this.value);
        }
    }

}
