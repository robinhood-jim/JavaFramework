package com.robin.basis.service.biz.impl;

import com.robin.basis.mapper.biz.CustomerMapper;
import com.robin.basis.service.biz.ICustomerService;
import com.robin.biz.model.Customer;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends AbstractMybatisService<CustomerMapper, Customer,Long> implements ICustomerService {

}
