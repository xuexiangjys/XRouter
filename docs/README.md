# XRouter
[![xrouter][xrouter-svg]][xrouter]  [![api][apisvg]][api]

一个轻量级的Android路由框架，基于ARouter上进行改良，优化Fragment的使用，可结合XPage使用。

## 特征

> 由于是借鉴了ARouter，拥有ARouer所有特征，并在此基础上加入了Fragment的路由。

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

## 添加Gradle依赖

1.先在项目根目录的 build.gradle 的 repositories 添加:

```
allprojects {
     repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

2.再在项目根目录的 build.gradle 的 dependencies 添加xrouter-plugin插件：

```
buildscript {
    ···
    dependencies {
        ···
        classpath 'com.github.xuexiangjys.XRouter:xrouter-plugin:1.0.0'
    }
}
```

3.在主项目的 build.gradle 中增加依赖并引用xrouter-plugin插件

```
apply plugin: 'com.xuexiang.xrouter' //引用xrouter-plugin插件实现自动注册

dependencies {
    ···
    implementation 'com.github.xuexiangjys.XRouter:xrouter-runtime:1.0.0'
    annotationProcessor 'com.github.xuexiangjys.XRouter:xrouter-compiler:1.0.0'
}

```

4.进行moduleName注册

```
defaultConfig {
    ...
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = [ moduleName : project.getName() ]
        }
    }
}
```

5.在主项目的Application中初始化XRouter

```
private void initXRouter() {
    if (isDebug()) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
        XRouter.openLog();     // 打印日志
        XRouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
    }
    XRouter.init(this);
}

private boolean isDebug() {
    return BuildConfig.DEBUG;
}
```

### 在Library中使用XRouter的配置

1.进行moduleName注册

```
defaultConfig {
    ...
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = [ moduleName : project.getName() ]
        }
    }
}
```

2.在项目的 build.gradle 中增加XRouter依赖。

```
dependencies {
    ···
    implementation 'com.github.xuexiangjys.XRouter:xrouter-runtime:1.0.0'
    annotationProcessor 'com.github.xuexiangjys.XRouter:xrouter-compiler:1.0.0'
}
```

### 在Kotlin项目中使用XRouter的配置

1.引用kotlin-kapt插件

```
apply plugin: 'kotlin-kapt'
```

2.进行moduleName注册

```
kapt {
    arguments {
        arg("moduleName", project.getName())
    }
}
```

3.在项目的 build.gradle 中增加XRouter依赖。

```
dependencies {
    ···
    implementation 'com.github.xuexiangjys.XRouter:xrouter-runtime:1.0.0'
    kapt 'com.github.xuexiangjys.XRouter:xrouter-compiler:1.0.0'
}
```

## 代码混淆

```
-keep public class com.xuexiang.xrouter.routes.**{*;}
-keep class * implements com.xuexiang.xrouter.facade.template.ISyringe{*;}

# 如果使用了 byType 的方式获取 Service，需添加下面规则，保护接口
-keep interface * implements com.xuexiang.xrouter.facade.template.IProvider

# 如果使用了 单类注入，即不定义接口实现 IProvider，需添加下面规则，保护实现
-keep class * implements com.xuexiang.xrouter.facade.template.IProvider
```

## 特别感谢

https://github.com/alibaba/ARouter

## 联系方式

[![](https://img.shields.io/badge/点击一键加入QQ交流群-602082750-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=9922861ef85c19f1575aecea0e8680f60d9386080a97ed310c971ae074998887)

[xrouter-svg]: https://img.shields.io/badge/XRouter-v1.0.0-brightgreen.svg
[xrouter]: https://github.com/xuexiangjys/XRouter
[apisvg]: https://img.shields.io/badge/API-14+-brightgreen.svg
[api]: https://android-arsenal.com/api?level=14
