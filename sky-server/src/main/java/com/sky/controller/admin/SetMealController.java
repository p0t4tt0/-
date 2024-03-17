package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理接口")
public class SetMealController {


    @Autowired
    private SetMealService setMealService;


    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO)
    {

        setMealService.saveSetmealwithDish(setmealDTO);
        return Result.success();
    }


    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        log.info("套餐分页查询 ：{}",setmealPageQueryDTO);
        PageResult pageResult=setMealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);

    }

@DeleteMapping
@ApiOperation("套餐批量删除")
@CacheEvict(cacheNames = "setmealCache",allEntries = true)
public Result delete(@RequestParam List<Long> ids)
{

    log.info("套餐批量删除 ：{}",ids);
    setMealService.deleteBatch(ids);
    return Result.success();

}


    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
@ApiOperation("根据id查询套餐")
public Result<SetmealVO> getById(@PathVariable Long id)
{
    SetmealVO setmealVO=setMealService.getById(id);

    return Result.success(setmealVO);

}

    /**
     * 修改套餐信息
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐信息")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO)
   {

       log.info("修改菜品信息 ：{}",setmealDTO);
       setMealService.updateWithDishes(setmealDTO);


    return Result.success();

}


   @PostMapping("/status/{status}")
   @ApiOperation("套餐起售停售接口")
   @CacheEvict(cacheNames = "setmealCache",allEntries = true)
   public Result startOrStop(@PathVariable Integer status,Long id)
   {
       log.info("起售停售套餐参数 ：{} {}",status,id);
       setMealService.startOrStop(status,id);
       return Result.success();
   }


}
