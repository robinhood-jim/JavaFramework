package com.robin.core.test.service;


import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.test.model.SysUserMybatis;
import org.springframework.stereotype.Service;

@Service
public class SysUserMybatisService extends BaseAnnotationJdbcService<SysUserMybatis,Long> {
}
