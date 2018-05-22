# XRouter
[![xrouter][xrouter-svg]][xrouter]  [![api][apisvg]][api]

一个轻量级的Android路由框架，基于ARouter上进行改良，优化Fragment的使用，可结合XPage使用。

## 关于我

[![github](https://img.shields.io/badge/GitHub-xuexiangjys-blue.svg)](https://github.com/xuexiangjys)   [![csdn](https://img.shields.io/badge/CSDN-xuexiangjys-green.svg)](http://blog.csdn.net/xuexiangjys)

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

## 1、演示（请star支持）

![][demo-gif]

### Demo下载

[![downloads][download-svg]][download-url]

![][download-img]

## 2、如何使用

目前支持主流开发工具AndroidStudio的使用，直接配置build.gradle，增加依赖即可.

### 2.1、Android Studio导入方法，添加Gradle依赖

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

### 2.2 在Library中使用XRouter的配置

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

### 2.3 在Kotlin项目中使用XRouter的配置

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

## 3、XRouter使用

### 3.1、路由注册

* 在支持路由的页面上添加`@Router`注解(必选)。
* 这里的路径需要注意的是`至少需要有两级`，格式：/xx/xx。

```
@Router(path = "/test/activity")
public class YourActivity extend Activity {
    ...
}
```

### 3.2、路由导航

1.简单的应用内跳转

```
XRouter.getInstance().build("/test/activity2").navigation();
```

2.跳转ForResult

```
XRouter.getInstance()
        .build("/test/activity2")
        .navigation(this, 666);
```

3.携带参数进行跳转

```
XRouter.getInstance().build("/test/activity1")
        .withString("name", "老王")
        .withInt("age", 18)
        .withBoolean("boy", true)
        .withLong("high", 180)
        .withString("url", "https://a.b.c")
        .withParcelable("pac", testParcelable)
        .withObject("obj", testObj)
        .withObject("objList", objList)
        .withObject("map", map)
        .navigation();
```

4.添加跳转动画

```
//旧版本跳转动画
XRouter.getInstance()
        .build("/test/activity2")
        .withTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
        .navigation(getActivity());

//新版本跳转动画
ActivityOptionsCompat compat = ActivityOptionsCompat.
        makeScaleUpAnimation(getListView(), getListView().getWidth() / 2, getListView().getHeight() / 2, 0, 0);
XRouter.getInstance()
        .build("/test/activity2")
        .withOptionsCompat(compat)
        .navigation();
```

5.添加路由跳转的监听

```
// 使用两个参数的navigation方法，可以获取单次跳转的结果
XRouter.getInstance().build("/test/1").navigation(this, new NavigationCallback() {
    @Override
    public void onFound(Postcard postcard) {
      ...
    }

    @Override
    public void onLost(Postcard postcard) {
	...
    }
});

```

### 3.3、通过URI跳转

1.新建一个Activity用于监听Scheme事件,之后直接把uri传递给XRouter即可

```
public class SchemeFilterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 直接通过XRouter处理外部Uri
        Uri uri = getIntent().getData();
        XRouter.getInstance().build(uri).navigation(this, new NavCallback() {
            @Override
            public void onArrival(Postcard postcard) {
                finish();
            }
        });
    }
}
```

2.在AndroidManifest.xml中注册`intent-filter`

```
<activity android:name=".activity.SchemeFilterActivity">
    <!-- Scheme -->
    <intent-filter>
        <data
            android:host="xuexiangjys.github.io"
            android:scheme="xrouter"/>

        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
    <!-- App Links -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW"/>

        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>

        <data
            android:host="xuexiangjys.github.io"
            android:scheme="http"/>
        <data
            android:host="xuexiangjys.github.io"
            android:scheme="https"/>
    </intent-filter>
</activity>
```

3.使用URI进行跳转

(1) 网页中的超链接URI路径：
```
<h2>自定义Scheme[通常来说都是这样的]</h2>
<p><a href="xrouter://xuexiangjys.github.io/test/activity1">xrouter://xuexiangjys.github.io/test/activity1</a></p>
<p><a href="xrouter://xuexiangjys.github.io/test/activity1?url=https%3a%2f%2fm.abc.com%3fa%3db%26c%3dd">测试URL Encode情况</a></p>
<p><a href="xrouter://xuexiangjys.github.io/test/activity1?name=alex&age=18&boy=true&high=180&obj=%7b%22name%22%3a%22jack%22%2c%22id%22%3a666%7d">xrouter://xuexiangjys.github.io/test/activity1?name=alex&age=18&boy=true&high=180&obj={"name":"jack","id":"666"}</a></p>
<p><a href="xrouter://xuexiangjys.github.io/test/activity2">xrouter://xuexiangjys.github.io/test/activity2</a></p>
<p><a href="xrouter://xuexiangjys.github.io/test/activity2?key1=value1">xrouter://xuexiangjys.github.io/test/activity2?key1=value1</a></p>
<p><a href="xrouter://xuexiangjys.github.io/test/activity3?name=alex&age=18&boy=true&high=180">xrouter://xuexiangjys.github.io/test/activity3?name=alex&age=18&boy=true&high=180</a></p>

<h2>App Links[防止被App屏蔽]</h2>
<p><a href="http://xuexiangjys.github.io/test/activity1">http://xuexiangjys.github.io/test/activity1</a></p>
<p><a href="http://xuexiangjys.github.io/test/activity2">http://xuexiangjys.github.io/test/activity2</a></p>
```

(2) 构建一个URI

```
Uri testUriMix = Uri.parse("xrouter://xuexiangjys.github.io/test/activity2");
XRouter.getInstance().build(testUriMix)
        .withString("key1", "value1")
        .navigation();
```

### 3.4、参数自动注入

1.使用`@AutoWired`标注需要自动注入的参数。

* 在需要自动注入的参数上添加`@AutoWired`注解，可设置name、required、desc属性。

* 自动注入的参数不能是`private`类型。

```
@AutoWired(required = true)
String name = "jack";

@AutoWired
int age = 10;

@AutoWired
int height = 175;

@AutoWired(name = "boy")
boolean girl;
```

2.使用`XRouter.getInstance().inject(this);`进行依赖注入。

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test1);
    //进行依赖注入
    XRouter.getInstance().inject(this);
}
```

3.如果需要传递或者依赖注入Object类型的参数，需要添加实现 SerializationService 服务，这里以Gson实现序列化的服务为例如下：

```
@Router(path = "/service/json")
public class JsonSerializationService implements SerializationService {
    @Override
    public void init(Context context) {

    }

    @Override
    public String object2Json(Object instance) {
        return JsonUtil.toJson(instance);
    }

    @Override
    public <T> T parseObject(String input, Type clazz) {
        return JsonUtil.fromJson(input, clazz);
    }
}
```

### 3.5、服务发现

1.构建服务：实现`IProvider`接口或者实现继承`IProvider`的接口，使用@Router进行标注。例如：

```
public interface HelloService extends IProvider {
    void sayHello(String name);
}

@Router(path = "/service/hello")
public class HelloServiceImpl implements HelloService {
    /**
     * Do your init work in this method, it well be call when processor has been load.
     *
     * @param context ctx
     */
    @Override
    public void init(Context context) {
    }

    @Override
    public void sayHello(String name) {
        ToastUtils.toast("Hello " + name);
    }
}
```

【注意】：这里需要注意，实现的服务不能自定义构造函数，只能使用默认的构造函数。

2.发现服务：根据服务的类或者服务注册的路由获取服务，也可以使用依赖注入的方式获取服务。

（1）根据服务的类获取服务

```
XRouter.getInstance().navigation(HelloService.class)
                .sayHello("mike");
```

（2）根据服务注册的路由获取服务

```
((HelloService) XRouter.getInstance().build("/service/hello")
                .navigation())
                .sayHello("mike~~");
```

（3）依赖注入的方式获取服务

```
public class Test {
    @Autowired
    HelloService helloService;

    @Autowired(name = "/service/hello")
    HelloService helloService2;

    public Test() {
	    ARouter.getInstance().inject(this);
    }

    public void testService() {
        // 1. (推荐)使用依赖注入的方式发现服务,通过注解标注字段,即可使用，无需主动获取
        // Autowired注解中标注name之后，将会使用byName的方式注入对应的字段，不设置name属性，会默认使用byType的方式发现服务(当同一接口有多个实现的时候，必须使用byName的方式发现服务)
        helloService.sayHello("Vergil");
        helloService2.sayHello("Vergil");
    }
}
```

### 3.6、路由拦截器

1.需要实现`IInterceptor`接口，使用@Interceptor进行标注。

2.配置拦截器的优先级（必填）。优先级的数字越小，拦截器的优先级越高。XRouter将按优先级高低依次执行拦截.

3.拦截器的优先级不能重复，每个拦截器只能拥有不同的优先级。

```
@Interceptor(priority = 8, name = "测试用拦截器")
public class TestInterceptor implements IInterceptor {
    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
	...
	callback.onContinue(postcard);  // 处理完成，交还控制权
	// callback.onInterrupt(new RuntimeException("我觉得有点异常")); // 觉得有问题，中断路由流程

	// 以上两种至少需要调用其中一种，否则不会继续路由
    }

    @Override
    public void init(Context context) {
	// 拦截器的初始化，会在sdk初始化的时候调用该方法，仅会调用一次
    }
}
```

### 3.7、路由降级

1.全局路由降级：全局路由降级需要实现`DegradeService`接口，示例代码如下：

```
@Router(path = "/service/degrade")
public class ToastDegradeService implements DegradeService {
    /**
     * 路由丢失.
     *
     * @param context
     * @param postcard 路由信息
     */
    @Override
    public void onLost(Context context, Postcard postcard) {
        //这里做你的降级操作
        ToastUtils.toast("进行全局的降级~~");
    }

    /**
     * 进程初始化的方法
     *
     * @param context 上下文
     */
    @Override
    public void init(Context context) {

    }
}
```

2.局部路由降级

```
XRouter.getInstance().build("/xxx/xxx").navigation(getContext(), new NavCallback() {
    @Override
    public void onFound(Postcard postcard) {
        Log.d("XRouter", "找到了");
    }

    @Override
    public void onLost(Postcard postcard) {
        Log.d("XRouter", "找不到了");
        //这里做你的降级操作
        ToastUtils.toast("进行局部的降级~~");
    }

    @Override
    public void onArrival(Postcard postcard) {
        Log.d("XRouter", "跳转完了");
    }

    @Override
    public void onInterrupt(Postcard postcard) {
        Log.d("XRouter", "被拦截了");
    }
});
```

### 3.8、路由重定向

需要实现`PathReplaceService`，并配置@Router标注。实现如下:

```
@Router(path = "/xxx/xxx") // 必须标明注解
public class PathReplaceServiceImpl implements PathReplaceService {
    /**
     * For normal path.
     *
     * @param path raw path
     */
    String forString(String path) {
	    return path;    // 按照一定的规则处理之后返回处理后的结果
    }

    /**
     * For uri type.
     *
     * @param uri raw uri
     */
    Uri forUri(Uri uri) {
        return url;    // 按照一定的规则处理之后返回处理后的结果
    }
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