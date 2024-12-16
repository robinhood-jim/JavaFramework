package com.robin.core.web.util;


public class WebConstant {
    public static final Long DEFAULT_ORG=0L;
    public static final String SUCCESS="success";
    public static final String MESSAGE="message";
    public static final String DATA="data";
    public enum SYS_RESPONSIBLITIY{
        SYS_RESP(1L),
        ORG_RESP(2L),
        FREE_RESP(3L);
        private Long value;
        SYS_RESPONSIBLITIY(Long value){
            this.value=value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
        public Long getValue(){
            return value;
        }
    }
    public enum ACCOUNT_TYPE{
        SYSUSER(1L),  //System User,can operator system menu
        ORGUSER(2L),  // Org User,can access Org right menu and menu Assign by system user
        FREEUSER(3L); // User not include in Any Org
        private Long value;
        ACCOUNT_TYPE(Long value){
            this.value=value;
        }
        @Override
        public String toString(){
            return value.toString();
        }
        public Long getValue(){
            return value;
        }
    }
    public static final String TOKEN="tokenMap";
    public static final String LOCALE_KEY="application.locale";

}
