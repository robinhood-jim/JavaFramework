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
package com.robin.webui.contorller.system;


import com.robin.webui.contorller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/system")
public class SysCodeContorller extends BaseController {
    @GetMapping("/codecombo")
    @ResponseBody
    public Map<String,Object> getCodeSetCombo(HttpServletRequest request, @RequestParam String codeSetNo){
        return getResultFromRest(request,"system/codecombo?codeSetNo={1}",codeSetNo);
    }
    @GetMapping("/codeset/select")
    @ResponseBody
    public Map<String,Object> showCodeSetSelection(HttpServletRequest request, HttpServletResponse response, @RequestParam String codeSetNo){
        return getResultFromRest(request,"system/codeset/select?codeSetNo={1}",codeSetNo);
    }
}
