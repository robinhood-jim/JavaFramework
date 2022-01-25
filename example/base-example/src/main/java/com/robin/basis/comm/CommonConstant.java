package com.robin.basis.comm;


public class CommonConstant {
    public static enum USER_STATUS{
        WAITACTIVE("0"),
        ACIVATED("1"),
        FROZZEN("4");
        String value;
        USER_STATUS(String value){
            this.value=value;
        }
        public String getValue() {
            return value;
        }
        public Integer getStatus(){
            return Integer.valueOf(value);
        }
    }
    public static enum ORG_STATUS{
        WAITACTIVE("0"),
        ACTIVATED("1"),
        FROZEN("4");
        String value;
        ORG_STATUS(String value){
            this.value=value;
        }
        public String getValue() {
            return value;
        }
        public Integer getStatus(){
            return Integer.valueOf(value);
        }
    }
    public static enum CUST_STATUS{
        WAITACTIVE("0"),
        ACTIVATED("1"),
        FROZEN("4");
        String value;
        CUST_STATUS(String value){
            this.value=value;
        }
        public String getValue() {
            return value;
        }
        public Integer getStatus(){
            return Integer.valueOf(value);
        }
    }
    public static enum TENANT_STATUS{
        WAITACTIVE("0"),
        ACTIVATED("1"),
        FROZEN("4");
        String value;
        TENANT_STATUS(String value){
            this.value=value;
        }
        public String getValue() {
            return value;
        }
        public Integer getStatus(){
            return Integer.valueOf(value);
        }
    }
}
