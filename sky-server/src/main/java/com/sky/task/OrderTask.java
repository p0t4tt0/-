package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理订单超时
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    //@Scheduled(cron = "1/5 * * * * ?")//测试
    public void process()
    {

        log.info("每分钟执行一次 ：{}",new Date());
        //查询超时订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (ordersList!=null&&ordersList.size()>0)
        {
            for (Orders orders :ordersList)
            {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }


    /**
     * 处理派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点
    //@Scheduled(cron = "0/5 * * * * ?")//测试
    public void processDeliveryOrder()
    {

        log.info("处理派送中订单 ：{}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusMinutes(60));//上一工作日处于派送中订单


        if (ordersList!=null&&ordersList.size()>0)
        {
            for (Orders orders :ordersList)
            {
                orders.setStatus(Orders.COMPLETED);


                orderMapper.update(orders);
            }
        }


    }
}
