package com.robin.example.service.user;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.example.model.user.SysCredential;
import org.springframework.stereotype.Component;

/**
 * System credential
 */
@Component
public class SysCredentialService extends BaseAnnotationJdbcService<SysCredential,Long> {
}
