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
    public static final String FLOWPARAM="flowParam";
    public static final String CYCLEPARAM="cycleType";
    public static final List<String> WORKNODECMDPARAM= Arrays.asList(new String[]{"command","operation","jobId","taskId","deplayTs"});
    public static final List<String> MASTERNODECMDPARAM= Arrays.asList(new String[]{"command","operation","jobId","taskId","deplayTs","status"});


}
