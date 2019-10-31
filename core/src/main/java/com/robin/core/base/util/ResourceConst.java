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
        TYPE_LOCALFILE(0L),  //local
        TYPE_HDFSFILE(1L),   //hdfs
        TYPE_FTPFILE(2L),     //ftp
        TYPE_SFTPFILE(3L), //sftp
        TYPE_DB(4L),  //db
        TYPE_REDIS(5L), //redis
        TYPE_MONGODB(6L), //mongodb
        TYPE_KAFKA(7L),
        TYPE_CASSANDRA(8L),
        TYPE_HBASE(9L),
        TYPE_ROCKETDB(10L);
        private Long value;

        ResourceType(Long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(this.value);
        }

        public Long getValue() {
            return value;
        }
    }
    public enum FileFormat{
        TYPE_CSV("1"),       //csv
        TYPE_JSON("2"),   //json
        TYPE_XLSX("3"),     //xlsx
        TYPE_XML("4"),     //xml
        TYPE_PARQUET("5"),  //parquet
        TYPE_AVRO("6"),   //avro
        TYPE_PROTO("7");
        private String value;
        FileFormat(String value){this.value=value;}
        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
    }
    public enum RESTYPE{
        MENU("1"),
        BUTTON("2");

        private String value;

        RESTYPE(String value){
            this.value=value;
        }
        @Override
        public String toString() {
            return value;
        }
    }
}
