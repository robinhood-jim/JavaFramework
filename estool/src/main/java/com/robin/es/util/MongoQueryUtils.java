package com.robin.es.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.util.Const;
import org.bson.conversions.Bson;
import org.springframework.util.Assert;

import static com.mongodb.client.model.Filters.*;

public class MongoQueryUtils {
    public static Bson getCondition(FieldContent content, Const.OPERATOR operator, Object[] value) {
        String columnName = content.getPropertyName();
        String columnType = AnnotationRetriever.getFieldType(content);
        Assert.isTrue(value!=null && value.length>1,"");
        Bson filter=null;
        switch (operator) {
            case GE:
                filter=gte(columnName, CommEsQueryUtils.parseValue(columnType, value[0].toString()));
                break;
            case GT:
                filter=gt(columnName,CommEsQueryUtils.parseValue(columnType, value[0].toString()));
                break;
            case LE:
                filter=lte(columnName,CommEsQueryUtils.parseValue(columnType, value[0].toString()));
                break;
            case LT:
                filter=lt(columnName,CommEsQueryUtils.parseValue(columnType, value[0].toString()));
                break;
            case NE:
                filter=ne(columnName,CommEsQueryUtils.parseValue(columnType, value[0].toString()));
                break;
            case NULL:
                filter=eq(columnName,null);
                break;
            case NOTNULL:
                filter=ne(columnName,null);
                break;
            case LLIKE:
            case RLIKE:
            case LIKE:
                filter=regex(columnName,".*?\\" +value[0].toString()+ ".*");
                break;
            case BETWEEN:
                Assert.isTrue(value.length==2,"between must have two parameters");
                filter=and(lte(columnName,CommEsQueryUtils.parseValue(columnType, value[1].toString())),gte(columnName,CommEsQueryUtils.parseValue(columnType, value[0].toString())));
                break;
            default:
                filter=eq(columnName,CommEsQueryUtils.parseValue(columnType, value[0].toString()));
        }
        return filter;
    }
}
