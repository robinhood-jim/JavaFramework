package com.robin.core.base.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.robin.core.base.util.Const;

public class MybatisUtils<T> {
    public static <T> LambdaQueryWrapper<T> getWrapper(SFunction<T,?> queryField, Const.OPERATOR operator,Class<T> clazz,Object... value){
        LambdaQueryWrapper<T> queryWrapper=new QueryWrapper<T>().lambda();
        if(operator.equals(Const.OPERATOR.EQ)){
            queryWrapper.eq(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.GE)){
            queryWrapper.ge(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.LE)){
            queryWrapper.ge(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.GT)){
            queryWrapper.gt(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.LT)){
            queryWrapper.gt(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.IN)){
            queryWrapper.in(queryField,value);
        }else if(operator.equals(Const.OPERATOR.NOTIN)){
            queryWrapper.notIn(queryField,value);
        }else if(operator.equals(Const.OPERATOR.NE)){
            queryWrapper.ne(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.NVL)){
            queryWrapper.isNotNull(queryField);
        }else if(operator.equals(Const.OPERATOR.NULL)){
            queryWrapper.isNull(queryField);
        }else if(operator.equals(Const.OPERATOR.BETWEEN)){
            queryWrapper.between(queryField,value[0],value[1]);
        }else if(operator.equals(Const.OPERATOR.NOTEXIST)){
            queryWrapper.notExists(value[0].toString());
        }
        return queryWrapper;
    }
}
