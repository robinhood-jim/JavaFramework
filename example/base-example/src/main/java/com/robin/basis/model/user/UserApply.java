package com.robin.basis.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.time.LocalDateTime;


@MappingEntity(table = "t_user_apply")
@Data
public class UserApply extends BaseObject {
    @MappingField(increment = true,primary = true)
    private Long id;
    @MappingField
    private Long userId;
    @MappingField
    private String email;
    @MappingField
    private String phoneNum;
    @MappingField
    private LocalDateTime applyTm;
    @MappingField
    private String status;
    private String checkCode;
}
