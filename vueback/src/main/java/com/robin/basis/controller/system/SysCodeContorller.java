/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.basis.controller.system;

import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.web.codeset.Code;

import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.service.CodeSetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system")
public class SysCodeContorller extends AbstractController {
    @GetMapping("/code/{codeSetNo}")
    @ResponseBody
    public Map<String,Object> getCodeSetCombo(@PathVariable String codeSetNo){
        Map<String,Object> map=new HashMap<>();
        List<Map<String,Object>> list=new ArrayList<>();
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        List<Code> codeList=getCodeList(util.getCacheCode(codeSetNo));
        for(Code code:codeList){
            Map<String, Object> tmap = new HashMap<>();
            tmap.put("value", code.getValue());
            tmap.put("label", code.getCodeName());
            list.add(tmap);
        }
        wrapSuccessMap(map,"");
        map.put("data", list);
        return map;
    }
}
