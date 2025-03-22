package com.robin.basis.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.robin.basis.dto.SysCodeDTO;
import com.robin.basis.dto.SysCodeSetDTO;
import com.robin.basis.mapper.SysCodeSetMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysCode;
import com.robin.basis.model.system.SysCodeSet;
import com.robin.basis.service.system.ISysCodeService;
import com.robin.basis.service.system.ISysCodeSetService;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.util.Const;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/dict")
public class SysCodeSetController extends AbstractMyBatisController<ISysCodeSetService, SysCodeSetMapper,SysCodeSet,Long> {
    @Resource
    private ISysCodeService sysCodeService;

    @GetMapping
    public Map<String,Object> list(SysCodeSetDTO dto){
        return wrapObject(service.list(dto));
    }

    @GetMapping("/data/{id}")
    public Map<String,Object> getById(@PathVariable Long id){
        SysCode code=sysCodeService.getById(id);
        if(!ObjectUtils.isEmpty(code)){
            return wrapObject(code);
        }else{
            return wrapFailedMsg("no id found");
        }
    }

    @GetMapping("/code/{dictCode}")
    public Map<String,Object> getByCode(@PathVariable String dictCode){
        LambdaQueryWrapper<SysCodeSet> codeSetWrapper=new QueryWrapper<SysCodeSet>().lambda();
        codeSetWrapper.eq(SysCodeSet::getEnName,dictCode);
        codeSetWrapper.eq(AbstractMybatisModel::getStatus,Const.VALID);
        List<SysCodeSet> codeSets=service.list(codeSetWrapper);
        if(CollectionUtils.isEmpty(codeSets) ||  codeSets.size()>1){
            throw new WebException("codeSet not exist or not unique");
        }
        SysCodeSet set=codeSets.get(0);
        LambdaQueryWrapper<SysCode> queryWrapper=new QueryWrapper<SysCode>().lambda();
        queryWrapper.eq(SysCode::getCsId,set.getId());
        queryWrapper.eq(AbstractMybatisModel::getStatus,Const.VALID);
        queryWrapper.orderByAsc(SysCode::getOrderNo);
        List<SysCode> list=sysCodeService.list(queryWrapper);
        return wrapObject(list.stream().map(SysCodeDTO::fromVO).collect(Collectors.toList()));
    }

}
