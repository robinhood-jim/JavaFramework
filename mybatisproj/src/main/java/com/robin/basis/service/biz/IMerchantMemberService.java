package com.robin.basis.service.biz;

import com.robin.basis.dto.biz.MerchantMemberDTO;
import com.robin.biz.model.MerchantMember;
import com.robin.core.base.service.IMybatisBaseService;

public interface IMerchantMemberService extends IMybatisBaseService<MerchantMember,Long> {
    boolean saveMember(MerchantMemberDTO dto);
}
