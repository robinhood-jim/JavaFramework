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
package com.robin.core.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Entity
@Table(name="t_test")
@Data
public class TestJPaModel extends BaseObject{
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) 
	private Long id;
	@Column(nullable=false)
	private String name;
	@Column(name="code_desc")
	private String description;
	@Column(name = "cs_id")
	private Integer csId;

}
