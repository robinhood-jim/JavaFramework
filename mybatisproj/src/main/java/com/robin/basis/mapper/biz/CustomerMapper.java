package com.robin.basis.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.biz.model.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper  extends BaseMapper<Customer> {
}
