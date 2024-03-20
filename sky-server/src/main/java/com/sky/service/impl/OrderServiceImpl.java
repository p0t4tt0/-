package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;//订单表


    @Autowired
    private OrderDetailMapper orderDetailMapper;//订单明细


    @Autowired
    private AddressBookMapper addressBookMapper;


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;


    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional //两张表，事务处理
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常（地址簿、购物车为空）

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0)
        {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//时间戳
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(currentId);

        orderMapper.insert(orders);



        //向订单明细表插入n条数据
List<OrderDetail> orderDetailList=new ArrayList<>();
        for(ShoppingCart cart:list)
        {
            OrderDetail orderDetail = new OrderDetail();//订单明细
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);


        //清空购物车
        shoppingCartMapper.deleteByUserId(currentId);


        //封装VO

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());

        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList=getOrderVOList(page);
        long total = page.getTotal();
        return new PageResult(total,orderVOList);
    }
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
    /**
     * 各状态订单数据统计
     * @return
     */
    public OrderStatisticsVO getSatistics() {

        Integer tobeconfirmed=orderMapper.count(2);
        Integer confirmed=orderMapper.count(3);
        Integer deliveryInProgress=orderMapper.count(4);

        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(tobeconfirmed);

        return orderStatisticsVO;
    }

    /**
     * 订单详细信息
     * @param id
     * @return
     */
    public OrderVO details(Long id) {
       List<OrderDetail> detailList= orderDetailMapper.getById(id);
       Orders orders=orderMapper.getById(id);
       OrderVO orderVO=new OrderVO();
       BeanUtils.copyProperties(orders,orderVO);
       orderVO.setOrderDetailList(detailList);
       orderVO.setOrderDishes(getOrderDishesStr(orders));

        return orderVO;
    }


    /**
     * 接单
     * @param id
     */
    public void confirm(Long id) {
        Orders orders=new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CONFIRMED);


        orderMapper.update(orders);

    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersdb=orderMapper.getById(ordersRejectionDTO.getId());
        if (ordersdb==null||ordersdb.getStatus()!= Orders.TO_BE_CONFIRMED)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(ordersRejectionDTO.getId());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) {



        Orders orders=new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);

    }


    /**
     * 订单派送
     * @param id
     */
    public void delivery(Long id) {

        Orders ordersdb=orderMapper.getById(id);
        if(ordersdb==null||ordersdb.getStatus()!=3)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);


        orderMapper.update(orders);

    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {

        Orders ordersdb=orderMapper.getById(id);
        if(ordersdb==null||ordersdb.getStatus()!=4)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);


        orderMapper.update(orders);

    }


    /**
     * 用户历史订单
     * @param
     * @return
     */
    public PageResult userPageQuery(int page,int pageSize,Integer status) {
        PageHelper.startPage(page,pageSize);
        Long userId=BaseContext.getCurrentId();
        OrdersPageQueryDTO ordersPageQueryDTO=new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(userId);

            ordersPageQueryDTO.setStatus(status);

        Page<Orders> ordersPage=orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOS=new ArrayList<>();
        if(ordersPage.getTotal()>0&&ordersPage!=null) {
            for (Orders orders: ordersPage)
            {
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                Long id=orders.getId();
                List<OrderDetail> orderDetailList=orderDetailMapper.getById(id);
                orderVO.setOrderDetailList(orderDetailList);

                orderVOS.add(orderVO);
            }
        }
        return new PageResult(ordersPage.getTotal(),orderVOS);
    }


    /**
     * 用户订单取消
     * @param id
     */
    public void cancelById(Long id) {
        Orders orders=orderMapper.getById(id);

        if(orders==null)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(orders.getStatus()>2)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders1=new Orders();
        orders1.setId(id);
        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED))
        {
            //退款
        }

        orders1.setStatus(Orders.CANCELLED);
        orders1.setCancelTime(LocalDateTime.now());
        orders1.setCancelReason("用户取消");
        orderMapper.update(orders1);

    }


    /**
     * 再来一单
     * @param id
     */
    public void repetition(Long id) {

        List<OrderDetail> orderDetailList=orderDetailMapper.getById(id);

        List<ShoppingCart> shoppingCarts=new ArrayList<>();
        Long userId=BaseContext.getCurrentId();

        for (OrderDetail od:orderDetailList)
        {
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(od,shoppingCart);

            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCarts.add(shoppingCart);
        }

        shoppingCartMapper.insertBatch(shoppingCarts);

    }
}
