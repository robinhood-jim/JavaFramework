/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;


@MappingEntity(value = "t_sys_resource_role_r")
@TableName("t_sys_resource_role_r")
@Data
public class SysResourceRole extends AbstractMybatisModel {

    // primary key
    @MappingField(primary = true, increment = true)
    @TableId(type = IdType.AUTO)
    private Long id;   //

    // fields
    @MappingField(value = "role_id")
    private Long roleId;
    @MappingField(value = "res_id")
    private Long resId;

    public SysResourceRole(Long resId,Long roleId){
        this.resId=resId;
        this.roleId=roleId;
        this.setStatus(Const.VALID);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof SysResourceRole)) {
            return false;
        } else {
            SysResourceRole o = (SysResourceRole) obj;
            if (null == this.getId() || null == o.getId()) {
                return false;
            } else {
                return (this.getId().equals(o.getId()));
            }
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }




}
