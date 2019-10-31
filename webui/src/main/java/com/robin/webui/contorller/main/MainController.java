package com.robin.webui.contorller.main;

import com.robin.core.base.util.Const;
import com.robin.core.web.util.CookieUtils;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;

/**
 * <p>Created at: 2019-09-05 15:47:31</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Controller
public class MainController {
    @Autowired
    private ResourceBundleMessageSource messageSource;
    @RequestMapping("/health")
    @ResponseBody
    String health(){
        return "OK";
    }

    @RequestMapping("/main")
    String mainpage(HttpServletRequest request,HttpServletResponse response){
        if(request.getSession().getAttribute(Const.SESSION)!=null){
            Session session=(Session) request.getSession().getAttribute(Const.SESSION);
            request.setAttribute("userName", session.getUserName());
            if (session.getOrgName() != null) {
                request.setAttribute("orgName", session.getOrgName());
            } else {
                request.setAttribute("orgName", messageSource.getMessage("title.defaultOrg", null, Locale.getDefault()));
            }
        }else{
            response.setStatus(401);
            return null;
        }
        return "main";
    }
    @RequestMapping("/index")
    String indexpage(){
        return "main";
    }
    @RequestMapping("/login")
    String login(){
        return "login";
    }

    @RequestMapping("/menu/list")
    @ResponseBody
    Map<String,Object> getMenu(HttpServletRequest request, HttpServletResponse response,@RequestParam String id){
        Session session=(Session) request.getSession().getAttribute(Const.SESSION);
        List<Map<String,Object>> privList=session.getPrivileges().get(id);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("id", id);
        List<Map<String,Object>> itemlist=new ArrayList<Map<String,Object>>();
        if(privList!=null){
            for (Map<String, Object> tmap:privList) {
                Map<String,Object> insertMap=new HashMap<String, Object>();
                insertMap.put("id", tmap.get("id"));
                insertMap.put("text", tmap.get("name"));
                Map<String,String> userMap=new HashMap<String, String>();
                userMap.put("name", "url");
                if(tmap.get("url")!=null) {
                    userMap.put("content", tmap.get("url").toString());
                } else{
                    userMap.put("content", "");
                }
                List<Map<String,String>> list1=new ArrayList<Map<String,String>>();
                list1.add(userMap);
                insertMap.put("userdata", list1);
                if(tmap.get("leafTag").toString().equals(Const.VALID)){
                    insertMap.put("open", "1");
                }else {
                    insertMap.put("child", "1");
                }
                itemlist.add(insertMap);
            }
            map.put("item", itemlist);
        }else{
            map.put("item", new ArrayList<String>());
        }
        return map;
    }

}
