package com.robin.basis.system.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

@Slf4j
public class MyBatisPlusIdHandler implements MetaObjectHandler {
    private static final String ID = "id";

    //创建时间
    private static final String CREATED_TS = "createTm";
    //修改时间
    private static final String UPDATED_TS = "modifyTm";
    private static final String DELETED = "ifValid";
    private static final String CREATOR = "creatorId";
    private static final String MODIFIER = "modifierId";
    private static final String TENANTID = "tenantId";


    /**
     * 获取Model的主键字段,必须以@TableId注解，优先取注解的value，支持spring Cache
     * add by luoming 2021-04-14
     *
     * @param clazzName 类名称
     * @return
     */


   /* public MyBatisPlusIdHandler(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }*/
    public MyBatisPlusIdHandler() {

    }

    /**
     * 判断id为空时自动填充IdGenerator生成器生成的id
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject == null) {
            return;
        }

        if (metaObject.hasSetter(CREATOR) && metaObject.hasSetter(MODIFIER)
                && (metaObject.getValue(CREATOR) == null || metaObject.getValue(MODIFIER) == null)) {
            try {
                /*if (SecurityUtils.getLoginUser() != null) {
                    Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
                    //Long tenantId =SecurityUtils.getLoginUser().getUser().getTenantId();//SecurityUtils.getLoginUser().getUser().getTenantId();
                    setFieldValByNameIfNull(CREATOR, userId, metaObject);
                    //setFieldValByNameIfNull(TENANTID, tenantId, metaObject);
                    setFieldValByNameIfNull(MODIFIER, userId, metaObject);
                }*/
            } catch (ServiceException ex) {
                log.error("{}", ex);
            }
        }


        setFieldValByNameIfNull(CREATED_TS, LocalDateTime.now(), metaObject);
        setFieldValByNameIfNull(UPDATED_TS, LocalDateTime.now(), metaObject);
        setFieldValByNameIfNull(DELETED, Const.COLUMN_VALID, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter(CREATOR) && metaObject.hasSetter(MODIFIER)
                && (metaObject.getValue(CREATOR) == null || metaObject.getValue(MODIFIER) == null)) {
           /* if (null != SecurityUtils.getLoginUser()) {
                Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
                setFieldValByNameUpdate(MODIFIER, userId, metaObject);
                setFieldValByNameUpdate("et." + MODIFIER, userId, metaObject);
                setFieldValByNameUpdate(UPDATED_TS, LocalDateTime.now(), metaObject);
                setFieldValByNameUpdate("et." + UPDATED_TS, LocalDateTime.now(), metaObject);
            }*/
        }
    }

    private void setFieldValByNameIfNull(String fieldname, Object value, MetaObject metaObject) {
        if (metaObject.hasGetter(fieldname) && metaObject.hasSetter(fieldname)) {
            Object existValue = metaObject.getValue(fieldname);
            if (existValue == null) {
                setFieldValByName(fieldname, value, metaObject);
            }
        }
    }

    private void setFieldValByNameUpdate(String fieldname, Object value, MetaObject metaObject) {
        if (metaObject.hasGetter(fieldname) && metaObject.hasSetter(fieldname)) {
            setFieldValByName(fieldname, value, metaObject);
        }
    }

}
