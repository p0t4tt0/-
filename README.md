# 黑马-苍穹外卖
# springboot+ssm 练手项目
## DAY1 
## 新增员工功能实现
针对业务需求，前端发送post请求，将新增员工信息已以json格式传递给后端，后端新增save方法，通过表现层捕获post请求，将数据封装到employeeDTO实体中，并传递到业务层方法，由业务层继续封装为employee实体并调用mapper数据层对数据库进行insert操作；
涉及到jwt请求头token校验问题；
对sql数据重复异常的处理，在handler中构建处理函数，捕获该异常并返回相应信息；
当前添加员工的id问题，由jwt解析生成id，使用threadlocal的组件类保存该id，然后由业务层获取
//TODO 密码校验需要MD5加密处理


## DAY 2
## 员工分页查询实现
在员工管理页面需要查询到当前员工表，前端将员工姓名、页码、页面条数推送给后端，后端接受后返回json数据给前端，其中data字段包括总条数total，及查询到的页面记录records
前端页面：![QQ截图20240309214132](https://github.com/p0t4tt0/-/assets/147514081/fbceecda-1f8e-487c-9a13-ced7adecab12)

### 具体实现：
表现层：
```
@GetMapping("/page")
@ApiOperation(value = "员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO)
    {
        log.info("员工分页查询："+employeePageQueryDTO);
        PageResult pageResult= employeeService.pageQuery(employeePageQueryDTO);


        return Result.success(pageResult);
    }
```
业务层：
```
/**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
```
业务层实现：

```
 /**
     * 分页查询
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO)
{
    //使用pagehelper插件简化分页查询,动态拼接sql,底层使用了threadlocal，存入页码值，动态拼接limit关键字用于分页查询
    PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());

    //page对象为数组
    Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);

    Long total=page.getTotal();
    List<Employee> records=page.getResult();
    return new PageResult(total,records);
}
```

mapper层：
```
/**
     * 分页查询--动态查询，通过xml映射文件
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
```
xml映射文件：
```
<mapper namespace="com.sky.mapper.EmployeeMapper">
<select id="pageQuery" resultType="com.sky.entity.Employee">
    select  * from employee
  <where>
    <if test="name!=null and name!=''">
        and name like concat('%',#{name},'%')
    </if>

  </where>
      order by create_time desc
</select>
</mapper>
```
#### 需要注意的是后端返回的data数据中，localtime格式的数据为数组形式，显示到前端是一串数字，不符合时间格式，需要进行处理，两种方式：
一、在实体类属性前加注解（需要挨个加，比较繁琐）
二、在配置类中添加消息转换器，统一将后端返回的数据转为json格式（推荐）
```
/**
     * 扩展spring mvc框架的 消息转换器，对后端传送的数据统一进行转换处理
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器....");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //为消息转换器设置一个对象转化器，将java对象转换为jason数据
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将自定义转换器添加进spring框架的转换器容器，并设置序号为0，优先使用
        converters.add(0,messageConverter);
    }
```
## DAY3
## 1.启用禁用员工实现

前端在员工列表中点击某员工的启用/禁用按钮，通过地址栏传参，将员工的status属性修改为0或1，其中员工id为query查询参数

后端接受参数post请求后，调用update修改员工属性status，返回前端成功消息

### 具体实现

controller：

```
 /**
     * 员工启用、禁用；
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用员工账号")

    public Result startOrStop(@PathVariable Integer status,Long id)
    {
        log.info("启用禁用员工账号参数：{}，{}",status,id);
        employeeService.startOrStop(status,id);
        return  Result.success();
    }
```

service:
```
/**
     * 员工启用禁用
     * @param status
     * @param id
     */

    void startOrStop(Integer status, Long id);
```
serviceImpl:
```
/**
     * 员工启用禁用
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {

        //update 动态传参
        Employee employee=new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.update(employee);

    }
```

mapper: 为便于以后更新员工数据，这里定义了一个统一的update方法，用于动态更新员工属性值

```
/**
     * 动态更新员工属性
     * @param employee
     */
    void update(Employee employee);
```
xml:
```
    <update id="update" parameterType="Employee">
update employee
<set>
    <if test="name != null">name=#{name},</if>
    <if test=" username != null ">username=#{username},</if>
    <if test=" password != null ">password=#{password},</if>
    <if test=" phone != null ">phone=#{phone},</if>
    <if test=" sex != null ">sex =#{sex},</if>
    <if test=" idNumber != null ">id_Number=#{idNumber},</if>
    <if test=" updateTime != null ">update_Time=#{updateTime},</if>
    <if test=" updateUser!= null ">update_User=#{updateUser},</if>
    <if test=" status != null ">status=#{status},</if>
</set>
    <where>id = #{id}</where>

    </update>
```

## 2.编辑员工功能实现

## 3.分类管理功能导入

## 4.公共字段自动填充

## 5.新增菜品功能实现

## DAY4

## 1.菜品分页查询
## 2.菜品删除
## 3.修改菜品信息
## 4.起售、停售菜品
## 5.新增套餐
## 6.套餐分页查询实现
## 7.套餐删除

## DAY5


## 1.套餐修改实现

## 2.套餐起售与停售

## 3.店铺营业状态设置功能实现及redis入门


## DAY6
## 1.微信小程序入门及微信登陆功能实现

## DAY7
## 1.微信商品浏览功能实现
## 2.缓存菜品功能实现以及菜品起售停售的补充修改

## DAY8
 ## 1.套餐缓存功能实现：Spring Cache框架

## DAY9
## 1.添加购物车功能实现
## 2.查看购物车功能实现
## 3.清空购物车功能实现
## 4.用户下单功能实现

## DAY10
## 1.订单支付功能代码导入（未实现）
## 2.商家端订单管理模块：

- 订单搜索
- 各个状态的订单数量统计
- 查询订单详情
- 接单
- 拒单
- 取消订单
- 派送订单
- 完成订单
## 3.用户端查询历史订单


## DAY11
## 1.用户查询订单详情
## 2.用户取消订单
## 3.用户再来一单


## DAY12
## 1.判断地址是否超出配送范围


## 2.定时处理订单状态
## 3.websocket实现用户催单和来单提醒

## DAY13 
##  1.营业额数据统计
## 2.用户数据统计
## 3.统计销量top10

## DAY14
## 1.工作台业务功能实现
