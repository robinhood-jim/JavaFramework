package com.robin.webui.contorller.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.WebConstant;
import com.robin.webui.contorller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class LoginController extends BaseController {
    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Environment environment;
    private Gson gson = new Gson();

    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> ssoLogin(HttpServletRequest request, HttpServletResponse response, @RequestParam String accountName, @RequestParam String password) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            Map<String,String> vMap=new HashMap<>();
            vMap.put("username",accountName);
            vMap.put("password",password);
            vMap.put("grant_type","password");
            vMap.put("client_id",environment.getProperty("login.clientId"));
            vMap.put("client_secret",environment.getProperty("login.clientSecret"));
            Map<String, Object> map = postFromSsoRest("oauth/token", vMap);
            int expireTs=environment.containsProperty("cookie.expireTs")?Integer.parseInt(environment.getProperty("cookie.expireTs")):30*60;
            if (map.containsKey("access_token")) {
                Cookie atoken=new Cookie("access_token",map.get("access_token").toString());
                atoken.setPath("/");
                atoken.setMaxAge(expireTs);
                response.addCookie(atoken);
                Cookie rtoken=new Cookie("refresh_token",map.get("access_token").toString());
                rtoken.setPath("/");
                rtoken.setMaxAge(expireTs+10*60);
                response.addCookie(rtoken);
                Map<String, Object> rightMap = getResultFromSsoRest(request,"sso/getuserright", map.get("access_token").toString());
                Session session = gson.fromJson(gson.toJson(rightMap.get("session")), new TypeToken<Session>() {}.getType());
                request.getSession().setAttribute(Const.SESSION, session);
                retMap.put("success", true);
                if (session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString()) && session.getOrgId() == null) {
                    //User has more than one Org,Select from page
                    retMap.put("selectOrg", true);
                    retMap.put("userId", session.getUserId());
                }
                response.addCookie(new Cookie("userName", URLEncoder.encode(session.getUserName(), "UTF-8")));
                response.addCookie(new Cookie("accountType", session.getAccountType()));
                response.addCookie(new Cookie("userId", String.valueOf(session.getUserId())));
                if (session.getOrgName() != null) {
                    response.addCookie(new Cookie("orgName", URLEncoder.encode(session.getOrgName(), "UTF-8")));
                } else {
                    response.addCookie(new Cookie("orgName", URLEncoder.encode(messageSource.getMessage("title.defaultOrg", null, Locale.getDefault()), "UTF-8")));
                }
            }
        } catch (Exception ex) {
            retMap = BaseController.wrapFailedMsg(ex);
        }
        return retMap;
    }

    @PostMapping
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            

            
            
            
            
            
            
            
            
            
            ("/loginold")
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, HttpServletResponse response, @RequestParam String accountName, @RequestParam String password) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            Map<String, Object> map = getResultFromRest(request,"login?accountName={1}&password={2}", new Object[]{accountName, password});
            //loginService.login(accountName, password);
            Session session = gson.fromJson(gson.toJson(map.get("session")), new TypeToken<Session>() {
            }.getType());
            request.getSession().setAttribute(Const.SESSION, session);
            retMap.put("success", true);
            if (session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString()) && session.getOrgId() == null) {
                //User has more than one Org,Select from page
                retMap.put("selectOrg", true);
                retMap.put("userId", session.getUserId());
            }
            response.addCookie(new Cookie("userName", URLEncoder.encode(session.getUserName(), "UTF-8")));
            response.addCookie(new Cookie("accountType", session.getAccountType()));
            response.addCookie(new Cookie("userId", String.valueOf(session.getUserId())));
            if (session.getOrgName() != null) {
                response.addCookie(new Cookie("orgName", URLEncoder.encode(session.getOrgName(), "UTF-8")));
            } else {
                response.addCookie(new Cookie("orgName", URLEncoder.encode(messageSource.getMessage("title.defaultOrg", null, Locale.getDefault()), "UTF-8")));
            }
        } catch (Exception ex) {
            retMap = wrapFailedMsg(ex);
        }
        return retMap;
    }
}
