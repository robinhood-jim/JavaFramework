package com.robin.core.base.util;


public class ResourceConst {

    public enum InputSourceType{
        TYPE_HDFS(1L,"HDFS"),
        TYPE_LOCAL(2L,"LOCAL"),
        TYPE_FTP(3L,"FTP"),
        TYPE_SFTP(4L,"SFTP"),
        TYPE_AWS(5L,"AWS");
        private Long key;
        private String value;
        InputSourceType(Long key,String value){
            this.key=key;
            this.value=value;
        }
        public Long getValue(){
            return this.key;
        }
        @Override
        public String toString(){
            return this.value;
        }
    }
    public  enum ResourceType{
        TYPE_LOCALFILE(0L,"LOCAL"),  //local
        TYPE_HDFSFILE(1L,"HDFS"),   //hdfs
        TYPE_FTPFILE(2L,"FTP"),     //ftp
        TYPE_SFTPFILE(3L,"SFTP"), //sftp
        TYPE_DB(4L,"DB"),  //db
        TYPE_REDIS(5L,"REDIS"), //redis
        TYPE_MONGODB(6L,"MONOGODB"), //mongodb
        TYPE_KAFKA(7L,"KAFKA"),
        TYPE_CASSANDRA(8L,"CASSANDRA"),
        TYPE_HBASE(9L,"HBASE"),
        TYPE_ROCKETDB(10L,"ROCKETDB"),
        TYPE_ES(11L,"ES"),
        TYPE_RABBIT(12L,"RABBITMQ");
        private Long key;
        private String value;

        ResourceType(Long key,String value) {
            this.key = key;
            this.value=value;
        }

        @Override
        public String toString() {
            return value;
        }

        public Long getValue() {
            return key;
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
    public enum VALUE_TYPE{
        AVRO("avro"),
        JSON("json"),
        XML("xml"),
        PROTOBUF("proto"),
        ORC("orc"),
        CSV("csv");
        private String value;
        VALUE_TYPE(String value){
            this.value=value;
        }

        public String getValue() {
            return value;
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
