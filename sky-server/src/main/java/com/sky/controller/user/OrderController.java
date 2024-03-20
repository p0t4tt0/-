package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单接口")
@Slf4j

public class OrderController {

    @Autowired
    private OrderService orderService;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO)
    {
        log.info("用户下单 ：{}",ordersSubmitDTO);

       OrderSubmitVO orderSubmitVO= orderService.submitOrder(ordersSubmitDTO);
       return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     * @param
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(int page,int pageSize,Integer status)
    {
        log.info("历史订单查询 ：{},{},{}",page,pageSize,status);
        PageResult pageResult=orderService.userPageQuery(page,pageSize,status);
        return Result.success(pageResult);
    }


    /**
     * 订单详情查询
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id)
    {

        log.info("查询订单详情 ：{}",id);
        OrderVO orderVO=orderService.details(id);
       return Result.success(orderVO);


    }


    /**
     * 用户订单取消
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id)
    {
        log.info("用户取消订单：{}",id);
        orderService.cancelById(id);
        return Result.success();
    }


    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id)
    {

        orderService.repetition(id);

        return Result.success();

    }

}
