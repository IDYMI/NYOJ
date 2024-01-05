# NYIST ACM OJ (NYOJ)

![logo](./hoj-vue/src/assets/nyoj-logo.png)

[![Java](https://img.shields.io/badge/Java-1.8-informational)](http://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.6.RELEASE-success)](https://spring.io/projects/spring-boot)
[![SpringCloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2.2.1.RELEASE-success)](https://spring.io/projects/spring-cloud-alibaba)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.19-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-5.0.9-red)](https://redis.io/)
[![Nacos](https://img.shields.io/badge/Nacos-1.4.2-%23267DF7)](https://github.com/alibaba/nacos)
[![Vue](https://img.shields.io/badge/Vue-2.6.11-success)](https://cn.vuejs.org/)
[![Github Star](https://img.shields.io/github/stars/HimitZH/HOJ?style=social)](https://github.com/HimitZH/HOJ)
[![Gitee Star](https://gitee.com/himitzh0730/hoj/badge/star.svg)](https://gitee.com/himitzh0730/hoj)
[![QQ Group 598587305](https://img.shields.io/badge/QQ%20Group-598587305-blue)](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi)


## 一、总概

- 基于Vue和Spring Boot、Spring Cloud Alibaba构建的前后端分离，分布式架构的评测系统
- **支持多种评测语言：C、C++、C#、Python、PyPy、Go、Java、JavaScript、PHP、Ruby、Rust**
- **支持HDU、POJ、Codeforces（包括GYM）、AtCoder、SPOJ、SCPC的Remote Judge评测**
- **支持移动端、PC端浏览，拥有讨论区与站内消息系统**
- **支持私有训练、公开训练（题单）和团队功能**
- **完善的评测功能：普通测评、特殊测评、交互测评、在线自测、子任务分组评测、文件IO**
- **完善的比赛功能：打星队伍、关注队伍、外榜、滚榜**
- **完善的比赛体验（赛前报名信息，赛后查重和结算）**


## 二、上线&更新日记

| 时间         | 内容                                       | 更新者         |
| ---------- | ---------------------------------------- | ----------- |
|  |      上线日记                       |    |
| 2020-10-26 | 正式开发                                     | Himit_ZH    |
| 2021-04-10 | 首次上线测试                                   | Himit_ZH    |
| 2021-04-15 | 判题调度2.0解决并发问题                            | Himit_ZH    |
| 2021-04-16 | 重构解耦JudgeServer判题逻辑，添加部署文档               | Himit_ZH    |
| 2021-04-19 | 加入rsync实现评测数据同步，修复一些已知的BUG               | Himit_ZH    |
| 2021-04-24 | 加入题目模板，修改页面页脚                            | Himit_ZH    |
| 2021-05-02 | 修复比赛后管理员重判题目导致排行榜失效的问题                   | Himit_ZH    |
| 2021-05-09 | 添加公共讨论区，题目讨论区，比赛评论                       | Himit_ZH    |
| 2021-05-12 | 添加评论及回复删除，讨论举报，调整显示时间。                   | Himit_ZH    |
| 2021-05-16 | 完善权限控制，讨论管理员管理，讨论删除与编辑更新。                | Himit_ZH    |
| 2021-05-22 | 更新docker-compose一键部署，修正部分bug             | Himit_ZH    |
| 2021-05-24 | 判题调度乐观锁改为悲观锁                             | Himit_ZH    |
| 2021-05-28 | 增加导入导出题目，增加用户页面的最近登录，开发正式结束，进入维护摸鱼       | Himit_ZH    |
| 2021-06-02 | 大更新，完善补充前端页面，修正判题等待超时时间，修补一系列bug         | Himit_ZH    |
| 2021-06-07 | 修正特殊判题，增加前台i18n                          | Himit_ZH    |
| 2021-06-08 | 添加后台i18n,路由懒加载                           | Himit_ZH    |
| 2021-06-12 | 完善比赛赛制，具体请看在线文档                          | Himit_ZH    |
| 2021-06-14 | 完善后台管理员权限控制，恢复CF的vjudge判题                | Himit_ZH    |
| 2021-06-25 | 丰富前端操作，增加POJ的vjudge判题                    | Himit_ZH    |
| 2021-08-14 | 增加spj对使用testlib的支持                       | Himit_ZH    |
| 2021-09-21 | 增加比赛打印功能、账号限制功能                          | Himit_ZH    |
| 2021-10-05 | 增加站内消息系统——评论、回复、点赞、系统通知的消息，优化前端。         | Himit_ZH    |
| 2021-10-06 | 美化比赛排行榜，增加对FPS题目导入的支持                    | Himit_ZH    |
| 2021-12-09 | 美化比赛排行榜，增加外榜、打星队伍、关注队伍的支持                | Himit_ZH    |
| 2022-01-01 | 增加公开训练和公开训练（题单）                          | Himit_ZH    |
| 2022-01-04 | 增加交互判题、重构judgeserver的三种判题模式（普通、特殊、交互）    | Himit_ZH    |
| 2022-01-29 | 重构remote judge，增加AtCoder、SPOJ的支持         | Himit_ZH    |
| 2022-02-19 | 修改首页前端布局和题目列表页                           | Himit_ZH    |
| 2022-02-25 | 支持PyPy2、PyPy3、JavaScript V8、JavaScript Node、PHP | Himit_ZH    |
| 2022-03-12 | 后端接口全部重构，赛外榜单增加缓存                        | Himit_ZH    |
| 2022-03-28 | 合并冷蕴提交的团队功能                              | Himit_ZH、冷蕴 |
| 2022-04-01 | 正式上线团队功能                                 | Himit_ZH、冷蕴 |
| 2022-05-29 | 增加在线调试、个人主页提交热力图                         | Himit_ZH    |
| 2022-08-06 | 增加题目标签的分类管理（二级标签）                        | Himit_ZH    |
| 2022-08-21 | 增加人工评测、取消评测                              | Himit_ZH    |
| 2022-08-30 | 增加OI题目的subtask、ACM题目的'遇错止评'模式            | Himit_ZH    |
| 2022-10-04 | 增加比赛奖项配置，增加ACM赛制的滚榜                      | Himit_ZH    |
| 2022-11-14 | 增加题目详情页专注模式，优化首页布局                       | Himit_ZH    |
| 2023-05-01 | 增加题目评测支持文件IO                             | Himit_ZH    |
| 2023-06-11 | 增加允许比赛结束后提交                              | Himit_ZH    |
| 2023-06-27 | 支持Ruby、Rust                              | Himit_ZH    |
|  |             更新日记                |    |
| 2023-08-20 | 增加新生排行榜                            | IDYMI    |
| 2023-08-21 | 增加用户偏好设置                            | IDYMI    |
| 2023-09-09 | 更新NYOJ UI                            | IDYMI    |
| 2023-10-06 | 增加点击榜单跳转                           | IDYMI    |
| 2023-10-06 | 增加首页轮播图跳转                           | IDYMI    |
| 2023-10-18 | 增加同步赛                          | IDYMI    |
| 2023-10-24 | 增加 SCPC 远程评测                          | IDYMI    |
| 2023-11-06 | 增加系列比赛排行榜                           | IDYMI    |
| 2023-11-26 | 增加正式赛                            | IDYMI    |
| 2023-11-27 | 增加暗色模式                          | IDYMI    |
| 2023-12-29 | 增加 Moss 查重                            | IDYMI    |
| 2024-01-02 | 增加赛供文件                            | IDYMI    |

## 三、新功能部分截图

# NYOJ新UI更新

## 更新内容

1.  全部界面宽度进行跳转，并添加 南阳理工 ACM 专属 LOGO

    ![image.png](https://nyoj.online/api/public/img/7c47edf042104fa294b3cfab959842f3.png)

2.  轮播图点击跳转对应链接，并显示文字信息

    ![image.png](https://nyoj.online/api/public/img/b8ce0b986bb743749c5fcd7e2a41b8b7.png)

3.  添加用户首页的比赛排行榜变化

    显示有排名的比赛排行榜变化图

    ![image.png](https://nyoj.online/api/public/img/2b525706182d4e3f86e4e231d068365f.png)

4.  添加题目时间轴对应时间段榜单跳转

    点击或者拖拽时间轴跳转到对应时间段榜单

    ![image.png](https://nyoj.online/api/public/img/7bcc5bc5e2914852bd00e38355939d16.png)

5.  添加个人偏好设置

    可修改界面语言，默认公共题库代码语言，默认提交代码语言，默认编译器字体大小，默认代码模板

    ![image.png](https://nyoj.online/api/public/img/7f60831c60274b54bdb354ca2cdf327e.png)

6.  规范题目ID

    现将 NYOJ 对应题目ID全部改为数字展示ID


2023年10月06日10点

# 添加 SCPC 远程评测

## 更新内容

1.  SCPC 远程评测

    SCPC是西南科技大学 计科 ACM 工作室，机缘巧合下决定共同训练，打造校方 ACM 品牌效应。

    下方为 SCPC 的远程评测题目，有需要欢迎到其 OJ 上做题

    ![image.png](https://nyoj.online/api/public/img/340298eda27d43e184aec927d293cd4a.png)

    ![image.png](https://nyoj.online/api/public/img/a010380715384021a2d876aabcb8b412.png)

2023年10月28日12点

# 添加系列比赛总榜单

## 更新内容

1.  添加系列比赛总榜单


    端口为 `acm-rank-static/`, 后加比赛的cids，用 `+` 号隔开

    eg: https://nyoj.online/acm-rank-static/1208+1211+1210 对应为23年新生赛前三场比赛

    注：

    1\. 该功能只能登录后使用

    2\. 查询比赛任意一场比赛没有注册将无法查询

    ![image.png](https://nyoj.online/api/public/img/e7183c153dcf4ea4bbe91132a4d9d56e.png)

2023年11月06日3点

# 添加正式赛

## 更新内容

1.  添加正式赛


    用户需要报名填写竞赛信息参赛，用户可邀请其他用户组成自己的队伍

    注：

    1\. 请用户前往我的设置中的竞赛设置中添加竞赛信息

    ![image.png](https://nyoj.online/api/public/img/af18b6cacf2a49dcaed0c78f8692fef3.png)

    2\. 进入正式比赛，发送审核信息

    ![image.png](https://nyoj.online/api/public/img/7770732ca71e4af186608fa351e31fad.png)

    3\. 等待审核通过进入比赛

    ![image.png](https://nyoj.online/api/public/img/325e2a1098ca48c3b5358e3c576228e0.png)

    4\. 多人比赛可邀请队友，队友同意后参加比赛的账号都可进入比赛（人员变化会重新审核！）

    ![image.png](https://nyoj.online/api/public/img/c8482183d8f643a293931920ce4b8b5c.png)

    5\. 队友同意，ACM启动！

    ![image.png](https://nyoj.online/api/public/img/4fecfa64630a48e49dd20fe5c9d2bf14.png)

2023年11月26日2点

# 添加暗色模式

## 更新内容

1.  添加暗色模式


    程序员护眼

    ![image.png](https://nyoj.online/api/public/img/a847d33b5ebf495aa753bac46ffd76a5.png)

    同时偏好设置中可设置

    ![image.png](https://nyoj.online/api/public/img/8cfc070949d94eaeaa44eb1175d143d6.png)

2023年11月27日19点

# 添加赛供文件

## 更新内容

1.  添加赛供文件


    ![image.png](https://nyoj.online/api/public/img/096fae5fce1b4369ab7fccb8cb737e6f.png)

2024年1月2日19点
