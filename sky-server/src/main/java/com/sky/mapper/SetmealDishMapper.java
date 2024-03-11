package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据dishID查询套餐id
     * @param dishIds
     * @return
     */

    List<Long> getStmIdsByDishId(List<Long> dishIds);


    /**
     * 向菜品套餐表批量插入数据
     * @param setmealDishes
     */

    void insertBatch(List<SetmealDish> setmealDishes);
}
