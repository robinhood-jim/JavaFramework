package com.robin.basis.service.biz.impl;

import com.robin.basis.mapper.biz.MemberConsumptionMapper;
import com.robin.basis.service.biz.IMemberConsumptionService;
import com.robin.biz.model.MemberConsumption;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class MemberConsumptionServiceImpl  extends AbstractMybatisService<MemberConsumptionMapper, MemberConsumption,Long> implements IMemberConsumptionService {
}
