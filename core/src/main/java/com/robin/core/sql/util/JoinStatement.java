package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JoinStatement {
    private List<Class<? extends BaseObject>> joinModelClass = new ArrayList<>();
    private List<PropertyFunction<? extends BaseObject, ?>> selectFields = new ArrayList<>();
    private List<Triple<PropertyFunction<? extends BaseObject, ?>, PropertyFunction<? extends BaseObject, ?>, Const.JOINTYPE>> joins = new ArrayList<>();
    private FilterCondition condition;

    private JoinStatement() {

    }

    public static class Builder {
        private JoinStatement statement;

        public Builder() {
            statement = new JoinStatement();
        }

        public JoinStatement.Builder join(PropertyFunction<? extends BaseObject, ?> leftColumn, PropertyFunction<? extends BaseObject, ?> rightColumn, Const.JOINTYPE joinType) {
            String leftColumnType = AnnotationRetriever.getFieldType(leftColumn);
            String rightColumnType = AnnotationRetriever.getFieldType(rightColumn);
            Assert.isTrue(leftColumnType.equals(rightColumnType), " join columns does not have same type!");
            statement.getJoins().add(Triple.of(leftColumn, rightColumn, joinType));
            return this;
        }

        public JoinStatement.Builder select(PropertyFunction<? extends BaseObject, ?>... fields) {
            statement.getSelectFields().addAll(Arrays.stream(fields).collect(Collectors.toList()));
            return this;
        }
        public JoinStatement.Builder withCondition(FilterCondition condition){
            statement.setCondition(condition);
            return this;
        }

        public JoinStatement build(){
            return statement;
        }

    }


}
