package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class JoinCondition {
    private List<Class<? extends BaseObject>> joinModelClass = new ArrayList<>();
    private Map<Class<? extends BaseObject>,String> tableAliasMap =new LinkedHashMap<>();
    private Map<PropertyFunction<? extends BaseObject,?>,String> columnAlias=new LinkedHashMap<>();
    private List<PropertyFunction<? extends BaseObject, ?>> selectFields = new ArrayList<>();
    private List<Triple<PropertyFunction<? extends BaseObject, ?>, PropertyFunction<? extends BaseObject, ?>, Const.JOINTYPE>> joins = new ArrayList<>();
    private FilterCondition condition;

    JoinCondition() {

    }

    public static class Builder {
        private JoinCondition statement;

        public Builder() {
            statement = new JoinCondition();
        }

        public <L extends BaseObject,R extends BaseObject> JoinCondition.Builder join(PropertyFunction<L, ?> leftColumn, PropertyFunction<R, ?> rightColumn, Const.JOINTYPE joinType) {
            String leftColumnType = AnnotationRetriever.getFieldType(leftColumn);
            String rightColumnType = AnnotationRetriever.getFieldType(rightColumn);
            //Assert.isTrue(leftColumnType.equals(rightColumnType), " join columns does not have same type!");
            statement.getJoins().add(Triple.of(leftColumn, rightColumn, joinType));
            Class<L> leftModelClass=AnnotationRetriever.getFieldOwnedClass(leftColumn);
            Class<R> rightModelClass=AnnotationRetriever.getFieldOwnedClass(rightColumn);
            if(!statement.getJoinModelClass().contains(leftModelClass)){
                statement.getJoinModelClass().add(leftModelClass);
            }
            if(!statement.getJoinModelClass().contains(rightModelClass)){
                statement.getJoinModelClass().add(rightModelClass);
            }

            return this;
        }

        public <T extends BaseObject> JoinCondition.Builder select(PropertyFunction<T, ?>... fields) {
            statement.getSelectFields().addAll(Arrays.asList(fields));
            return this;
        }
        public <T extends BaseObject> JoinCondition.Builder select(PropertyFunction<T, ?> field,String alias){
            statement.getSelectFields().add(field);
            statement.getColumnAlias().put(field,alias);
            return this;
        }
        public JoinCondition.Builder withCondition(FilterCondition condition){
            statement.setCondition(condition);
            return this;
        }
        public JoinCondition.Builder alias(Class<? extends BaseObject> modelClass, String aliasName){
            statement.getTableAliasMap().put(modelClass,aliasName);
            return this;
        }

        public JoinCondition build(){
            return statement;
        }

    }
    public String toSql(List<Object> params){
        Map<Class<? extends BaseObject>,List<PropertyFunction<? extends BaseObject,?>>> fieldMap=
                selectFields.stream().collect(Collectors.groupingBy(AnnotationRetriever::getFieldOwnedClass));

        StringBuilder builder=new StringBuilder(Const.SQL_SELECT);
        StringBuilder fieldBuilder=new StringBuilder();
        StringBuilder joinBuilder=new StringBuilder();
        StringBuilder whereBuilder=new StringBuilder();
        if(!CollectionUtils.isEmpty(joinModelClass)) {
            for (Class<? extends BaseObject> modelClass : joinModelClass) {
                if (fieldMap.containsKey(modelClass)){
                    for(PropertyFunction<? extends BaseObject,?> field:fieldMap.get(modelClass)) {
                        fieldBuilder.append(Optional.ofNullable(tableAliasMap.get(modelClass)).map(f -> f + "." +AnnotationRetriever.getFieldColumnName(field)).orElse(AnnotationRetriever.getFieldColumnName(field)));
                        Optional.ofNullable(columnAlias.get(field)).map(f->fieldBuilder.append(" AS ").append(f).append(",")).orElseGet(()->fieldBuilder.append(","));
                    }
                }
            }
            for(Triple<PropertyFunction<? extends BaseObject, ?>, PropertyFunction<? extends BaseObject, ?>, Const.JOINTYPE> join:joins){
                AnnotationRetriever.EntityContent leftTab=AnnotationRetriever.getMappingTableByCache(AnnotationRetriever.getFieldOwnedClass(join.getLeft()));
                AnnotationRetriever.EntityContent rightTab=AnnotationRetriever.getMappingTableByCache(AnnotationRetriever.getFieldOwnedClass(join.getMiddle()));
                Class<? extends BaseObject> leftModelClass=AnnotationRetriever.getFieldOwnedClass(join.getLeft());
                Class<? extends BaseObject> rightModelClass=AnnotationRetriever.getFieldOwnedClass(join.getMiddle());

                String leftColumn=Optional.ofNullable(tableAliasMap.get(leftModelClass)).map(f->f+"."+AnnotationRetriever.getFieldColumnName(join.getLeft())).orElse(AnnotationRetriever.getFieldColumnName(join.getLeft()));

                String rightColumn=Optional.ofNullable(tableAliasMap.get(rightModelClass)).map(f->f+"."+AnnotationRetriever.getFieldColumnName(join.getMiddle())).orElse(AnnotationRetriever.getFieldColumnName(join.getMiddle()));

                String leftTabSep=Optional.ofNullable(tableAliasMap.get(leftModelClass)).map(f->leftTab.getTableName()+" "+f).orElse(leftTab.getTableName());
                String rightTabSep=Optional.ofNullable(tableAliasMap.get(rightModelClass)).map(f->rightTab.getTableName()+" "+f).orElse(rightTab.getTableName());

                switch (join.getRight()){
                    case INNER:
                        joinBuilder.append(leftTabSep).append(",").append(rightTabSep).append(",");
                        whereBuilder.append(leftColumn).append("=").append(rightColumn).append(Const.LINKOPERATOR.LINK_AND.getSignal());
                        break;
                    case LEFT:
                        joinBuilder.append(leftTabSep).append(" left join ").append(rightTabSep)
                                .append(" ON ").append(leftColumn).append("=").append(rightColumn);
                        break;
                    case RIGHT:
                        joinBuilder.append(leftTabSep).append(" right join ").append(rightTabSep)
                                .append(" ON ").append(leftColumn).append("=").append(rightColumn);
                        break;
                    case OUT:
                        joinBuilder.append(leftTabSep).append(" full outer join ").append(rightTabSep)
                                .append(" ON ").append(leftColumn).append("=").append(rightColumn);
                        break;
                    default:
                        joinBuilder.append(leftTabSep).append(",").append(rightTabSep).append(",");
                        whereBuilder.append(leftColumn).append("=").append(rightColumn).append(Const.LINKOPERATOR.LINK_AND.getSignal());
                        break;
                }
            }
            if(!ObjectUtils.isEmpty(condition)){
                condition.setAliasMap(tableAliasMap);
                whereBuilder.append(condition.toPreparedSQLPart(params));
            }
        }
        builder.append(fieldBuilder.substring(0,fieldBuilder.length()-1)).append(Const.SQL_FROM)
                .append(joinBuilder.substring(0,joinBuilder.length()-1)).append(Const.SQL_WHERE).append(whereBuilder);
        return builder.toString();
    }




}
