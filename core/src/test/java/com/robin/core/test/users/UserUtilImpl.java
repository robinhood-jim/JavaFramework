package com.robin.core.test.users;

import com.robin.core.base.model.LoginUser;
import com.robin.core.base.util.IUserUtils;
import org.springframework.stereotype.Service;

@Service
public class UserUtilImpl implements IUserUtils {

    @Override
    public Long getLoginUserId() {
        return 1L;
    }

    @Override
    public LoginUser getLoginUser() {
        return new LoginUser();
    }
}
