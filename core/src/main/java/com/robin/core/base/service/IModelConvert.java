package com.robin.core.base.service;

/**
 * <p>Project:  sadp-server</p>
 *
 * <p>Description: IModelConvert </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-04-02</p>
 *
 * <p>Company: seaboxdata</p>
 *
 * @author luoming
 * @version 1.0
 */
@FunctionalInterface
public interface IModelConvert<T> {
    T doConvert(Object input);
}
