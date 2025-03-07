package com.robin.basis.sercurity;

import com.robin.basis.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("checker")
public class PermissionCheckService {
    public boolean isSuperAdmin(){
        return SecurityUtils.isLoginUserSystemAdmin();
    }
    public boolean isAdmin(){
        return SecurityUtils.isLoginUserSystemAdmin() || SecurityUtils.isLoginUserOrgAdmin();
    }
    public boolean hasRoles(String... roles){
        List<String> testRoles= Arrays.stream(roles).map(f-> {
            if ("ROLE_".startsWith(f)) {
                return f;
            }else{
                return "ROLE_"+f.toUpperCase();
            }
        }).collect(Collectors.toList());
        return testRoles.stream().anyMatch(SecurityUtils.getLoginUser().getPermissions()::contains);
    }

    public boolean hasPermission(String... permissions){
        if(SecurityUtils.isLoginUserSystemAdmin()){
            return true;
        }
        return Arrays.stream(permissions).allMatch(SecurityUtils.getLoginUser().getPermissions()::contains);
    }


}
