package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "用户端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //构造redis key
        String key="dish_"+categoryId;

        //查询redis缓存中是否有要查询的数据

        List<DishVO> s = (List<DishVO>) redisTemplate.opsForValue().get(key);

        if (s!=null&&s.size()>0) {


            //如果有，则直接取出返回

            return Result.success(s);
        }

        //若无，则从数据库中查找，并存入redis

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        s = dishService.listWithFlavor(dish);


        redisTemplate.opsForValue().set(key,s);

        return Result.success(s);
    }

}
