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
        SYSADMIN(1L),  //System User,can operator system menu
        ORGADMIN(2L),  // Org User,can access Org right menu and menu Assign by system user
        ORDINARY(3L); // User not include in Any Org
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
    public enum TENANT_TYPE{
        ORGADMIN(Short.valueOf("1")),  //System User,can operator system menu
        EMPLOYEE(Short.valueOf("2")),  // Org User,can access Org right menu and menu Assign by system user
        TEMPORARY(Short.valueOf("3")),
        NORIGHT(Short.valueOf("4"));

        private Short value;
        TENANT_TYPE(Short value){
            this.value=value;
        }
        @Override
        public String toString(){
            return value.toString();
        }
        public Short getValue(){
            return value;
        }
    }
    public enum TENANT_KEYS{
        PERMISSIONS_DEFAULT("PERMISSIONS_DEFAULT"),
        ROLES_DEFAULT("ROLES_DEFAULT"),
        ALL_PERMISSIONS("allPermissions"),
        MANAGER_PERMISSION("managerPermissions"),
        ORDINARY_PERMISSIONS("ordinaryPermissions"),
        USER_CAPACITY("userCapacity"),
        USE_DIVIDED_STORAGE("useDividedStorage"),
        DIVIDE_STORAGE_TYPE("dividedStorageType"),
        USE_CLOUD_STORAGE("useCloudStorage"),
        CLOUD_TYPE("cloudType"),
        CLOUD_ACCESSKEY("cloudAccessKey"),
        CLOUD_SECRETKEY("cloudSecretKey");
        private String key;
        TENANT_KEYS(String key){
            this.key=key;
        }

        public String getKey() {
            return key;
        }
    }
    public static final String SYSPARAMS_PERMISSIONS_PERFIX="PERMISSIONS_";
    public static final String SYSPARAMS_PERMISSIONS_ORG_PERFIX="PERMISSIONS_ORG";
    public static final String SYSPARAMS_PERMISSIONS_ORDINARY_PERFIX="PERMISSIONS_ORDINARY";
    public static final String SYSPARAMS_ROLES_PERFIX="ROLES_";
    public static final String SYSPARAMS_DEFAULT_PERFIX="DEFAULT";
    public static final String TOKEN="tokenMap";
    public static final String LOCALE_KEY="application.locale";

}
