package com.robin.basis.service.biz.impl;

import com.robin.basis.mapper.biz.MerchantMapper;
import com.robin.basis.service.biz.IMerchantService;
import com.robin.biz.model.Merchant;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends AbstractMybatisService<MerchantMapper, Merchant,Long> implements IMerchantService {


}
