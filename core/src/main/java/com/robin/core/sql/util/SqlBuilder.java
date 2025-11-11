package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

public class SqlBuilder {
    private final Map<Class<? extends BaseObject>, AnnotationRetriever.EntityContent> entityClassMap = new HashMap<>();
    private final Map<Class<? extends BaseObject>, String> tabAliasMap = new HashMap<>();
    private final Map<Class<? extends BaseObject>,Map<String,String>> columnAliasMap=new HashMap<>();
    private final Map<Class<? extends BaseObject>, Map<String, FieldContent>> fieldMap = new HashMap<>();
    private final List<Pair<Class<? extends BaseObject>,FieldContent>> selectFields=new ArrayList<>();
    private final Map<String, String> newColumnMap = new LinkedHashMap<>();
    private BaseSqlGen sqlGen;
    private final List<Triple<Const.OPERATOR, Const.LINKOPERATOR, SqlBuilder>> subQueryList = new ArrayList<>();
    private final List<Join> joinList = new ArrayList<>();
    private final FilterConditionBuilder conditionBuilder = new FilterConditionBuilder();
    private final List<Object> objectParams=new ArrayList<>();

    private SqlBuilder() {

    }

    public static SqlBuilder newBuilder() {
        return new SqlBuilder();
    }

    public SqlBuilder sqlGen(BaseSqlGen sqlGen) {
        this.sqlGen = sqlGen;
        return this;
    }

    public SqlBuilder addEntity(Class<? extends BaseObject> clazz, String aliasName) {
        if (!entityClassMap.containsKey(clazz)) {
            AnnotationRetriever.EntityContent entityContent = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, FieldContent> fields = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            if (!CollectionUtils.isEmpty(fields)) {
                entityClassMap.put(clazz, entityContent);
                fieldMap.put(clazz, fields);
            }
            if (!ObjectUtils.isEmpty(aliasName)) {
                tabAliasMap.put(clazz, aliasName);
            }
        }
        return this;
    }
    public <T extends BaseObject> SqlBuilder selectAs(PropertyFunction<T, ?> function,String aliasName) {
        Class<? extends BaseObject> clazz = AnnotationRetriever.getFieldClass(function);
        appendFields(function);
        String fieldName = AnnotationRetriever.getFieldName(function);
        if(!columnAliasMap.containsKey(clazz)){
            Map<String,String> tmap=new HashMap<>();
            tmap.put(fieldName,aliasName);
            columnAliasMap.put(clazz,tmap);
        }else{
            columnAliasMap.get(clazz).put(fieldName,aliasName);
        }
        return this;
    }

    public <T extends BaseObject> SqlBuilder select(PropertyFunction<T, ?>... propertyFunctions) {
        if (propertyFunctions.length > 0) {
            for (PropertyFunction<?, ?> function : propertyFunctions) {
                appendFields(function);

            }
        }
        return this;
    }

    private void appendFields(PropertyFunction<?, ?> function) {
        Class<? extends BaseObject> clazz = AnnotationRetriever.getFieldClass(function);
        if (!entityClassMap.containsKey(clazz)) {
            addEntity(clazz, null);
        }
        String fieldName = AnnotationRetriever.getFieldName(function);
        selectFields.add(Pair.of(clazz,fieldMap.get(clazz).get(fieldName)));
    }

    public <L extends BaseObject,R extends BaseObject> SqlBuilder join(PropertyFunction<L, ?> leftColumn, PropertyFunction<R, ?> rightColumn, Const.JOINTYPE joinType) {
        Assert.isTrue(leftColumn != null && rightColumn != null, "");
        addEntity(AnnotationRetriever.getFieldClass(leftColumn), null);
        addEntity(AnnotationRetriever.getFieldClass(rightColumn), null);
        joinList.add(new Join(leftColumn, rightColumn, joinType));
        return this;
    }

    public <L extends BaseObject,R extends BaseObject> SqlBuilder join(PropertyFunction<L, ?> leftColumn, String leftAlias, PropertyFunction<R, ?> rightColumn, String rightAlias, Const.JOINTYPE joinType) {
        Assert.isTrue(leftColumn != null && rightColumn != null, "");
        addEntity(AnnotationRetriever.getFieldClass(leftColumn), leftAlias);
        addEntity(AnnotationRetriever.getFieldClass(rightColumn), rightAlias);
        joinList.add(new Join(leftColumn, rightColumn, joinType));
        return this;
    }

    public SqlBuilder exists(SqlBuilder newBuilder, Const.LINKOPERATOR linkoperator) {
        subQueryList.add(Triple.of(Const.OPERATOR.EXISTS,linkoperator, newBuilder));
        return this;
    }

    public SqlBuilder notExists(SqlBuilder newBuilder,Const.LINKOPERATOR linkoperator) {
        subQueryList.add(Triple.of(Const.OPERATOR.NOTEXIST,linkoperator, newBuilder));
        return this;
    }

    public SqlBuilder in(SqlBuilder newBuilder,Const.LINKOPERATOR linkoperator) {
        subQueryList.add(Triple.of(Const.OPERATOR.IN,linkoperator, newBuilder));
        return this;
    }
    public SqlBuilder union(SqlBuilder newBuilder) {
        subQueryList.add(Triple.of(Const.OPERATOR.UNION, Const.LINKOPERATOR.LINK_AND, newBuilder));
        return this;
    }
    public SqlBuilder unionAll(SqlBuilder newBuilder) {
        subQueryList.add(Triple.of(Const.OPERATOR.UNIONALL, Const.LINKOPERATOR.LINK_AND, newBuilder));
        return this;
    }

    public SqlBuilder notIn(SqlBuilder newBuilder,Const.LINKOPERATOR linkoperator) {
        subQueryList.add(Triple.of(Const.OPERATOR.NOTIN,linkoperator, newBuilder));
        return this;
    }

    public SqlBuilder not(SqlBuilder newBuilder,Const.LINKOPERATOR linkoperator) {
        subQueryList.add(Triple.of(Const.OPERATOR.NOT,linkoperator, newBuilder));
        return this;
    }

    public FilterConditionBuilder getConditionBuilder() {
        conditionBuilder.aliasMap(tabAliasMap);
        return conditionBuilder;
    }

    public SqlBuilder selectCase(String newColumnName, String caseFields, Map<Object, Object> whenConditions) {
        StringBuilder builder = new StringBuilder();
        builder.append("CASE ").append(caseFields);
        String defaultValue = null;
        Iterator<Map.Entry<Object, Object>> iter = whenConditions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Object, Object> entry = iter.next();
            if (entry.getKey() != null) {
                if (String.class.isAssignableFrom(entry.getKey().getClass())) {
                    builder.append(" WHEN ").append(entry.getKey().toString());
                } else if (entry.getKey().getClass().getInterfaces().length > 0 && List.class.isAssignableFrom(entry.getKey().getClass().getInterfaces()[0])) {
                    List<Object> tList = (List<Object>) entry.getKey();
                    builder.append(" WHEN ");
                    wrapList(builder, tList);
                }
                if (String.class.isAssignableFrom(entry.getValue().getClass())) {
                    builder.append(" THEN ").append(entry.getValue().toString());
                } else if (entry.getKey().getClass().getInterfaces().length > 0 && List.class.isAssignableFrom(entry.getKey().getClass().getInterfaces()[0])) {
                    builder.append(" THEN ");
                    List<Object> tList = (List<Object>) entry.getKey();
                    wrapList(builder, tList);
                }
            } else {
                StringBuilder builder1 = new StringBuilder();
                if (String.class.isAssignableFrom(entry.getValue().getClass())) {
                    builder1.append(entry.getValue());
                } else if (entry.getKey().getClass().getInterfaces().length > 0 && List.class.isAssignableFrom(entry.getKey().getClass().getInterfaces()[0])) {
                    List<Object> tList = (List<Object>) entry.getKey();
                    wrapList(builder1, tList);
                }
                defaultValue = builder1.toString();
            }
        }
        if (defaultValue != null) {
            builder.append(" ELSE ").append(defaultValue).append(" END ");
        }
        newColumnMap.put(newColumnName, builder.toString());
        return this;
    }

    public SqlBuilder selectSubQuery(String newColumnName, SqlBuilder builder) {
        newColumnMap.put(newColumnName, builder.getAppendSql());
        return this;
    }

    private void wrapList(StringBuilder builder, List<Object> tList) {
        for (Object tmpObj : tList) {
            wrapField(builder, tmpObj);
        }
    }

    private void wrapField(StringBuilder builder, Object tmpObj) {
        if (PropertyFunction.class.isAssignableFrom(tmpObj.getClass())) {
            Class<? extends BaseObject> clazz = AnnotationRetriever.getFieldClass((PropertyFunction<? extends BaseObject, ?>) tmpObj);
            if (tabAliasMap.containsKey(clazz)) {
                builder.append(tabAliasMap.get(clazz)).append(".");
            }
            String fieldName = AnnotationRetriever.getFieldName((PropertyFunction<? extends BaseObject, ?>) tmpObj);
            builder.append(fieldMap.get(clazz).get(fieldName).getFieldName());
        } else {
            builder.append(tmpObj);
        }
    }

    public SqlBuilder function(String newColumnName, String functionName, Object... params) {
        Assert.isTrue(!ObjectUtils.isEmpty(newColumnName) && !ObjectUtils.isEmpty(functionName) && params.length > 0, "");
        StringBuilder builder = new StringBuilder();
        builder.append(functionName).append("(");
        for (Object tmpObj : params) {
            wrapField(builder,tmpObj);
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(")");
        newColumnMap.put(newColumnName, builder.toString());
        return this;
    }

    public SqlBuilder arithmetic(String newColumnName, Object... params) {
        Assert.isTrue(!ObjectUtils.isEmpty(newColumnName) && params.length > 0, "");
        StringBuilder builder = new StringBuilder();
        for (Object tmpObj : params) {
            wrapField(builder,tmpObj);
        }
        newColumnMap.put(newColumnName, builder.toString());
        return this;
    }

    public String getAppendSql() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        StringBuilder whereBuilder=new StringBuilder();
        StringBuilder joinBuilder=new StringBuilder();
        //exists Columns
        for(Pair<Class<? extends BaseObject>,FieldContent> pair:selectFields){
            if (tabAliasMap.containsKey(pair.getKey())) {
                builder.append(tabAliasMap.get(pair.getKey())).append(".");
            }
            builder.append(pair.getValue().getFieldName());
            if(columnAliasMap.containsKey(pair.getKey()) && columnAliasMap.get(pair.getKey()).containsKey(pair.getValue().getFieldName())){
                builder.append(" AS ").append(columnAliasMap.get(pair.getKey()).get(pair.getValue().getFieldName()));
            }
            builder.append(",");
        }

        //new Columns
        if (!CollectionUtils.isEmpty(newColumnMap)) {
            for (Map.Entry<String, String> entry : newColumnMap.entrySet()) {
                //sub Query
                if (entry.getValue().toLowerCase().contains("select")) {
                    builder.append("(").append(entry.getValue()).append(") as ").append(entry.getKey());
                } else {
                    builder.append(entry.getValue()).append(" as ").append(entry.getKey()).append(",");
                }
            }
        }
        builder.delete(builder.length()-1,builder.length());
        builder.append(" FROM ");
        //join
        boolean appendFirst=true;
        if (!CollectionUtils.isEmpty(joinList)) {
            for (Join join : joinList) {
                if(tabAliasMap.containsKey(join.getLeftClass()) && appendFirst){
                    joinBuilder.append(entityClassMap.get(join.getLeftClass()).getTableSchemaName());
                    joinBuilder.append(" ").append(tabAliasMap.get(join.getLeftClass())).append(" ");
                    appendFirst=false;
                }
                joinBuilder.append(join.getJoinType().getValue()).append(" JOIN ");
                joinBuilder.append(entityClassMap.get(join.getRightClass()).getTableSchemaName());
                if(tabAliasMap.containsKey(join.getRightClass())){
                    joinBuilder.append(" ").append(tabAliasMap.get(join.getRightClass()));
                }
                joinBuilder.append(" ON ");
                if(tabAliasMap.containsKey(join.getLeftClass())){
                    joinBuilder.append(tabAliasMap.get(join.getLeftClass())).append(".");
                }
                joinBuilder.append(fieldMap.get(join.getLeftClass()).get(AnnotationRetriever.getFieldName(join.getLeftColumn())).getFieldName()).append("=");
                if(tabAliasMap.containsKey(join.getRightClass())){
                    joinBuilder.append(tabAliasMap.get(join.getRightClass())).append(".");
                }
                joinBuilder.append(fieldMap.get(join.getRightClass()).get(AnnotationRetriever.getFieldName(join.getRightColumn())).getFieldName()).append(" ");
            }
        }
        //where Condition
        extractQueryParts(conditionBuilder.build(),whereBuilder);
        //sub Query
        if(!CollectionUtils.isEmpty(subQueryList)){
            for(Triple<Const.OPERATOR, Const.LINKOPERATOR,SqlBuilder> pair:subQueryList){
                if(whereBuilder.length()>0 && !pair.getLeft().equals(Const.OPERATOR.UNION) && !pair.getLeft().equals(Const.OPERATOR.UNIONALL)){
                    whereBuilder.append(pair.getMiddle().getSignal()).append(" ");
                }
                whereBuilder.append(pair.getLeft().getSignal()).append("(");
                whereBuilder.append(pair.getRight().getAppendSql());
                objectParams.addAll(pair.getRight().getObjectParams());
                whereBuilder.append(")");
            }
        }
        if(joinBuilder.length()>0) {
            builder.append(joinBuilder);
        }
        if(whereBuilder.length()>0){
            builder.append(" WHERE ").append(whereBuilder);
        }
        return builder.toString();
    }


    private void extractQueryParts(FilterCondition condition, StringBuilder buffer) {
        condition.setAliasMap(tabAliasMap);
        if (!CollectionUtils.isEmpty(condition.getConditions())) {
            if (condition.getConditions().size() == 1) {
                buffer.append(condition.getConditions().get(0).toPreparedSQLPart(objectParams));
            } else {
                buffer.append(condition.toPreparedSQLPart(objectParams));
            }
        } else {
            buffer.append(condition.toPreparedSQLPart(objectParams));
        }
    }

    public List<Object> getObjectParams() {
        return objectParams;
    }

    @Getter
    public class Join {
        private final Class<? extends BaseObject> leftClass;
        private final Class<? extends BaseObject> rightClass;
        private final PropertyFunction<? extends BaseObject, ?> leftColumn;
        private final PropertyFunction<? extends BaseObject, ?> rightColumn;
        private final Const.JOINTYPE joinType;

        public Join(PropertyFunction<? extends BaseObject, ?> leftColumn, PropertyFunction<? extends BaseObject, ?> rightColumn, Const.JOINTYPE joinType) {
            this.leftClass = AnnotationRetriever.getFieldClass(leftColumn);
            this.leftColumn = leftColumn;
            this.rightClass = AnnotationRetriever.getFieldClass(rightColumn);
            this.rightColumn = rightColumn;
            this.joinType = joinType;
        }

    }
}
