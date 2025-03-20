package com.robin.basis.utils;

import com.robin.basis.sercurity.SysLoginUser;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.web.util.WebConstant;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    private SecurityUtils(){

    }
    public static Long getUserId(){
        try {
            return getLoginUser().getId();
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    public static String getUserName(){
        try{
            return getLoginUser().getUsername();
        }catch (Exception ex){
            throw new ServiceException(ex.getMessage());
        }
    }
    public static SysLoginUser getLoginUser(){
        try{
            return (SysLoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }catch (Exception ex){
            throw new ServiceException(ex.getMessage());
        }
    }
    public static boolean isLoginUserSystemAdmin(){
        return WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(getLoginUser().getAccountType());
    }
    public static boolean isAdmin(){
        return isLoginUserSystemAdmin() ||isLoginUserOrgAdmin();
    }
    public static boolean isLoginUserOrgAdmin(){
        return WebConstant.ACCOUNT_TYPE.ORGADMIN.toString().equals(getLoginUser().getAccountType());
    }
}
