package com.robin.etl.meta.service;

import com.google.common.collect.Maps;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.etl.common.EtlConstant;
import com.robin.etl.meta.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class EtlFlowCfgService extends BaseAnnotationJdbcService<EtlFlowCfg,Long> {
    @Resource
    private EtlFlowParamService etlFlowParamService;
    @Resource
    private EtlStepConditionService etlStepConditionService;
    @Resource
    private EtlStepCfgService etlStepCfgService;
    @Resource
    private EtlStepParamService etlStepParamService;

    public Map<String,Object> getCfgConfig(Long flowId){
        Map<String,Object> retMap=new HashMap<>();
        try {
            EtlFlowCfg cfg=getEntity(flowId);
            retMap.put(EtlConstant.CYCLEPARAM,cfg.getCycleType());
            List<EtlFlowParam> flowParamList = etlFlowParamService.queryByField("flowId", BaseObject.OPER_EQ, flowId);
            List<EtlStepCfg> stepCfgs=etlStepCfgService.queryByField("flowId",BaseObject.OPER_EQ,flowId);
            List<EtlStepParam> stepParams=etlStepParamService.queryByField("flowId",BaseObject.OPER_EQ,flowId);
            Map<Long,List<EtlStepCfg>> stepCfgMap=stepCfgs.stream().collect(Collectors.groupingBy(EtlStepCfg::getId));
            Map<Long,List<EtlStepParam>> stepParamsMap=stepParams.stream().collect(Collectors.groupingBy(EtlStepParam::getStepId));

            List<EtlStepCondition> conditions = etlStepConditionService.queryByField("flowId", BaseObject.OPER_EQ, flowId);
            Map<Long, List<EtlStepCondition>> parentMap = conditions.stream().collect(Collectors.groupingBy(EtlStepCondition::getId));
            Map<String,List<EtlStepCondition>> childrelationMap=new HashMap<>();
            Map<String,String> cfgParam=Maps.newHashMap();
            flowParamList.forEach(f->{
                cfgParam.put(f.getParamName(),f.getParamValue());
            });
            retMap.put("flowParam",cfgParam);
            retMap.put("stepCfgs",stepCfgMap);
            retMap.put("stepParamsMap",stepParamsMap);

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
            retMap.put("stepConditions",parentMap);
            retMap.put("relationMap",childrelationMap);
            //adjust rootNode
            EtlStepCondition rootNode = parentMap.get("NULL").get(0);
            if(null!=rootNode){
                retMap.put("rootNode",rootNode);
            }


        }catch (Exception ex){

        }
        return null;
    }
}
