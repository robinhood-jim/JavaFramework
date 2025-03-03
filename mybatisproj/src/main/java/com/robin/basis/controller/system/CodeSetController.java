package com.robin.basis.controller.system;

import com.robin.core.web.codeset.Code;

import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.service.CodeSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/system/codeset")
@RestController
public class CodeSetController extends AbstractController {
    @Autowired
    private CodeSetService codeSetService;


    @GetMapping("/data/{dictCode}")

    public Map<String,Object> getDict(@PathVariable String dictCode){
        List<Code> codeMap=codeSetService.getCodeSet(dictCode);
        return wrapObject(codeMap);
    }


}
