package com.robin.etl.meta.service;

import com.google.common.collect.Maps;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import com.robin.etl.common.EtlConstant;
import com.robin.etl.meta.dto.JobConfiguration;
import com.robin.etl.meta.model.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EtlFlowCfgService extends BaseAnnotationJdbcService<EtlFlowCfg, Long> {
    @Resource
    private EtlFlowParamService etlFlowParamService;
    @Resource
    private EtlStepConditionService etlStepConditionService;
    @Resource
    private EtlStepCfgService etlStepCfgService;
    @Resource
    private EtlStepParamService etlStepParamService;

    public JobConfiguration getCfgConfig(Long flowId) {
        Map<String, Object> retMap = new HashMap<>();
        JobConfiguration configuration=new JobConfiguration();
        try {
            EtlFlowCfg cfg = getEntity(flowId);
            Assert.notNull(cfg,"flow "+flowId+" does not exists!");
            configuration.setFlowCfg(cfg);
            List<EtlFlowParam> flowParamList = etlFlowParamService.queryByField("flowId", Const.OPERATOR.EQ, flowId);
            List<EtlStepCfg> stepCfgs = etlStepCfgService.queryByField("flowId", Const.OPERATOR.EQ, flowId);
            List<EtlStepParam> stepParams = etlStepParamService.queryByField("flowId", Const.OPERATOR.EQ, flowId);
            Map<Long, List<EtlStepCfg>> stepCfgMap = stepCfgs.stream().collect(Collectors.groupingBy(EtlStepCfg::getParentStepId));
            Map<Long, List<EtlStepCfg>> relayStepMap = stepCfgs.stream().collect(Collectors.groupingBy(EtlStepCfg::getId));

            Map<Long, List<EtlStepParam>> stepParamsMap = stepParams.stream().collect(Collectors.groupingBy(EtlStepParam::getStepId));

            List<EtlStepCondition> conditions = etlStepConditionService.queryByField("flowId", Const.OPERATOR.EQ, flowId);
            Map<Long, List<EtlStepCondition>> parentMap = conditions.stream().collect(Collectors.groupingBy(EtlStepCondition::getParentStepId));
            Map<Long, List<EtlStepCondition>> currerntMap = conditions.stream().collect(Collectors.groupingBy(EtlStepCondition::getStepId));
            Map<String, String> cfgParam = Maps.newHashMap();
            flowParamList.forEach(f -> {
                cfgParam.put(f.getParamName(), f.getParamValue());
            });
            configuration.setPublicParameters(cfgParam);
            configuration.setNextSteps(stepCfgMap);
            configuration.setRelaySteps(relayStepMap);
            configuration.setNextStepConditions(parentMap);
            configuration.setRelayStepConditions(currerntMap);
            configuration.setStepParameters(stepParamsMap);
            //adjust rootNode
            EtlStepCfg rootNode = stepCfgMap.get(0L).get(0);
            if (null != rootNode) {
                retMap.put("rootNode", rootNode);
            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return configuration;
    }

}
