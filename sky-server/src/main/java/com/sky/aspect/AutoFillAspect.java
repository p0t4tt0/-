package com.sky.aspect;

import com.sky.anotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，用于公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点,那些类的哪些方法
     */
@Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.anotation.AutoFill)")
    public void autoFillPointCut()
    {
    }
//前置通知,公共字段赋值
@Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint)
    {
        log.info("开始进行公共字段的自动填充...");

        MethodSignature methodSignature= (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill=methodSignature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType=autoFill.value();//获取数据库操作类型

        //获取当前操作的参数
        Object[] args=joinPoint.getArgs();

        if(args==null||args.length==0)
        {
            return;
        }

        Object entity=args[0];

        //准备赋值数据

        LocalDateTime localDateTime=LocalDateTime.now();
        Long id= BaseContext.getCurrentId();

        //根据操作类型，对相应属性进行反射赋值

        if(operationType==OperationType.INSERT)
        {
            //四个
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCteateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                 Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                 Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                 setCreateTime.invoke(entity,localDateTime);
                 setCteateUser.invoke(entity,id);
                 setUpdateTime.invoke(entity,localDateTime);
                 setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else if(operationType==OperationType.UPDATE)
        {
            //两个
            try {

                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);


                setUpdateTime.invoke(entity,localDateTime);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }
}
