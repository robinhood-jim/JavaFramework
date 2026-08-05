package com.robin.etl.common;


import java.util.Arrays;
import java.util.List;

public class EtlConstant {
    public enum CYCLE_TYPE{
        YEAR("8"),    //年
        QUARTER("7"), //季度
        XUN("5"),  //旬
        MONTH("6"),
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
    public enum STEP_TYPE{
        INBOUND("1"),
        TRANSFORM("2"),
        FILTER("3"),
        OUTBOUND("4");

        private String value;
        STEP_TYPE(String value){
            this.value=value;
        }
        public Integer getInt(){
            return Integer.valueOf(value);
        }
    }
    public enum STATUS{
        PEDDING("1"),
        INIT("2"),
        SCHEDULE("3"),
        RUNNING("4"),
        FAIELD("5"),
        FINISH("6");
        private String value;
        STATUS(String value){
            this.value=value;
        }
        public String getValue(){
            return this.value;
        }
        public Integer getInt(){
            return Integer.valueOf(this.value);
        }
    }
    public enum AGGERATION{
        SUM("SUM"),
        AVG("AVG"),
        MAX("MAX"),
        MIN("MIN"),
        COUNT("COUNT");
        private String value;
        AGGERATION(String value){this.value=value;}

        public String getValue() {
            return value;
        }
        public static AGGERATION forName(String name){
            for(AGGERATION s: AGGERATION.values()){
                if(s.getValue().equalsIgnoreCase(name)){
                    return s;
                }
            }
            return null;
        }
    }

    public static final String FILESTORAGETYPE="cloud.storageType";
    public static final String FLOWPARAM="flowParam";
    public static final String CYCLEPARAM="cycleType";
    public static final String PARAM_EXPRESSIONS="expressions";
    public static final String PARAM_REVERSEPOLAND="reversePoland";
    public static final String PARAM_SCRIPTENGINE="scriptEngine";
    public static final String PARAM_COALEASE_NAME="coalease.name";
    public static final String PARAM_COALEASE_TYPE="coalease.type";
    public static final String PARAM_COALEASE_REMOVEFIELDS="coalease.removeFields";
    public static final String PARAM_COALEASE_INPUTFIEDLS="coalease.inputFields";
    public static final String PARAM_SQL_SELECT="db.selectSql";
    public static final String PARAM_SQL_PARAMETER="db.sqlParameters";
    public static final String PARAM_DB_HOST="db.hostName";
    public static final String PARAM_DB_PORT="db.port";
    public static final String PARAM_DB_DATABASENAME="db.databaseName";
    public static final String PARAM_DB_JDBCURL="db.jdbcUrl";
    public static final String PARAM_DB_USER="db.username";
    public static final String PARAM_DB_PASSWORD="db.password";
    public static final String PARAM_DB_TYPE="db.type";
    public static final String PARAM_DATASOURCE_ID="dataSourceId";
    public static final String PARAM_STORAGE_TYPE="storageType";
    public static final String PARAM_STORAGE_PARAMETERS="storageParameters";
    public static final String PARAM_FLUSHOUT_PATH="flushOutPath";
    public static final String PARAM_FILTER_SQL="filterSql";
    public static final String PARAM_INPUT_PATH="inputPath";
    public static final String PARAM_DATAMETA_DEFINE="dataMetaDefine";
    public static final String PARAM_FLUSHOUT_COLUMNS="flushOutColumns";
    public static final String PARAM_DATASET_ID="dataSetId";
    public static final String PARAM_MAPPING_FIELDS="mappingFields";
    public static final String PARAM_GROUPBY_FIELDS="groupByFields";
    public static final String PARAM_AGGREGATES="aggregates";
    public static final String AGG_COUNT_FIELD="$AGGCOUNTFIELD";
    public static final String PARAM_INPUTSOURCEMETA = "$inputMeta";


    public static final List<String> WORKNODECMDPARAM= Arrays.asList(new String[]{"command","operation","jobId","taskId","deplayTs"});
    public static final List<String> MASTERNODECMDPARAM= Arrays.asList(new String[]{"command","operation","jobId","taskId","deplayTs","status"});


}
