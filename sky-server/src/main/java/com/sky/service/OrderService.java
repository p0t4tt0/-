package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);


    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 各状态订单数量统计
     * @return
     */
    OrderStatisticsVO getSatistics();

    /**
     * 订单详细信息
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 接单
     * @param id
     */
    void confirm(Long id);

    /**
     * 拒单
     * @param id
     */
    void reject(OrdersRejectionDTO id);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 订单派送
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 用户历史订单
     * @param
     * @return
     */
    PageResult userPageQuery(int page,int pageSize,Integer status);

    /**
     * 用户取消订单
     * @param id
     */
    void cancelById(Long id);

    /**
     * 用户再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 用户催单
     * @param id
     */
    void reminder(Long id);
}
