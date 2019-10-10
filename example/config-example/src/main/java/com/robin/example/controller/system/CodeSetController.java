package com.robin.example.controller.system;

import com.robin.core.web.codeset.CodeSetService;
import com.robin.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/system/codeset")
@Controller
public class CodeSetController extends BaseController {
    @Autowired
    private CodeSetService codeSetService;

    @RequestMapping("/select")
    @ResponseBody
    public Map<String,Object> showCodeSetSelection(HttpServletRequest request, HttpServletResponse response, @RequestParam String codeSetNo){
        Map<String,String> codeMap=codeSetService.getCacheCode(codeSetNo);
        Map<String,Object> retMap=new HashMap<>();
        boolean insertNullVal = true;
        String allowNull = request.getParameter("allowNull");
        if (allowNull != null && !allowNull.isEmpty() && allowNull.equalsIgnoreCase("false")) {
            insertNullVal = false;
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        insertMapToSelect(list,codeMap);
        retMap.put("options", list);
        return retMap;
    }

}
