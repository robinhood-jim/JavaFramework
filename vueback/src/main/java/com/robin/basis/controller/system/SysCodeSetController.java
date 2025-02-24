package com.robin.basis.controller.system;

import com.robin.basis.dto.SysCodeDTO;
import com.robin.basis.model.system.SysCode;
import com.robin.basis.model.system.SysCodeSet;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.core.web.controller.AbstractController;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/codeSet/")
public class SysCodeSetController extends AbstractController {
    @Resource
    private JdbcDao jdbcDao;

    @GetMapping("/data/{id}")
    public Map<String,Object> getById(@PathVariable Long id){
        SysCode code=jdbcDao.getEntity(SysCode.class,id);
        if(!ObjectUtils.isEmpty(code)){
            return wrapObject(code);
        }else{
            return wrapFailedMsg("no id found");
        }
    }
    @GetMapping("/data/list")
    public Map<String,Object> queryCode(Map<String,Object> reqMap){
        FilterConditionBuilder conditionBuilder=new FilterConditionBuilder();
        if(!ObjectUtils.isEmpty(reqMap.get("dictType"))){
            SysCodeSet set=jdbcDao.getByField(SysCodeSet.class,SysCodeSet::getEnName, Const.OPERATOR.EQ,reqMap.get("dictType").toString());
            if(!ObjectUtils.isEmpty(set))
            conditionBuilder.addEq(SysCode::getCsId,set.getId());
        }else if(!ObjectUtils.isEmpty(reqMap.get("dictLabel"))){
            conditionBuilder.addFilter(SysCode::getItemName, Const.OPERATOR.LIKE,reqMap.get("dictLabel").toString());
        }else if(!ObjectUtils.isEmpty(reqMap.get("status"))){
            conditionBuilder.addEq(SysCode::getCodeStatus,Integer.parseInt(reqMap.get("status").toString()));
        }
        PageQuery<SysCode> pageQuery=new PageQuery<>();
        pageQuery.setPageSize(0);
        jdbcDao.queryByCondition(SysCode.class,conditionBuilder.build(),pageQuery);
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("code",200);
        retMap.put("rows",pageQuery.getRecordSet());
        retMap.put("total",pageQuery.getRecordSet().size());
        return reqMap;
    }
    @GetMapping("/data/type/{dictCode}")
    public Map<String,Object> getByCode(@PathVariable String dictCode){
        SysCode code=new SysCode();
        SysCodeSet set=jdbcDao.getByField(SysCodeSet.class,SysCodeSet::getEnName, Const.OPERATOR.EQ,dictCode);
        if(!ObjectUtils.isEmpty(set)) {
            code.setCsId(set.getId());
            code.setCodeStatus(Const.VALID);
        }
        List<SysCode> list=jdbcDao.queryByVO(SysCode.class,code,"order_no");
        return wrapObject(list.stream().map(SysCodeDTO::fromVO).collect(Collectors.toList()));
    }

}
