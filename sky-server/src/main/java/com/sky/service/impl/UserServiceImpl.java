package com.sky.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static  final  String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";


    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;



    /**
     * 微信登陆
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {

        String openid = getOpenid(userLoginDTO.getCode());
        //判断是否为空
        if (openid==null)
        {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断是否为新用户

        User user = userMapper.getByOpenId(openid);
        if(user==null)
        {
            //注册

             user = User.builder().openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }




        return user;
    }


    private String getOpenid(String code)
    {
        //调用微信接口获取openid
        Map<String, String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");


        String s = HttpClientUtil.doGet(WX_LOGIN, map);

        //将字符串解析为json对象
        JSONObject jsonObject=JSONObject.parseObject(s);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
