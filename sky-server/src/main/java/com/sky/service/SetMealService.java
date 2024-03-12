package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;


public interface SetMealService {

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveSetmealwithDish(SetmealDTO setmealDTO);


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /**
     * 套餐批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐
     * @param setmealId
     * @return
     */
    SetmealVO getById(Long setmealId);


    /**
     * 更新套餐及关联菜品
     * @param setmealDTO
     */
    void updateWithDishes(SetmealDTO setmealDTO);


    /**
     * 起售停售套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
