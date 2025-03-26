package com.robin.basis.service.biz.impl;

import com.robin.basis.mapper.biz.BrandMapper;
import com.robin.basis.service.biz.IBrandService;
import com.robin.biz.model.Brand;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class BrandServiceImpl extends AbstractMybatisService<BrandMapper, Brand,Long> implements IBrandService {
}
