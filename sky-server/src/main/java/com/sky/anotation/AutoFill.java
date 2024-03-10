package com.sky.anotation;
//自定义注解，标识方法需要自动填充处理

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//指定注解位置
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
//数据库操作类型update insert
    OperationType value();
}
