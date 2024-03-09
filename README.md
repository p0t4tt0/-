黑马-苍穹外卖
springboot+ssm 练手项目
#DAY 1
##新增员工功能实现
针对业务需求，前端发送post请求，将新增员工信息已以json格式传递给后端，后端新增save方法，通过表现层捕获post请求，将数据封装到employeeDTO实体中，并传递到业务层方法，由业务层继续封装为employee实体并调用mapper数据层对数据库进行insert操作；
涉及到jwt请求头token校验问题；
对sql数据重复异常的处理，在handler中构建处理函数，捕获该异常并返回相应信息；
当前添加员工的id问题，由jwt解析生成id，使用threadlocal的组件类保存该id，然后由业务层获取
//TODO 密码校验需要MD5加密处理
