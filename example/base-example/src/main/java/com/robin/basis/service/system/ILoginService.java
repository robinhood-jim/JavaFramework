package com.robin.basis.service.system;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.web.util.Session;

public interface ILoginService {
    Session doLogin(String accountName, String password) throws ServiceException;
    void getRights(Session session);
    Session simpleLogin(String accountName, String password) throws ServiceException;
    Session ssoGetUserById(Long userId);
    Session ssoGetUser(String userName);
}
