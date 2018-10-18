package com.robin.comm.util.lineprocess;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.util.lineprocess</p>
 * <p>
 * <p>Copyright: Copyright (c) 2017 create at 2017年11月10日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public interface MapResultProcess<T extends Serializable> {
    void doBefore(Map<String,T> record);
    void doAfter(Map<String,T> record);
    void doWithLine(Map<String,T> record);
}
