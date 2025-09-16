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
package com.robin.meta.contorller;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.meta.service.resource.GlobalResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/schema")
public class SchemaController {
    @Resource
    private GlobalResourceService globalResourceService;
    @GetMapping("/resource")
    @ResponseBody
    public Map<String,Object> getResourceSchema(@RequestParam(required = true) Long sourceId,@RequestParam String sourceParam){
        DataCollectionMeta collectionMeta=null;
        if(sourceId!=0L) {
            collectionMeta=globalResourceService.getResourceMetaDef(sourceId + "," + sourceParam);
        }else{
            DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
            builder.fsType(Const.FILESYSTEM.LOCAL.getValue())
                    .resPath(sourceParam);
            collectionMeta=builder.build();
        }
        //Schema schema=globalResourceService.getDataSourceSchema(collectionMeta,sourceId,sourceParam);
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("schema",globalResourceService.getDataSourceSchemaDesc(collectionMeta,sourceId,sourceParam,0));
        return retMap;
    }

}
