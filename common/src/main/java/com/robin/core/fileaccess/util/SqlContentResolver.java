package com.robin.core.fileaccess.util;

import cn.hutool.core.util.NumberUtil;
import com.robin.comm.sql.CommSqlParser;
import com.robin.comm.sql.SqlSegment;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import org.apache.calcite.sql.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

public class SqlContentResolver {
    private SqlContentResolver() {

    }

    public static void doCompare(Calculator calculator, SqlNode node) {
        calculator.setRunValue(false);
        switch (node.getKind()) {
            case GREATER_THAN:
            case LESS_THAN:
            case GREATER_THAN_OR_EQUAL:
            case EQUALS:
            case LESS_THAN_OR_EQUAL:
            case NOT_EQUALS:
                List<SqlNode> nodes = ((SqlBasicCall) node).getOperandList();
                getValueBySide(calculator, nodes.get(0), true);
                getValueBySide(calculator, nodes.get(1), false);
                if (!ObjectUtils.isEmpty(calculator.getLeftValue()) && !ObjectUtils.isEmpty(calculator.getRightValue())) {
                    if (NumberUtil.isNumber(calculator.getLeftValue().toString())) {
                        Assert.isTrue(NumberUtil.isNumber(calculator.getLeftValue().toString()) && NumberUtil.isNumber(calculator.getRightValue().toString()), " only number allowed");
                        calculator.setRunValue(cmpNumber(node.getKind(), (Number) calculator.getLeftValue(), (Number) calculator.getRightValue()));
                    } else {
                        calculator.setRunValue(doValueCompare(calculator.getSegment(), node, calculator.getLeftValue(), calculator.getRightValue()));
                    }
                }
                break;
            case IN:
            case NOT_IN:
                List<SqlNode> sqlNodes = ((SqlBasicCall) node).getOperandList();
                SqlIdentifier identifier = (SqlIdentifier) sqlNodes.get(0);
                Set<String> inSets = calculator.getSegment().getInPartMap().get(identifier.toString());
                if (calculator.getInputRecord().containsKey(identifier.toString())) {
                    calculator.setRunValue(SqlKind.IN.equals(node.getKind()) ? inSets.contains(calculator.getInputRecord().get(identifier.toString())) : !inSets.contains(calculator.getInputRecord().get(identifier.toString())));
                } else {
                    calculator.setRunValue(false);
                }
                break;
            case LIKE:
                List<SqlNode> sqlNodes1 = ((SqlBasicCall) node).getOperandList();
                SqlIdentifier identifier1 = (SqlIdentifier) sqlNodes1.get(0);
                calculator.setLeftValue(calculator.getStringLiteralMap().computeIfAbsent(sqlNodes1.get(1).toString(), k -> sqlNodes1.get(1).toString().replace("'", "")));
                calculator.setRightValue(calculator.getInputRecord().containsKey(identifier1.toString()) ? calculator.getInputRecord().get(identifier1.toString()).toString()
                        : calculator.getInputRecord().get(identifier1.toString().toUpperCase()).toString());
                if (!ObjectUtils.isEmpty(calculator.getRightValue())) {
                    if (calculator.getLeftValue().toString().startsWith("%")) {
                        if (calculator.getLeftValue().toString().endsWith("%")) {
                            calculator.setRunValue(calculator.getRightValue().toString().contains(calculator.getLeftValue().toString().substring(1, calculator.getLeftValue().toString().length() - 1)));
                        } else {
                            calculator.setRunValue(calculator.getRightValue().toString().endsWith(calculator.getLeftValue().toString().substring(1)));
                        }
                    } else if (calculator.getLeftValue().toString().endsWith("%")) {
                        calculator.setRunValue(calculator.getRightValue().toString().startsWith(calculator.getLeftValue().toString().substring(0, calculator.getLeftValue().toString().length() - 1)));
                    }
                }
                break;
            case BETWEEN:
                List<SqlNode> childNodes = ((SqlBasicCall) node).getOperandList();
                Assert.isTrue(childNodes.size() == 3, "between must have two values");
                calculator.setCmpColumn(childNodes.get(0).toString());
                checkColumnNumeric(calculator.getSegment(), calculator.getCmpColumn());
                if (!ObjectUtils.isEmpty(calculator.getInputRecord().get(calculator.getCmpColumn()))) {
                    calculator.setLeftValue(((SqlLiteral) childNodes.get(1)).getValue());
                    calculator.setRightValue(((SqlLiteral) childNodes.get(2)).getValue());
                    calculator.setRunValue((Double) calculator.getInputRecord().get(calculator.getCmpColumn()) >= ((Number) calculator.getLeftValue()).doubleValue()
                            && (Double) calculator.getInputRecord().get(calculator.getCmpColumn()) <= ((Number) calculator.getRightValue()).doubleValue());
                } else {
                    calculator.setRunValue(false);
                }
                break;
            case IS_NULL:
            case IS_NOT_NULL:
                calculator.setLeftValue(((SqlBasicCall) node).getOperandList().get(0).toString());
                calculator.setRunValue(!ObjectUtils.isEmpty(calculator.getInputRecord().get(calculator.getLeftValue().toString())) || !ObjectUtils.isEmpty(calculator.getInputRecord().get(calculator.getLeftValue().toString().toUpperCase())));
                calculator.setRunValue(SqlKind.IS_NOT_NULL.equals(node.getKind()) ? calculator.getRunValue() : !calculator.getRunValue());
                break;
            case NOT:
                calculator.setRunValue(!walkTree(calculator, ((SqlBasicCall) node).getOperandList().get(0)));
                break;
            default:
                throw new OperationNotSupportException("can not handle this opertator " + node.getKind());
        }
    }

    public static boolean walkTree(Calculator ca, SqlNode node) {
        boolean cmpOkFlag = false;
        List<SqlNode> childNodes = ((SqlBasicCall) node).getOperandList();
        if (SqlBasicCall.class.isAssignableFrom(node.getClass()) && (SqlKind.AND.equals(node.getKind()) || SqlKind.OR.equals(node.getKind()))) {
            if (childNodes.size() == 2 && !SqlKind.AND.equals(childNodes.get(1).getKind()) && !SqlKind.OR.equals(childNodes.get(1).getKind())) {
                cmpOkFlag = walkTree(ca, childNodes.get(1));
                if (SqlKind.AND.equals(node.getKind()) && !cmpOkFlag) {
                    return false;
                }
                if (SqlKind.OR.equals(node.getKind()) && cmpOkFlag) {
                    return true;
                }
                cmpOkFlag = walkTree(ca, childNodes.get(0));
            } else {
                for (SqlNode node1 : childNodes) {
                    cmpOkFlag = walkTree(ca, node1);
                    if (SqlKind.AND.equals(node.getKind()) && !cmpOkFlag) {
                        return false;
                    }
                    if (SqlKind.OR.equals(node.getKind()) && cmpOkFlag) {
                        return true;
                    }
                }
            }
        } else {
            doCompare(ca, node);
            cmpOkFlag = ca.getRunValue();
        }
        return cmpOkFlag;
    }

    public static boolean doCalculate(Calculator ca, CommSqlParser.ValueParts valueParts) {
        ca.setColumnName(!ObjectUtils.isEmpty(valueParts.getAliasName()) ? valueParts.getAliasName() : valueParts.getIdentifyColumn());
        ca.setLeftValue(null);
        if (SqlKind.LISTAGG.equals(valueParts.getSqlKind())) {
            if (!CollectionUtils.isEmpty(valueParts.getPolandQueue())) {
                ca.getOutputRecord().put(ca.getColumnName(), PolandNotationUtil.computeResult(valueParts.getPolandQueue(), ca.getInputRecord()));
            }
        } else if (SqlKind.CASE.equals(valueParts.getSqlKind())) {
            Assert.isTrue(!CollectionUtils.isEmpty(valueParts.getCaseMap()), "case without switch");
            if (!ObjectUtils.isEmpty(ca.getInputRecord().get(valueParts.getIdentifyColumn()))) {
                ca.setLeftValue(ca.getInputRecord().get(valueParts.getIdentifyColumn()).toString());
            } else if (!ObjectUtils.isEmpty(ca.getInputRecord().get(valueParts.getIdentifyColumn().toUpperCase()))) {
                ca.setLeftValue(ca.getInputRecord().get(valueParts.getIdentifyColumn().toUpperCase()).toString());
            }
            if (ObjectUtils.isEmpty(ca.getLeftValue()) && !ObjectUtils.isEmpty(valueParts.getCaseElseParts())) {
                calculateNode(ca, valueParts.getCaseElseParts(), ca.getColumnName(), ca.getInputRecord(), ca.getOutputRecord());
            } else {
                if (valueParts.getCaseMap().containsKey(ca.getLeftValue().toString())) {
                    calculateNode(ca, valueParts.getCaseMap().get(ca.getLeftValue().toString()), ca.getColumnName(), ca.getInputRecord(), ca.getOutputRecord());
                } else if (!ObjectUtils.isEmpty(valueParts.getCaseElseParts())) {
                    calculateNode(ca, valueParts.getCaseElseParts(), ca.getColumnName(), ca.getInputRecord(), ca.getOutputRecord());
                }
            }
        } else if (SqlKind.FUNCTION.contains(valueParts.getSqlKind())) {
            SqlBasicCall node = (SqlBasicCall) valueParts.getNode();
            ca.setCmpColumn(valueParts.getFunctionName());
            switch (ca.getCmpColumn()) {
                case "substr":
                    List<SqlNode> sqlNodes = node.getOperandList();
                    Assert.isTrue(sqlNodes.size() == 3, "substr function must have three parameters");
                    Assert.isTrue(SqlKind.IDENTIFIER.equals(sqlNodes.get(0).getKind()), " first parameter must be column");
                    ca.setColumnName(sqlNodes.get(0).toString());
                    if (!Const.META_TYPE_STRING.equals(ca.getSegment().getOriginSchemaMap().get(ca.getColumnName()).getColumnType())) {
                        throw new OperationNotSupportException("only string column can substr");
                    }
                    ca.setLeftValue(((Number) ((SqlLiteral) sqlNodes.get(1)).getValue()).intValue());
                    ca.setRightValue(((Number) ((SqlLiteral) sqlNodes.get(2)).getValue()).intValue());
                    ca.setCmpColumn((String) Optional.ofNullable(ca.getInputRecord().get(ca.getColumnName())).orElse(ca.getInputRecord().get(ca.getColumnName().toUpperCase())));
                    if (!ObjectUtils.isEmpty(ca.getRightValue())) {
                        if (ca.getCmpColumn().length() <= (Integer) ca.getLeftValue()) {
                            ca.getOutputRecord().put(ca.getColumnName(), "");
                        } else {
                            if (ca.getCmpColumn().length() < (Integer) ca.getLeftValue() + (Integer) ca.getRightValue()) {
                                ca.setRightValue(ca.getCmpColumn().length() - (Integer) ca.getLeftValue());
                            }
                            ca.getOutputRecord().put(ca.getColumnName(), ca.getCmpColumn().substring((Integer) ca.getLeftValue(), (Integer) ca.getLeftValue() + (Integer) ca.getRightValue()));
                        }
                    }
                    break;
                case "trim":
                    List<SqlNode> childNodes = node.getOperandList();
                    if (SqlLiteral.class.isAssignableFrom(childNodes.get(0).getClass())) {
                        ca.getOutputRecord().put(ca.getColumnName(), childNodes.get(0).toString().trim());
                    } else if (SqlIdentifier.class.isAssignableFrom(childNodes.get(0).getClass())) {
                        ca.getOutputRecord().put(ca.getColumnName(), ca.getInputRecord().get(childNodes.get(0).toString()).toString().trim());
                    }
                    break;
                case "concat":
                    List<SqlNode> childNodes1 = node.getOperandList();
                    if (ca.getBuilder().length() > 0) {
                        ca.getBuilder().delete(0, ca.getBuilder().length());
                    }
                    for (SqlNode tnode : childNodes1) {
                        if (SqlLiteral.class.isAssignableFrom(tnode.getClass())) {
                            ca.getBuilder().append(tnode.toString());
                        } else if (SqlIdentifier.class.isAssignableFrom(tnode.getClass())) {
                            ca.getBuilder().append(ca.getInputRecord().get(tnode.toString()).toString());
                        }
                    }
                    ca.getOutputRecord().put(ca.getColumnName(), ca.getBuilder().toString());
                case "abs":
                    if (SqlLiteral.class.isAssignableFrom(node.getOperandList().get(0).getClass())) {
                        ca.getOutputRecord().put(ca.getColumnName(), Math.abs((Double) ((SqlLiteral) node.getOperandList().get(0)).getValue()));
                    } else if (SqlIdentifier.class.isAssignableFrom(node.getOperandList().get(0).getClass())) {
                        checkColumnNumeric(ca.getSegment(), node.getOperandList().get(0).toString());
                        ca.setCmpColumn(node.getOperandList().get(0).toString());
                        ca.getOutputRecord().put(ca.getColumnName(), Math.abs((Double) ca.getInputRecord().get(ca.getCmpColumn())));
                    }
                    break;
                case "decode":
                    break;
                case "nvl":
                    break;
                default:


            }
        } else if (SqlKind.IDENTIFIER.equals(valueParts.getSqlKind())) {
            if (ca.getInputRecord().containsKey(valueParts.getIdentifyColumn())) {
                ca.getOutputRecord().put(ca.getColumnName(), ca.getInputRecord().get(valueParts.getIdentifyColumn()));
            } else if (ca.getInputRecord().containsKey(valueParts.getIdentifyColumn().toUpperCase())) {
                ca.getOutputRecord().put(ca.getColumnName(), ca.getInputRecord().get(valueParts.getIdentifyColumn().toUpperCase()));
            }

        }
        return false;
    }

    public static void doAggregate(Calculator ca, CommSqlParser.ValueParts valueParts, byte[] key, Map<byte[], Map<String, Object>> groupMap) {
        ca.setColumnName(!ObjectUtils.isEmpty(valueParts.getAliasName()) ? valueParts.getAliasName() : valueParts.getIdentifyColumn());
        if (SqlKind.FUNCTION.contains(valueParts.getSqlKind())) {
            ca.setCmpColumn(valueParts.getFunctionName());
            List<SqlNode> nodes = valueParts.getFunctionParams();
            switch (ca.getCmpColumn()) {
                case "sum":
                    extractValue(ca, valueParts, nodes);
                    if (!ObjectUtils.isEmpty(ca.getLeftValue())) {
                        groupMap.computeIfAbsent(key, k -> {
                            Map<String, Object> retMap = new HashMap<>();
                            retMap.put(ca.getColumnName(), ca.getLeftValue());
                            return retMap;
                        });
                        groupMap.computeIfPresent(key, (k, v) -> {
                            v.put(ca.getColumnName(), (Double) ca.getLeftValue() + (Double) v.get(ca.getColumnName()));
                            return v;
                        });
                    }
                    break;
                case "max":
                    extractValue(ca, valueParts, nodes);
                    if (!ObjectUtils.isEmpty(ca.getLeftValue())) {
                        groupMap.computeIfAbsent(key, k -> {
                            Map<String, Object> retMap = new HashMap<>();
                            retMap.put(ca.getColumnName(), ca.getLeftValue());
                            return retMap;
                        });
                        groupMap.computeIfPresent(key, (k, v) -> {
                            if ((Double) ca.getLeftValue() > (Double) v.get(ca.getColumnName())) {
                                v.put(ca.getColumnName(), ca.getLeftValue());
                            }
                            return v;
                        });
                    }
                    break;
                case "min":
                    extractValue(ca, valueParts, nodes);
                    if (!ObjectUtils.isEmpty(ca.getLeftValue())) {
                        groupMap.computeIfAbsent(key, k -> {
                            Map<String, Object> retMap = new HashMap<>();
                            retMap.put(ca.getColumnName(), ca.getLeftValue());
                            return retMap;
                        });
                        groupMap.computeIfPresent(key, (k, v) -> {
                            if ((Double) ca.getLeftValue() < (Double) v.get(ca.getColumnName())) {
                                v.put(ca.getColumnName(), ca.getLeftValue());
                            }
                            return v;
                        });
                    }
                    break;
                case "avg":
                    break;
                case "count":
                    groupMap.computeIfAbsent(key, k -> {
                        Map<String, Object> retMap = new HashMap<>();
                        retMap.put(ca.getColumnName(), 1);
                        return retMap;
                    });
                    groupMap.computeIfPresent(key, (k, v) -> {
                        v.put(ca.getColumnName(), (Integer)v.get(ca.getColumnName())+1);
                        return v;
                    });
                    break;
            }
        }else if(SqlKind.IDENTIFIER.equals(valueParts.getSqlKind()) && !ObjectUtils.isEmpty(ca.getInputRecord().get(ca.getColumnName()))){
            groupMap.get(key).put(ca.getColumnName(), ca.getInputRecord().get(ca.getColumnName()));
        }else{
            throw new OperationNotSupportException("not supported");
        }

    }

    private static void extractValue(Calculator ca, CommSqlParser.ValueParts valueParts, List<SqlNode> nodes) {
        if (SqlBasicCall.class.isAssignableFrom(nodes.get(0).getClass())) {
            if (!CollectionUtils.isEmpty(valueParts.getPolandQueue())) {
                ca.setLeftValue(PolandNotationUtil.computeResult(valueParts.getPolandQueue(), ca.getInputRecord()));
            }
        } else if (SqlKind.IDENTIFIER.equals(nodes.get(0).getKind())) {
            if (!ObjectUtils.isEmpty(ca.getInputRecord().get(ca.getColumnName()))) {
                ca.setLeftValue(ca.getInputRecord().get(ca.getColumnName()));
            }
        } else {
            throw new OperationNotSupportException(" not support");
        }
    }

    private static void calculateNode(Calculator ca, CommSqlParser.ValueParts parts, String columnName, Map<String, Object> inputRecord, Map<String, Object> retMap) {
        if (!ObjectUtils.isEmpty(parts.getConstantValue())) {
            if (!SqlCharStringLiteral.class.isAssignableFrom(parts.getConstantValue().getClass())) {
                retMap.put(columnName, parts.getConstantValue().getValue());
            } else {
                retMap.put(columnName, ca.getStringLiteralMap().computeIfAbsent(parts.getConstantValue().toString(), k -> parts.getConstantValue().toString().replace("'", "")));
            }
        }
        if (SqlKind.LISTAGG.equals(parts.getSqlKind())) {
            if (!CollectionUtils.isEmpty(parts.getPolandQueue())) {
                retMap.put(columnName, PolandNotationUtil.computeResult(parts.getPolandQueue(), inputRecord));
            }
        } else if (SqlKind.IDENTIFIER.equals(parts.getSqlKind())) {
            if (inputRecord.containsKey(parts.getIdentifyColumn())) {
                retMap.put(columnName, inputRecord.get(parts.getIdentifyColumn()));
            } else if (inputRecord.containsKey(parts.getIdentifyColumn().toUpperCase())) {
                retMap.put(columnName, inputRecord.get(parts.getIdentifyColumn().toUpperCase()));
            } else {
                retMap.put(columnName, null);
            }
        }
    }

    public static void checkColumnNumeric(SqlSegment segment, String columnName) {
        Assert.isTrue(Const.META_TYPE_INTEGER.equals(segment.getOriginSchemaMap().get(columnName).getColumnType())
                || Const.META_TYPE_BIGINT.equals(segment.getOriginSchemaMap().get(columnName).getColumnType())
                || Const.META_TYPE_DOUBLE.equals(segment.getOriginSchemaMap().get(columnName).getColumnType()), "require numeric column");
    }

    private static boolean cmpNumber(SqlKind comparator, Number left, Number right) {
        boolean retValue = false;
        if (Double.class.isAssignableFrom(left.getClass()) || Double.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case GREATER_THAN:
                    retValue = left.doubleValue() > right.doubleValue();
                    break;
                case LESS_THAN:
                    retValue = left.doubleValue() < right.doubleValue();
                    break;
                case GREATER_THAN_OR_EQUAL:
                    retValue = left.doubleValue() >= right.doubleValue();
                    break;
                case LESS_THAN_OR_EQUAL:
                    retValue = left.doubleValue() <= right.doubleValue();
                    break;
                case NOT_EQUALS:
                    retValue = left.doubleValue() != right.doubleValue();
                    break;
                case EQUALS:
                    retValue = left.doubleValue() == right.doubleValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }

        } else if (Integer.class.isAssignableFrom(left.getClass()) || Integer.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case GREATER_THAN:
                    retValue = left.intValue() > right.intValue();
                    break;
                case LESS_THAN:
                    retValue = left.intValue() < right.intValue();
                    break;
                case GREATER_THAN_OR_EQUAL:
                    retValue = left.intValue() >= right.intValue();
                    break;
                case LESS_THAN_OR_EQUAL:
                    retValue = left.intValue() <= right.intValue();
                    break;
                case NOT_EQUALS:
                    retValue = left.intValue() != right.intValue();
                    break;
                case EQUALS:
                    retValue = left.intValue() == right.intValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }
        } else if (Long.class.isAssignableFrom(left.getClass()) || Long.class.isAssignableFrom(right.getClass())) {
            switch (comparator) {
                case GREATER_THAN:
                    retValue = left.longValue() > right.longValue();
                    break;
                case LESS_THAN:
                    retValue = left.longValue() < right.longValue();
                    break;
                case GREATER_THAN_OR_EQUAL:
                    retValue = left.longValue() >= right.longValue();
                    break;
                case LESS_THAN_OR_EQUAL:
                    retValue = left.longValue() <= right.longValue();
                    break;
                case NOT_EQUALS:
                    retValue = left.longValue() != right.longValue();
                    break;
                case EQUALS:
                    retValue = left.longValue() == right.longValue();
                    break;
                default:
                    throw new MissingConfigException("can not handle");
            }
        }
        return retValue;
    }

    private static boolean doValueCompare(SqlSegment segment, SqlNode node, Object leftValue, Object rightValue) {
        boolean fit = false;
        switch (node.getKind()) {
            case EQUALS:
                if (!ObjectUtils.isEmpty(leftValue) && !ObjectUtils.isEmpty(rightValue)) {
                    fit = leftValue.equals(rightValue);
                }
                break;
            case NOT_EQUALS:
                if (!ObjectUtils.isEmpty(leftValue) && !ObjectUtils.isEmpty(rightValue)) {
                    fit = !leftValue.equals(rightValue);
                }
                break;
            case IN:
            case NOT_IN:
                String columnName = leftValue.toString();
                if (segment.getInPartMap().containsKey(columnName) && segment.getInPartMap().containsKey(rightValue.toString())) {
                    fit = SqlKind.IN.equals(node.getKind());
                }
                break;
            case NOT:
                fit = doValueCompare(segment, ((SqlBasicCall) node).getOperandList().get(1), leftValue, rightValue);
                break;
            default:
                throw new OperationNotSupportException("can not handle this opertator " + node.getKind());

        }
        return fit;
    }

    private static void getValueBySide(Calculator calculator, SqlNode nodes, boolean leftTag) {
        if (SqlBasicCall.class.isAssignableFrom(nodes.getClass())) {
            if (calculator.getSegment().getWherePartsMap().containsKey(nodes.toString())) {
                Queue<String> queue = calculator.getSegment().getWherePartsMap().get(nodes.toString()).getPolandQueue();
                if (leftTag) {
                    calculator.setLeftValue(PolandNotationUtil.computeResult(queue, calculator.getInputRecord()));
                } else {
                    calculator.setRightValue(PolandNotationUtil.computeResult(queue, calculator.getInputRecord()));
                }
            } else {
                throw new ConfigurationIncorrectException("");
            }
        } else if (SqlKind.LITERAL.equals(nodes.getKind())) {
            if (leftTag) {
                if (SqlCharStringLiteral.class.isAssignableFrom(nodes.getClass())) {
                    calculator.setLeftValue(calculator.getStringLiteralMap().computeIfAbsent(nodes.toString(), k -> nodes.toString().replace("'", "")));
                } else {
                    calculator.setRightValue(((SqlLiteral) nodes).getValue());
                }
            } else {
                if (SqlCharStringLiteral.class.isAssignableFrom(nodes.getClass())) {
                    calculator.setRightValue(calculator.getStringLiteralMap().computeIfAbsent(nodes.toString(), k -> nodes.toString().replace("'", "")));
                } else {
                    calculator.setRightValue(((SqlLiteral) nodes).getValue());
                }
            }
        } else if (SqlIdentifier.class.isAssignableFrom(nodes.getClass()) && calculator.getInputRecord().containsKey(nodes.toString())) {
            if (leftTag) {
                calculator.setLeftValue(calculator.getInputRecord().get(nodes.toString()));
            } else {
                calculator.setRightValue(calculator.getInputRecord().get(nodes.toString()));
            }
        }
    }
}
