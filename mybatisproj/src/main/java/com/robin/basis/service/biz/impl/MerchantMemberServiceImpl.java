package com.robin.basis.service.biz.impl;

import com.robin.basis.mapper.biz.MerchantMemberMapper;
import com.robin.basis.service.biz.IMerchantMemberService;
import com.robin.biz.model.MerchantMember;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class MerchantMemberServiceImpl extends AbstractMybatisService<MerchantMemberMapper, MerchantMember,Long> implements IMerchantMemberService {
}
