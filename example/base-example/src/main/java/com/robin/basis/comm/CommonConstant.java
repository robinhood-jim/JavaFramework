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
    public enum MenuType {
        /**
         * 目录
         */
        CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    public static final int UNAUTHRIZED=401;
    public static final String LOGIN_URL="/login/user";
    public static final String OAUTH_LOGIN_URL="/oauth/token";
    public static final int SUPER_ADMIN = 1;
}
