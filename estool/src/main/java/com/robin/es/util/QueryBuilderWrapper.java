package com.robin.es.util;

import org.elasticsearch.index.query.QueryBuilder;

@FunctionalInterface
public interface QueryBuilderWrapper {
    void wrapBuilder(QueryBuilder queryBuilder);
}
