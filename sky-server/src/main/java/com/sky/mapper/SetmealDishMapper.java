package com.sky.mapper;

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

}
