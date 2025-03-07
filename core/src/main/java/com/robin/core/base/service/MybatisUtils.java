package com.robin.core.base.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.robin.core.base.util.Const;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class MybatisUtils {
    public static <T> QueryWrapper<T> getWrapper(SFunction<T,?> queryField, Const.OPERATOR operator,Object... value){
        Assert.isTrue(!ObjectUtils.isEmpty(value),"value must not be null!");
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        LambdaQueryWrapper<T> queryWrapper=wrapper.lambda();
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
            Assert.isTrue(value.length==2,"must have two parameters");
            queryWrapper.between(queryField,value[0],value[1]);
        }else if(operator.equals(Const.OPERATOR.NBT)){
            Assert.isTrue(value.length==2,"must have two parameters");
            queryWrapper.notBetween(queryField,value[0],value[1]);
        }
        else if(operator.equals(Const.OPERATOR.NOTEXIST)){
            queryWrapper.notExists(value[0].toString());
        }else if(operator.equals(Const.OPERATOR.LLIKE)){
            queryWrapper.likeLeft(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.RLIKE)){
            queryWrapper.likeRight(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.LIKE)){
            queryWrapper.like(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.NOTLIKE)){
            queryWrapper.notLike(queryField,value[0]);
        }

        return wrapper;
    }
}
