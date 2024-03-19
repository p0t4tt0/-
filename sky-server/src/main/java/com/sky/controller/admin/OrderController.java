package com.sky.controller.admin;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Api(tags = "管理员端订单相关接口")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;




    /**
     * 订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单查询")
    public Result<PageResult> conditionSearch( OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("订单分页查询 ：{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.pageQuery(ordersPageQueryDTO);


        return Result.success(pageResult);
    }


    /**
     * 各状态订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各状态订单数量统计")
    public Result<OrderStatisticsVO> statistics()
    {


        log.info("各状态订单数量统计...");
        OrderStatisticsVO orderStatisticsVO=orderService.getSatistics();
        return Result.success(orderStatisticsVO);
    }


    /**
     * 订单详细信息
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("订单详细信息")
    public Result<OrderVO> showDetail(@PathVariable Long id)
    {

        OrderVO orderVO=orderService.details(id);

        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO)
    {
        log.info("接单id：{}",ordersConfirmDTO);

        orderService.confirm(ordersConfirmDTO.getId());
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */

    @PutMapping("/rejection")
    @ApiOperation("商家拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO)
    {

        log.info("商家拒单：{}",ordersRejectionDTO);
        orderService.reject(ordersRejectionDTO);

        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO)
    {
        log.info("取消订单：{}",ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }


    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送")
    public Result delivery(@PathVariable Long id)
    {
        log.info("订单派送 ：{}",id);
        orderService.delivery(id);
        return Result.success();
    }


    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id)
    {
        log.info("订单完成");
        orderService.complete(id);
        return Result.success();
    }
}
