# 2,4DAnalyser

## 项目简介

2022年秋季吉林大学《Android开发》课程设计 - **2,4D浓度检测App**。

[《Android开发》课程设计-2,4D浓度检测App-测试视频_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV1Zg411q7qP/?spm_id_from=333.999.0.0&vd_source=fcd3cb5437aab9fd75559e3cb04da3ed)



## 使用方法

1、在`backend`中的`sql/*.sql`文件进行DDL。

2、修改`backend`中配置文件中的数据库相关的配置。

3、启动`backend(Spring Boot)`服务。

4、通过`ipconfig`查询`WLAN`分配本机的`IPv4`地址。

5、修改前端中`utils.Constant.BASE_URL`为上述查询到的`IP:8080`

6、如若使用真机测试，请确保真机连接服务端所连接的`WIFI`。



## 技术栈

### 前端

- 语言
  - **Java JDK-1.8**
  - **API 32 min 23**

- 依赖

  - **UCrop** 截图工具

  - **OkHttp** 网络请求

  - **Lombok** 开发工具

  - **Hutool** 开发工具

### 后端

- 框架
  - **Spring Boot**
- 数据库
  - **MySQL**
- 依赖
  - **Lombok** 开发工具
  - **Hutool** 开发工具
  - **SpringBoot-JDBC** 数据库连接驱动
  - **Mysql-Connector-J MySQL **连接驱动
  - **MyBatisPlus** JPA支持



## 功能

- 主要
  - 通过一组用户自定义数据来预测其他图片中所表现的浓度。

- 详情
  - 用户登录/注册
  - 模型CRUD
  - 模型数据CRUD
  - 模型拟合（线性回归方程）
  - 结果分享



## 实现思路

#### 账户

最简单基本的功能，后端数据库建立用户表，字段有`id`、`username`、`password`，登录相当于用`username`和`password`查询。登陆成功后在响应体中加入`id`给予前端保存方便后续申请资源。

如若需要更加安全可以在后端通过依赖`SpringSecurity`与`JWT-Token`来实现，返回前端的`id`则用其生成的`JWT-Token`来取代。



#### 模型 + 模型数据 CRUD

申请资源比如申请属于自己的模型，在模型加入一个`user_id`外键指向`user`表的`id`，查询时通过`user_id`查询即可。

对属于同一个模型的数据也同理，建一个表`model_photo`，加入一个`model_id`外键指向`model`表的`id`，查询时通过`model_id`查询即可。



#### 图像截图

通过调用 打开相册/相机 获取图片资源，再将图片通过**OnActivityResult**转发给**UCrop**，**UCrop**进行截图再通过**OnActivityResult**转发处理即可。



#### 预测模型建立

只需要通过模型所给的数据以及RGB转灰度计算公式获取**浓度/灰度**散点，再套用回归直线方程即可计算出相关参数。最后通过Android的**Canvas**库来渲染模型。