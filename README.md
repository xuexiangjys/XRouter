# XRouter
[![xrouter][xrouter-svg]][xrouter]  [![api][apisvg]][api]

一个轻量级的Android路由框架，基于ARouter上进行改良，优化Fragment的使用，可结合XPage使用。

## 关于我

[![github](https://img.shields.io/badge/GitHub-xuexiangjys-blue.svg)](https://github.com/xuexiangjys)   [![csdn](https://img.shields.io/badge/CSDN-xuexiangjys-green.svg)](http://blog.csdn.net/xuexiangjys)

## 特征

> 由于是借鉴了ARouter，拥有ARouer所有属性，在此基础上加入了Fragment的路由。

* 支持直接解析标准URL进行跳转，并自动注入参数到目标页面中
* 支持多模块工程使用
* 支持添加多个拦截器，自定义拦截顺序
* 支持依赖注入，可单独作为依赖注入框架使用
* 支持InstantRun
* 支持MultiDex(Google方案)
* 映射关系按组分类、多级管理，按需初始化
* 支持用户指定全局降级与局部降级策略
* 页面、拦截器、服务等组件均自动注册到框架
* 支持多种方式配置转场动画
* 支持获取Fragment
* 支持在Fragment中使用Fragment的startActivityForResult启动Activity。
* 完全支持Kotlin以及混编
* 支持第三方 App 加固(使用 xrouter-plugin 实现自动注册)

## 典型应用

* 可结合[XPage--Fragment页面框架](https://github.com/xuexiangjys/XPage)，实现应用的全景路由。
* 从外部URL映射到内部页面，以及参数传递与自动解析。
* 跨模块页面跳转，模块间解耦。
* 拦截跳转过程，处理登陆、埋点等逻辑。
* 跨模块API调用，通过控制反转来做组件解耦，实现组件化。

## 1、演示（请star支持）

![][demo-gif]

### Demo下载

[![downloads][download-svg]][download-url]

![][download-img]















## 特别感谢

https://github.com/alibaba/ARouter

## 联系方式

[![](https://img.shields.io/badge/点击一键加入QQ交流群-602082750-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=9922861ef85c19f1575aecea0e8680f60d9386080a97ed310c971ae074998887)

![qq交流群](https://img-blog.csdn.net/20180514131732423?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3h1ZXhpYW5nanlz/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 向我提问

打开微信扫一扫，向我提问：

![这里写图片描述](https://img-blog.csdn.net/20180511001512918?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3h1ZXhpYW5nanlz/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

[xrouter-svg]: https://img.shields.io/badge/XRouter-v1.0.0-brightgreen.svg
[xrouter]: https://github.com/xuexiangjys/XRouter
[apisvg]: https://img.shields.io/badge/API-14+-brightgreen.svg
[api]: https://android-arsenal.com/api?level=14

[demo-gif]: https://github.com/xuexiangjys/XRouter/blob/master/img/xrouter.gif
[download-svg]: https://img.shields.io/badge/downloads-1.8M-blue.svg
[download-url]: https://github.com/xuexiangjys/XRouter/blob/master/apk/xrouter_demo.apk?raw=true
[download-img]: https://github.com/xuexiangjys/XRouter/blob/master/img/download.png