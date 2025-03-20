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
package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@Data
@TableName("t_sys_org_info")
public class SysOrg extends AbstractMybatisModel
{
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pid;
    private String orgCode;
    private String orgName;
    private String orgAbbr;
    private String remark;
    private String treeCode;
    private String orderNo;
    public static String STATUS_ACTIVE = "1";
    public static String STATUS_INACTIVE = "0";
    public static String ORGTYPE_CUSTTOP="1";
    public static String TOP_TREECODE="0001";


}
