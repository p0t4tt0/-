package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */

  @PostMapping
  @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO)
    {
        log.info("新增菜品 ：{}",dishDTO);
        dishService.saveDishAndFlavour(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分页查询")

    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO)
    {
         log.info("菜品分页查询 ：{}",dishPageQueryDTO);
         PageResult pageResult= dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     * @return
     */

    @DeleteMapping
    @ApiOperation(value = "菜品删除")
    public Result delete(@RequestParam List<Long> ids)
    {
        log.info("菜品批量删除 ：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }


    /**
     * 根据id查询菜品
     * @param id
     * @return
     */

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id)

    {

        DishVO dishVO = dishService.getByIdwithFlavor(id);

        return Result.success(dishVO);
    }


    @PutMapping
    @ApiOperation(value = "修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO)
    {
        log.info("修改菜品 ：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
    return Result.success();
    }

    /**
     * 菜品起售停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "起售停售菜品")

    public Result startOrEnd(@PathVariable Integer status,Long id)
    {

        log.info("起售停售菜品参数 ：{} {}",status,id);
        dishService.startOrEnd(status,id);
        return Result.success();
    }

}
