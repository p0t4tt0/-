package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavourMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
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

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 菜品批量删除
     * @param ids
     */

    @Transactional
    public void deleteBatch(List<Long> ids) {

        //判断是否能删除：起售中？
        for (Long id : ids)
        {
            Dish dish=dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE)
            {
                //起售中
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

        }
        //是否能删除： 关联了套餐？

        List<Long> stIds=setmealDishMapper.getStmIdsByDishId(ids);

        if (stIds !=null&&stIds.size()>0)
        {
            //关联了套餐
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }


        //可以删除，删除菜品数据
        /*
        for (Long id :ids)
        {

            dishMapper.deleteById(id);

            //删除关联口味数据
            dishFlavourMapper.deleteByDishId(id);

        }*/

        //优化，批量删除菜品及口味

        dishMapper.deleteById(ids);

        dishFlavourMapper.deleteByDishIds(ids);

    }



    /**
     * 根据id查询菜品和对应口味
     * @param id
     * @return
     */

    public DishVO getByIdwithFlavor(Long id) {
        //根据id查菜品
        Dish dish=dishMapper.getById(id);

        //根据id查口味
        List<DishFlavor> dishFlavors=dishFlavourMapper.getByDishId(id);

        //分装为vo

        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }


    /**
     * 根据id修改菜品信息和口味
     * @param dishDTO
     */

    public void updateWithFlavor(DishDTO dishDTO) {

        //更新菜品表
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //口味先删除后插入

        dishFlavourMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> dishFlavors=dishDTO.getFlavors();

        if (dishFlavors!=null&&dishFlavors.size()>0)
        {

            dishFlavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
//批量插入
            dishFlavourMapper.insertBatch(dishFlavors);

        }

    }
}
