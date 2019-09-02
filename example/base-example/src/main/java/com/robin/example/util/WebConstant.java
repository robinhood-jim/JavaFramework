package com.robin.example.util;

/**
 * <p>Created at: 2019-09-02 10:53:24</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class WebConstant {
    public static final Long DEFAULT_ORG=0L;
    public static enum SYS_RESPONSIBLITIY{
        SYS_RESP(1L),
        ORG_RESP(2L),
        FREE_RESP(3L);
        private Long value;
        private SYS_RESPONSIBLITIY(Long value){
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
        private ACCOUNT_TYPE(Long value){
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
}
