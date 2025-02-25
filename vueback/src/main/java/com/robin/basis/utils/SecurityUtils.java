package com.robin.basis.utils;

import com.robin.basis.sercurity.SysLoginUser;
import com.robin.core.base.exception.ServiceException;
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
}
