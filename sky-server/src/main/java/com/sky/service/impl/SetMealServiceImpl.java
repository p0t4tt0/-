package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
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


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }


    /**
     * 套餐批量删除
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {

        //判断是都可以删除，是否起售中
        for (Long id :ids)
        {
            Setmeal setmeal=setmealMapper.getById(id);

            if(setmeal.getStatus()== StatusConstant.ENABLE)
            {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //删除套餐

        setmealMapper.deleteBatch(ids);

        //删除套餐关联菜品数据

        setmealDishMapper.deleteBysetmealIds(ids);

    }
}
