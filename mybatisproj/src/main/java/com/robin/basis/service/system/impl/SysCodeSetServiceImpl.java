package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysCodeSetMapper;
import com.robin.basis.model.system.SysCodeSet;
import com.robin.basis.service.system.ISysCodeSetService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class SysCodeSetServiceImpl  extends AbstractMybatisService<SysCodeSetMapper, SysCodeSet,Long> implements ISysCodeSetService {

}
