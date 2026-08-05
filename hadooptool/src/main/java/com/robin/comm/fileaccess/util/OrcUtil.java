package com.robin.comm.fileaccess.util;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.calcite.sql.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.hadoop.hive.ql.io.sarg.PredicateLeaf;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgument;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.orc.TypeDescription;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class OrcUtil {
    public static TypeDescription getSchema(DataCollectionMeta colmeta){
        if(!CollectionUtils.isEmpty(colmeta.getColumnList())){
            TypeDescription schema=TypeDescription.createStruct();
            for(DataSetColumnMeta columnMeta:colmeta.getColumnList()){
                if(StringUtils.isEmpty(columnMeta.getColumnType())){
                    continue;
                }
                switch (columnMeta.getColumnType()){
                    case SHORT:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createShort());
                        break;
                    case INTEGER:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createInt());
                        break;
                    case NUMERIC:
                    case DOUBLE:
                    case FLOAT:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createDouble());
                        break;
                    case DECIMAL:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createDecimal());
                        break;
                    case LONG:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createLong());
                        break;
                    case DATE:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createDate());
                        break;
                    case TIMESTAMP:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createTimestamp());
                        break;
                    case STRING:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createString());
                        break;
                    case BLOB:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createBinary());
                        break;
                    default:
                        throw new InputMismatchException("input type not support!");
                }
            }
            return schema;
        }
        return null;
    }
    public static void wrapValue(TypeDescription schema, String columnName, ColumnVector vector, int row, Map<String,Object> valueMap){
        if(vector.noNulls || !vector.isNull[row]){
            switch (schema.getCategory()){
                case BOOLEAN:
                    valueMap.put(columnName,((LongColumnVector)vector).vector[row]!=0);
                    break;
                case SHORT:
                    valueMap.put(columnName,Long.valueOf(((LongColumnVector)vector).vector[row]).shortValue());
                    break;
                case INT:
                    valueMap.put(columnName,Long.valueOf(((LongColumnVector)vector).vector[row]).intValue());
                    break;
                case LONG:
                    valueMap.put(columnName,((LongColumnVector)vector).vector[row]);
                    break;
                case FLOAT:
                case DOUBLE:
                    valueMap.put(columnName,((DoubleColumnVector)vector).vector[row]);
                    break;
                case DECIMAL:
                    valueMap.put(columnName,((DecimalColumnVector)vector).vector[row].getHiveDecimal().bigDecimalValue());
                    break;
                case STRING:
                case CHAR:
                case VARCHAR:
                    valueMap.put(columnName,((BytesColumnVector)vector).toString(row));
                    break;
                case DATE:
                    valueMap.put(columnName,new Timestamp(((LongColumnVector)vector).vector[row]));
                    break;
                case TIMESTAMP:
                case TIMESTAMP_INSTANT:
                    valueMap.put(columnName,((TimestampColumnVector)vector).asScratchTimestamp(row));
                    break;
                case LIST:
                case MAP:
                case STRUCT:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type " + schema.toString());
            }
        }
    }
    public static void walkCondition(AbstractFileIterator reader,SqlNode node, SearchArgument.Builder argumentBuilder){
        if (SqlBasicCall.class.isAssignableFrom(node.getClass())) {
            List<SqlNode> nodes = ((SqlBasicCall) node).getOperandList();
            if (SqlIdentifier.class.isAssignableFrom(nodes.get(0).getClass()) && SqlLiteral.class.isAssignableFrom(nodes.get(1).getClass())) {
                OrcUtil.parseOperator(reader,node,argumentBuilder);
            } else {
                boolean canUse=false;
                List<SqlNode> nodes1=((SqlBasicCall) node).getOperandList();
                if(SqlKind.OR.equals(node.getKind())){
                    argumentBuilder.startOr();
                    canUse=true;
                }else if(SqlKind.AND.equals(node.getKind())){
                    argumentBuilder.startAnd();
                    canUse=true;
                }
                if(canUse) {
                    walkCondition(reader,nodes1.get(0), argumentBuilder);
                    walkCondition(reader,nodes1.get(0), argumentBuilder);
                    argumentBuilder.end();
                }else{
                    argumentBuilder.literal(SearchArgument.TruthValue.YES);
                }
            }
        }
    }
    public static void parseOperator(AbstractFileIterator reader, SqlNode node, SearchArgument.Builder argumentBuilder){
        List<SqlNode> nodes=((SqlBasicCall)node).getOperandList();
        String column=((SqlIdentifier)nodes.get(0)).getSimple();
        Pair<PredicateLeaf.Type,Object> pair=returnType(reader.getColumnMap(),column,((SqlLiteral)nodes.get(1)).getValue());
        switch (node.getKind()) {
            case GREATER_THAN:
                argumentBuilder.startNot();
                argumentBuilder.lessThanEquals(column,pair.getKey(),pair.getValue());
                argumentBuilder.end();
                break;
            case GREATER_THAN_OR_EQUAL:
                argumentBuilder.startNot();
                argumentBuilder.lessThan(column,pair.getKey(),pair.getValue());
                argumentBuilder.end();
                break;
            case EQUALS:
                argumentBuilder.nullSafeEquals(column, pair.getKey(), pair.getValue());
                break;
            case LESS_THAN:
                argumentBuilder.lessThan(column, pair.getKey(), pair.getValue());
                break;
            case LESS_THAN_OR_EQUAL:
                argumentBuilder.lessThanEquals(column, pair.getKey(), pair.getValue());
                break;
            case BETWEEN:
                argumentBuilder.between(column, pair.getKey(), pair.getValue(),pair.getValue());
                break;
            case IN:
                argumentBuilder.in(column,pair.getKey(),reader.getSegment().getInPartMap().get(column).stream().map(f-> returnWithType(reader.getColumnMap().get(column),f)).collect(Collectors.toList()).toArray());
                break;
            case NOT_EQUALS:
                argumentBuilder.startNot();
                argumentBuilder.equals(column, pair.getKey(), pair.getValue());
                argumentBuilder.end();
                break;
            default:
                throw new OperationNotSupportException(" not supported!");

        }
    }
    private static Pair<PredicateLeaf.Type,Object> returnType(Map<String, DataSetColumnMeta> columnMap, String columnName,Object value){
        if(columnMap.containsKey(columnName)){
            return returnType(columnMap.get(columnName),value);
        }else {
            return returnType(columnMap.get(columnName.toUpperCase()),value);
        }
    }
    private static Pair<PredicateLeaf.Type,Object> returnType(DataSetColumnMeta columnMeta, Object value){
        PredicateLeaf.Type type=null;
        Object targetVal=null;
        try {
            switch (columnMeta.getColumnType()) {
                case INTEGER:
                case LONG:
                    type = PredicateLeaf.Type.LONG;
                    targetVal = Long.parseLong(value.toString());
                    break;
                case DOUBLE:
                    type = PredicateLeaf.Type.FLOAT;
                    targetVal =Double.parseDouble(value.toString());
                    break;
                case DECIMAL:
                    type = PredicateLeaf.Type.DECIMAL;
                    targetVal =new HiveDecimalWritable(HiveDecimal.create(Double.parseDouble(value.toString())));
                    break;
                case DATE:
                    type = PredicateLeaf.Type.DATE;
                    targetVal = Long.parseLong(value.toString());
                    break;
                case TIMESTAMP:
                    type = PredicateLeaf.Type.TIMESTAMP;
                    if(Timestamp.class.isAssignableFrom(value.getClass())){
                        targetVal= value;
                    }else {
                        targetVal = new Timestamp(Long.parseLong(value.toString()));
                    }
                    break;
                default:
                    type = PredicateLeaf.Type.STRING;
                    targetVal = value.toString();
            }
        }catch (Exception ex){

        }
        return Pair.of(type,targetVal);
    }
    private static Object returnWithType(DataSetColumnMeta columnMeta,Object value){
        Object targetVal=null;
        switch (columnMeta.getColumnType()) {
            case INTEGER:
            case LONG:
                targetVal = Long.parseLong(value.toString());
                break;
            case DOUBLE:
            case DECIMAL:
                targetVal = Double.parseDouble(value.toString());
                break;
            case DATE:
            case TIMESTAMP:
                targetVal = new Timestamp(Long.parseLong(value.toString()));
                break;
            default:
                targetVal = value.toString();
        }
        return targetVal;
    }
    public static Schema parseSchemaByType(TypeDescription schema,DataCollectionMeta colmeta) {
        List<TypeDescription> columns=schema.getChildren();
        for(TypeDescription column:columns){
            colmeta.addColumnMeta(column.getFullFieldName(),parseFromCategory(column.getCategory()),null);
        }
        return AvroUtils.getSchemaFromMeta(colmeta);
    }
    private static Const.CLASSTYPE parseFromCategory(TypeDescription.Category category){
        Const.CLASSTYPE columnType= Const.CLASSTYPE.STRING;
        switch (category){
            case LONG:
                columnType= Const.CLASSTYPE.LONG;
                break;
            case INT:
                columnType= Const.CLASSTYPE.INTEGER;
                break;
            case FLOAT:
                columnType= Const.CLASSTYPE.FLOAT;
                break;
            case SHORT:
                columnType= Const.CLASSTYPE.SHORT;
                break;
            case DOUBLE:
            case DECIMAL:
                columnType= Const.CLASSTYPE.DOUBLE;
                break;
            case DATE:
                columnType= Const.CLASSTYPE.DATE;
                break;
            case TIMESTAMP:
            case TIMESTAMP_INSTANT:
                columnType= Const.CLASSTYPE.TIMESTAMP;
                break;
            case VARCHAR:
            case STRING:
                columnType= Const.CLASSTYPE.STRING;
                break;
            case BINARY:
                columnType= Const.CLASSTYPE.BLOB;
                break;
        }
        return columnType;
    }
}
