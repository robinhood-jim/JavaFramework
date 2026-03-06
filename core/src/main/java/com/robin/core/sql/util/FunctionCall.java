package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

@Getter
@Setter
public class FunctionCall<L extends BaseObject, R extends BaseObject> {
    FunctionCall() {

    }

    private Object leftColumn;
    private PropertyFunction<L, ?> leftColumnProp;
    private Object rightColumn;
    private PropertyFunction<R, ?> rightProp;
    private Const.ARITHMETIC operator;
    private String functionName;
    private SqlBuilder sqlBuilder;

    public static class FunctionBuilder<L extends BaseObject, R extends BaseObject> {
        private FunctionCall call = new FunctionCall<L, R>();

        private FunctionBuilder(SqlBuilder sqlBuilder) {
            call.setSqlBuilder(sqlBuilder);
        }

        public FunctionBuilder<L, R> leftProp(PropertyFunction<L, ?> propertyFunction) {
            call.setLeftColumnProp(propertyFunction);
            return this;
        }

        public FunctionBuilder<L, R> leftColumn(Object leftColumn) {
            call.setLeftColumn(leftColumn);
            return this;
        }

        public FunctionBuilder<L, R> rightProp(PropertyFunction<R, ?> propertyFunction) {
            call.setRightProp(propertyFunction);
            return this;
        }

        public FunctionBuilder<L, R> rightColumn(Object rightColumn) {
            call.setRightColumn(rightColumn);
            return this;
        }

        public FunctionBuilder<L, R> operator(Const.ARITHMETIC operator) {
            call.setOperator(operator);
            return this;
        }

        public FunctionBuilder<L, R> functionName(String name) {
            call.setFunctionName(name);
            return this;
        }

        public FunctionCall<L, R> build() {
            return call;
        }

    }

    public static <L extends BaseObject, R extends BaseObject> FunctionBuilder newBuilder(SqlBuilder sqlBuilder) {
        return new FunctionBuilder<L, R>(sqlBuilder);
    }

    public String getFormula(boolean inner) {
        StringBuilder builder = new StringBuilder();
        if (inner) {
            builder.append("(");
        }
        boolean hasFuncName = !ObjectUtils.isEmpty(functionName);
        if (hasFuncName) {
            builder.append(functionName).append("(");
        }
        appendPart(builder, leftColumn, leftColumnProp);
        if (rightColumn != null || rightProp != null) {
            builder.append(operator.getValue());
            appendPart(builder, rightColumn, rightProp);
        }
        if (hasFuncName) {
            builder.append(")");
        }
        if (inner) {
            builder.append(")");
        }
        return builder.toString();
    }

    private <T extends BaseObject> void appendPart(StringBuilder builder, Object columnObj, PropertyFunction<T, ?> propertyFunction) {
        if (columnObj != null) {
            if (FunctionCall.class.isAssignableFrom(columnObj.getClass())) {
                builder.append(((FunctionCall) columnObj).getFormula(true));
            } else {
                builder.append(columnObj);
            }
        } else if (propertyFunction != null) {
            Class<? extends BaseObject> clazz = AnnotationRetriever.getFieldClass(propertyFunction);
            String fieldName = AnnotationRetriever.getFieldColumnName(propertyFunction);
            if (sqlBuilder.getTabAliasMap().containsKey(clazz)) {
                builder.append(sqlBuilder.getTabAliasMap().get(clazz)).append(".");
            }
            builder.append(sqlBuilder.getFieldMap().get(clazz).get(fieldName).getFieldName());
        }
    }
}

