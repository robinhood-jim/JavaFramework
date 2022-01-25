package com.robin.basis.model;

import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

import java.time.LocalDateTime;


public abstract class AbstractModel extends BaseObject {
    @MappingField
    private LocalDateTime createTm;
    @MappingField
    private LocalDateTime modifyTm;
    @MappingField
    private Long createUser;
    @MappingField
    private Long modifyUser;

    public LocalDateTime getCreateTm() {
        return createTm;
    }

    public void setCreateTm(LocalDateTime createTm) {
        this.createTm = createTm;
    }

    public LocalDateTime getModifyTm() {
        return modifyTm;
    }

    public void setModifyTm(LocalDateTime modifyTm) {
        this.modifyTm = modifyTm;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(Long modifyUser) {
        this.modifyUser = modifyUser;
    }

}
