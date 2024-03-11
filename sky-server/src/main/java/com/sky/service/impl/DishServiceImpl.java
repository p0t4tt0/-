package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavourMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavourMapper dishFlavourMapper;

    /**
     * 保存菜品和口味
     * @param dishDTO
     */
    @Transactional//事物注解，保证操作原子性，因为操作涉及了菜品和口味两张表，要再启动类中开启注解事务管理
    public void saveDishAndFlavour(DishDTO dishDTO) {
        //像菜品表插入1条数据
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);

        //获取insert语句生成的主键值

        Long dishId=dish.getId();

        //向口味表插入n条

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors!=null&&flavors.size()>0)
        {

            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
//批量插入
            dishFlavourMapper.insertBatch(flavors);

        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
