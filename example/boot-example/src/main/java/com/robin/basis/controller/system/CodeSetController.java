package com.robin.basis.controller.system;

import com.robin.core.web.codeset.Code;
import com.robin.core.web.codeset.CodeSetService;
import com.robin.core.web.controller.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/system/codeset")
@Controller
public class CodeSetController extends AbstractController {
    @Autowired
    private CodeSetService codeSetService;


    @GetMapping("/data/{dictCode}")
    @ResponseBody
    public Map<String,Object> getDict(@PathVariable String dictCode){
        List<Code> codeMap=codeSetService.getCodeSet(dictCode);
        return wrapObject(codeMap);
    }
    @GetMapping("/select")
    @ResponseBody
    public Map<String,Object> showCodeSetSelection(HttpServletRequest request, HttpServletResponse response, @RequestParam String codeSetNo,@RequestParam String allowNull){
        Map<String,String> codeMap=codeSetService.getCacheCode(codeSetNo);
        Map<String,Object> retMap=new HashMap<>();
        boolean insertNullVal = true;
        if (!ObjectUtils.isEmpty(allowNull) && "false".equalsIgnoreCase(allowNull)) {
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
