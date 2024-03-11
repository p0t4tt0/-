package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {


    @Autowired
    private SetmealMapper setmealMapper;


    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveSetmealwithDish(SetmealDTO setmealDTO) {

        //向套餐表插入数据

        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.insert(setmeal);

        //向套餐菜品关联表插入数据

        //获取主键值
        Long setmealId=setmeal.getId();

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();

        if(setmealDishes !=null&&setmealDishes.size()>0)
        {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });

            setmealDishMapper.insertBatch(setmealDishes);


        }





    }
}
