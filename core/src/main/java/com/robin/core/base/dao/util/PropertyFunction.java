package com.robin.core.base.dao.util;

import java.io.Serializable;
import java.util.function.Function;
@FunctionalInterface
public interface PropertyFunction<T,R> extends Function<T,R>, Serializable {
}
