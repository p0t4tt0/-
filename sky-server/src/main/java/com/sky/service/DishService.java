package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {

    /**
     * 保存菜品和口味
     * @param dishDTO
     */
    public void saveDishAndFlavour(DishDTO dishDTO);
}
