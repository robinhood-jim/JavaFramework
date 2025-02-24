package com.robin.basis.utils;

import com.robin.core.base.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Md5PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        try{
            return StringUtils.getMd5Encry(charSequence.toString());
        }catch (Exception ex){

        }
        return null;
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        try {
            return StringUtils.getMd5Encry(charSequence.toString()).equalsIgnoreCase(s);
        }catch (Exception ex){

        }
        return false;
    }
}
