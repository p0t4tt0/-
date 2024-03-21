package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据penid查询用户信息
     * @param openid
     * @return
     */
    @Select("select  * from user where openid=#{openid}")
    User getByOpenId(String openid);

    /**
     * 插入新用户--需要返回主键值
     * @param user
     */

    void insert(User user);


    @Select("select * from user where id=#{id}")

    User getById(Long userId);


    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
