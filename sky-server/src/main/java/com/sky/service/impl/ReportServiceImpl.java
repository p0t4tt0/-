package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;


    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {


        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end))
        {
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverlist=new ArrayList<>();
        for(LocalDate localDate:dateList)
        {
            //查询当天营业额
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);


            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover= turnover==null?0.0:turnover;
            turnoverlist.add(turnover);
        }


        String dateString = StringUtils.join(dateList, ",");
        String turnoverString=StringUtils.join(turnoverlist,",");



       return TurnoverReportVO.builder().dateList(dateString).turnoverList(turnoverString).build();


    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList=new ArrayList<>();//每天日期

        dateList.add(begin);
        while (!begin.equals(end))
        {

            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList=new ArrayList<>();
        List<Integer> totalUserList=new ArrayList<>();
        for(LocalDate localDate:dateList)
        {
            LocalDateTime begintime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endtime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map=new HashMap();
            map.put("end",endtime);
            Integer totaluser = userMapper.countByMap(map);
            totalUserList.add(totaluser);

            map.put("begin",begintime);
            Integer newuser=userMapper.countByMap(map);

            newUserList.add(newuser);
        }







        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,",")).
                newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }
}
