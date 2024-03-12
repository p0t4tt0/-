package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户端店铺相关接口")
@Slf4j
public class ShopController {

    public static final String key="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     *获取店铺营业状态
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus()
    {
        Integer shop_status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("获取到营业状态为 ：{}",shop_status==1?"营业中":"打样中");


        return Result.success(shop_status);
    }
}
