package com.robin.basis.service.biz.impl;

import cn.hutool.core.util.StrUtil;
import com.robin.basis.dto.biz.MerchantMemberDTO;
import com.robin.basis.mapper.biz.MerchantMemberMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.service.biz.ICustomerService;
import com.robin.basis.service.biz.IMerchantMemberService;
import com.robin.basis.utils.IdCardNoIdentifier;
import com.robin.basis.utils.SecurityUtils;
import com.robin.biz.model.Customer;
import com.robin.biz.model.MerchantMember;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class MerchantMemberServiceImpl extends AbstractMybatisService<MerchantMemberMapper, MerchantMember,Long> implements IMerchantMemberService {
    @Resource
    private ICustomerService customerService;
    private static final DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(rollbackFor = RuntimeException.class)
    public boolean saveMember(MerchantMemberDTO dto){
        Long custId=null;
        if(ObjectUtils.isEmpty(dto.getCustId())){
            if(customerService.lambdaQuery().eq(Customer::getPhone,dto.getPhone()).eq(AbstractMybatisModel::getStatus, Const.VALID).exists()){
                throw new ServiceException("手机号码已存在");
            }else{
                Customer customer=new Customer();
                if(StrUtil.isNotBlank(dto.getCreditNo())){
                    Map<String,Object> map= IdCardNoIdentifier.identifyByCode(dto.getCreditNo());
                    if((Boolean) map.get("success")){
                        customer.setCreditNo(dto.getCreditNo());
                        customer.setBrithDay(LocalDate.parse(map.get("birthDay").toString()));
                        customer.setGender(map.get("gender").toString());
                        customer.setDistrictId(map.get("districtId").toString());
                    }
                    customer.setName(dto.getName());
                    customer.setPhone(dto.getPhone());
                    customer.setRegTime(LocalDateTime.now());
                    customer.setRegOrgId(SecurityUtils.getLoginUser().getOrgId());
                    customerService.save(customer);
                    custId=customer.getId();
                }
            }
        }else{
            custId=dto.getCustId();
        }
        MerchantMember member=new MerchantMember();
        BeanUtils.copyProperties(dto,member);
        member.setCustId(custId);
        return this.save(member);
    }
}
