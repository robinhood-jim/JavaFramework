package com.robin.basis.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.basis.dto.SysCodeSetDTO;
import com.robin.basis.mapper.SysCodeSetMapper;
import com.robin.basis.model.system.SysCodeSet;
import com.robin.basis.service.system.ISysCodeSetService;
import com.robin.basis.utils.WebUtils;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysCodeSetServiceImpl extends AbstractMybatisService<SysCodeSetMapper, SysCodeSet,Long> implements ISysCodeSetService {
    public Map<String,Object> list(SysCodeSetDTO dto){
        IPage<SysCodeSet> page=this.lambdaQuery().like(StrUtil.isNotBlank(dto.getNameOrDesc()),SysCodeSet::getEnName,dto.getNameOrDesc()).or()
                .like(StrUtil.isNotBlank(dto.getNameOrDesc()),SysCodeSet::getCnName,dto.getNameOrDesc())
                .page(getPage(dto));
        return WebUtils.toPageVO(page,null);
    }

}
