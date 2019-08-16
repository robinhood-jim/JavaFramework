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
package com.robin.core.fileaccess.holder;

import java.io.Closeable;

/**
 * <p>Project:  core</p>
 *
 * <p>Description:Iholder.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月28日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public interface IHolder extends Closeable {
	void setBusyTag(boolean tag);
	boolean isResourceAvaiable();
	boolean getBusyTag();
}
