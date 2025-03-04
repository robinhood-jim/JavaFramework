package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysCodeMapper;
import com.robin.basis.model.system.SysCode;
import com.robin.basis.service.system.ISysCodeService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class SysCodeServiceImpl extends AbstractMybatisService<SysCodeMapper, SysCode,Long> implements ISysCodeService {

}
