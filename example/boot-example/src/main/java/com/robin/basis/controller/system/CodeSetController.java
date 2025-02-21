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

@RequestMapping("/system")
@Controller
public class CodeSetController extends AbstractController {
    @Autowired
    private CodeSetService codeSetService;


    @GetMapping("/codeSet/data/{dictCode}")
    @ResponseBody
    public Map<String,Object> getDict(@PathVariable String dictCode){
        List<Code> codeMap=codeSetService.getCodeSet(dictCode);
        return wrapObject(codeMap);
    }


}
