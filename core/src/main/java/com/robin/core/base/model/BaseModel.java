package com.robin.core.base.model;



import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public abstract class BaseModel implements Serializable {

    @TableField(fill = FieldFill.INSERT)
    private Long creator;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long modifier;
    @TableField(fill = FieldFill.INSERT)
    private Timestamp createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Timestamp modifyTime;
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
    @TableField(fill = FieldFill.INSERT)
    private Integer ifValid;


}
