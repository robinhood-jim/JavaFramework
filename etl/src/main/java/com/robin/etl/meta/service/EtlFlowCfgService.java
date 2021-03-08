package com.robin.etl.meta.service;

import com.google.common.collect.Maps;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.etl.meta.model.EtlFlowCfg;
import com.robin.etl.meta.model.EtlFlowParam;
import com.robin.etl.meta.model.EtlStepCfg;
import com.robin.etl.meta.model.EtlStepCondition;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EtlFlowCfgService extends BaseAnnotationJdbcService<EtlFlowCfg,Long> {
    @Resource
    private EtlFlowParamService etlFlowParamService;
    @Resource
    private EtlStepConditionService etlStepConditionService;
    @Resource
    private EtlStepCfgService etlStepCfgService;

    public Map<String,Object> getCfgConfig(Long flowId){
        Map<String,Object> retMap=new HashMap<>();
        try {
            List<EtlFlowParam> flowParamList = etlFlowParamService.queryByField("flowId", BaseObject.OPER_EQ, flowId);
            List<EtlStepCondition> conditions = etlStepConditionService.queryByField("flowId", BaseObject.OPER_EQ, flowId);
            Map<String, EtlStepCondition> parentMap = CollectionMapConvert.convertListToMap(conditions, "id");
            Map<String,List<EtlStepCondition>> childrelationMap=new HashMap<>();
            Map<String,String> cfgParam=Maps.newHashMap();
            flowParamList.forEach(f->{
                cfgParam.put(f.getParamName(),f.getParamValue());
            });
            retMap.put("flowParam",cfgParam);

            conditions.forEach(f->{
                if(null==f.getParentStepId()){
                    if(childrelationMap.containsKey("NULL")){
                        childrelationMap.get("NULL").add(f);
                    }else{
                        List<EtlStepCondition> list=new ArrayList<>();
                        list.add(f);
                        childrelationMap.put("NULL",list);
                    }
                }else{
                    if(childrelationMap.containsKey(f.getParentStepId().toString())){
                        childrelationMap.get(f.getParentStepId()).add(f);
                    }else{
                        List<EtlStepCondition> list=new ArrayList<>();
                        list.add(f);
                        childrelationMap.put(f.getParentStepId().toString(),list);
                    }
                }
            });
            //adjust rootNode
            EtlStepCondition rootNode = parentMap.get("NULL");
            if(null!=rootNode){
                retMap.put("rootNode",rootNode);
            }


        }catch (Exception ex){

        }
        return null;
    }
}
