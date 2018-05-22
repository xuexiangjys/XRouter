/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuexiang.xrouter.launcher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.xuexiang.xrouter.core.LogisticsCenter;
import com.xuexiang.xrouter.exception.HandlerException;
import com.xuexiang.xrouter.exception.InitException;
import com.xuexiang.xrouter.exception.NoRouteFoundException;
import com.xuexiang.xrouter.facade.Postcard;
import com.xuexiang.xrouter.facade.callback.InterceptorCallback;
import com.xuexiang.xrouter.facade.callback.NavigationCallback;
import com.xuexiang.xrouter.facade.service.AutoWiredService;
import com.xuexiang.xrouter.facade.service.DegradeService;
import com.xuexiang.xrouter.facade.service.InterceptorService;
import com.xuexiang.xrouter.facade.service.PathReplaceService;
import com.xuexiang.xrouter.facade.template.IProvider;
import com.xuexiang.xrouter.logs.ILogger;
import com.xuexiang.xrouter.logs.XRLog;
import com.xuexiang.xrouter.thread.DefaultPoolExecutor;
import com.xuexiang.xrouter.utils.Consts;
import com.xuexiang.xrouter.utils.TextUtils;

import java.util.concurrent.ThreadPoolExecutor;

import static com.xuexiang.xrouter.utils.Consts.ROUTE_ROOT_SEIVICE;
import static com.xuexiang.xrouter.utils.Consts.ROUTE_SERVICE_AUTOWIRED;
import static com.xuexiang.xrouter.utils.Consts.ROUTE_SERVICE_INTERCEPTORS;

/**
 * XRouter 核心功能
 *
 * @author xuexiang
 * @since 2018/5/18 上午12:32
 */
final class _XRouter {
    private volatile static boolean monitorMode = false;
    private volatile static boolean debuggable = false;
    private volatile static _XRouter sInstance = null;
    private volatile static boolean hasInit = false;
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    private static Handler mMainHandler = new Handler(Looper.getMainLooper());
    private static Context mContext;

    private static InterceptorService interceptorService;

    private _XRouter() {
    }

    protected static synchronized boolean init(Application application) {
        mContext = application;
        LogisticsCenter.init(mContext, executor);
        XRLog.i("XRouter init success!");
        hasInit = true;

        // It's not a good idea.
        // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        //     application.registerActivityLifecycleCallbacks(new AutowiredLifecycleCallback());
        // }
        return true;
    }

    /**
     * Destroy XRouter, it can be used only in debug mode.
     */
    static synchronized void destroy() {
        if (debuggable()) {
            hasInit = false;
            LogisticsCenter.suspend();
            XRLog.i("XRouter destroy success!");
        } else {
            XRLog.e("Destroy can be used in debug mode only!");
        }
    }

    protected static _XRouter getInstance() {
        if (!hasInit) {
            throw new InitException("XRouterCore::Init::Invoke init(context) first!");
        } else {
            if (sInstance == null) {
                synchronized (_XRouter.class) {
                    if (sInstance == null) {
                        sInstance = new _XRouter();
                    }
                }
            }
            return sInstance;
        }
    }

    static synchronized void openDebug() {
        debuggable = true;
        XRLog.i("XRouter openDebug");
    }

    static synchronized void openLog() {
        XRLog.debug(true);
        XRLog.i("XRouter openLog");
    }

    static synchronized void setExecutor(ThreadPoolExecutor tpe) {
        executor = tpe;
    }

    static synchronized void monitorMode() {
        monitorMode = true;
        XRLog.i("XRouter monitorMode on");
    }

    static boolean isMonitorMode() {
        return monitorMode;
    }

    static boolean debuggable() {
        return debuggable;
    }

    static void setLogger(ILogger logger) {
        XRLog.setLogger(logger);
    }

    static void inject(Object target) {
        AutoWiredService autoWiredService = ((AutoWiredService) XRouter.getInstance().build(ROUTE_SERVICE_AUTOWIRED).navigation());
        if (autoWiredService != null) {
            autoWiredService.autoWire(target);
        }
    }

    /**
     * 通过path和default group构建路由表
     *
     * @param path 路由路径
     */
    protected Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new HandlerException(Consts.TAG + "Parameter is invalid!");
        } else {
            PathReplaceService pService = XRouter.getInstance().navigation(PathReplaceService.class);
            if (pService != null) {
                path = pService.forString(path);
            }
            return build(path, extractGroup(path));
        }
    }

    /**
     * 通过path和group构建路由表
     *
     * @param path  路由路径
     * @param group 路由组
     */
    protected Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new HandlerException(Consts.TAG + "Parameter is invalid!");
        } else {
            PathReplaceService pService = XRouter.getInstance().navigation(PathReplaceService.class);
            if (pService != null) {
                path = pService.forString(path);
            }
            return new Postcard(path, group);
        }
    }

    /**
     * 通过uri构建路由表
     *
     * @param uri 资源路径
     */
    protected Postcard build(Uri uri) {
        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            throw new HandlerException(Consts.TAG + "Parameter invalid!");
        } else {
            PathReplaceService pService = XRouter.getInstance().navigation(PathReplaceService.class);
            if (pService != null) {
                uri = pService.forUri(uri);
            }
            return new Postcard(uri.getPath(), extractGroup(uri.getPath()), uri, null);
        }
    }

    /**
     * 从路由路径中抽出路由组
     *
     * @param path 路由路径
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new HandlerException(Consts.TAG + "Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }
        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new HandlerException(Consts.TAG + "Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            XRLog.w("Failed to extract default group! " + e.getMessage());
            return null;
        }
    }

    static void afterInit() {
        // Trigger interceptor init, use byName.
        interceptorService = (InterceptorService) XRouter.getInstance().build(ROUTE_SERVICE_INTERCEPTORS).navigation();
    }

    /**
     * 服务发现（需要实现{@link IProvider}接口）
     *
     * @param service
     * @param <T>
     * @return
     */
    protected <T> T navigation(Class<? extends T> service) {
        try {
            Postcard postcard = LogisticsCenter.buildProvider(service.getName());
            // Compatible 1.0.5 compiler sdk.
            if (postcard == null) { // No service, or this service in old version.
                postcard = LogisticsCenter.buildProvider(service.getSimpleName());
            }
            LogisticsCenter.completion(postcard);
            return (T) postcard.getProvider();
        } catch (NoRouteFoundException ex) {
            XRLog.w(ex.getMessage());

            if (debuggable() && !service.getName().contains(ROUTE_ROOT_SEIVICE)) { // Show friendly tips for user.
                String tips = "There's no service matched!\n" +
                        " service name = [" + service.getName() + "]";
                Toast.makeText(mContext, tips, Toast.LENGTH_LONG).show();
                XRLog.i(tips);
            }
            return null;
        }
    }

    /**
     * 进行路由导航
     *
     * @param context     Activity or null.
     * @param postcard    路由信息容器
     * @param requestCode 请求码
     * @param callback    路由导航回调
     */
    protected Object navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        try {
            LogisticsCenter.completion(postcard);
        } catch (NoRouteFoundException ex) {
            XRLog.e(ex);

            if (debuggable()) { // Show friendly tips for user.
                String tips = "There's no route matched!\n" +
                        " Path = [" + postcard.getPath() + "]\n" +
                        " Group = [" + postcard.getGroup() + "]";
                Toast.makeText(mContext, tips, Toast.LENGTH_LONG).show();
                XRLog.i(tips);
            }

            if (callback != null) {
                callback.onLost(postcard);
            } else {    // No callback for this invoke, then we use the global degrade service.
                DegradeService degradeService = XRouter.getInstance().navigation(DegradeService.class);
                if (degradeService != null) {
                    degradeService.onLost(context, postcard);
                }
            }

            return null;
        }

        if (callback != null) {
            callback.onFound(postcard);
        }

        if (!postcard.isGreenChannel()) {   // It must be run in async thread, maybe interceptor cost too mush time made ANR.
            interceptorService.doInterceptions(postcard, new InterceptorCallback() {
                /**
                 * 继续执行下一个拦截器
                 *
                 * @param postcard 路由信息
                 */
                @Override
                public void onContinue(Postcard postcard) {
                    _navigation(context, postcard, requestCode, callback);
                }

                /**
                 * 拦截中断, 当该方法执行后，通道将会被销毁
                 *
                 * @param exception 中断的原因.
                 */
                @Override
                public void onInterrupt(Throwable exception) {
                    if (callback != null) {
                        callback.onInterrupt(postcard);
                    }
                    XRLog.i("Navigation failed, termination by interceptor : " + exception.getMessage());
                }
            });
        } else {
            return _navigation(context, postcard, requestCode, callback);
        }

        return null;
    }

    /**
     * 真正执行导航的方法
     *
     * @param context
     * @param postcard    路由容器
     * @param requestCode 请求code
     * @param callback    导航回调
     * @return
     */
    private Object _navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        final Context currentContext = null == context ? mContext : context;
        switch (postcard.getType()) {
            case ACTIVITY:
                // Build intent
                final Intent intent = new Intent(currentContext, postcard.getDestination());
                intent.putExtras(postcard.getExtras());

                // Set flags.
                int flags = postcard.getFlags();
                if (flags != -1) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {    // Non activity, need less one flag.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                // Navigation in main looper.
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (requestCode > 0) {  // Need start for result
                            ActivityCompat.startActivityForResult((Activity) currentContext, intent, requestCode, postcard.getOptionsBundle());
                        } else {
                            ActivityCompat.startActivity(currentContext, intent, postcard.getOptionsBundle());
                        }

                        if ((-1 != postcard.getEnterAnim() && -1 != postcard.getExitAnim()) && currentContext instanceof Activity) {    // Old version.
                            ((Activity) currentContext).overridePendingTransition(postcard.getEnterAnim(), postcard.getExitAnim());
                        }

                        if (callback != null) { // Navigation over.
                            callback.onArrival(postcard);
                        }
                    }
                });
                break;
            case PROVIDER:
                return postcard.getProvider();
            case BROADCAST:
            case CONTENT_PROVIDER:
            case FRAGMENT:
                Class fragmentMeta = postcard.getDestination();
                try {
                    Object instance = fragmentMeta.getConstructor().newInstance();
                    if (instance instanceof Fragment) {
                        ((Fragment) instance).setArguments(postcard.getExtras());
                    } else if (instance instanceof android.support.v4.app.Fragment) {
                        ((android.support.v4.app.Fragment) instance).setArguments(postcard.getExtras());
                    }
                    return instance;
                } catch (Exception ex) {
                    XRLog.e("Fetch fragment instance error, " + TextUtils.formatStackTrace(ex.getStackTrace()));
                }
            case METHOD:
            case SERVICE:
            default:
                return null;
        }

        return null;
    }


    /**
     * 进行路由导航 [Fragment]
     *
     * @param fragment    fragment
     * @param postcard    路由信息容器
     * @param requestCode 请求码
     * @param callback    路由导航回调
     */
    protected Object navigation(final Fragment fragment, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        try {
            LogisticsCenter.completion(postcard);
        } catch (NoRouteFoundException ex) {
            XRLog.e(ex);

            if (debuggable()) { // Show friendly tips for user.
                String tips = "There's no route matched!\n" +
                        " Path = [" + postcard.getPath() + "]\n" +
                        " Group = [" + postcard.getGroup() + "]";
                Toast.makeText(mContext, tips, Toast.LENGTH_LONG).show();
                XRLog.i(tips);
            }

            if (callback != null) {
                callback.onLost(postcard);
            } else {    // No callback for this invoke, then we use the global degrade service.
                DegradeService degradeService = XRouter.getInstance().navigation(DegradeService.class);
                if (degradeService != null) {
                    degradeService.onLost(fragment.getActivity(), postcard);
                }
            }

            return null;
        }

        if (callback != null) {
            callback.onFound(postcard);
        }

        if (!postcard.isGreenChannel()) {   // It must be run in async thread, maybe interceptor cost too mush time made ANR.
            interceptorService.doInterceptions(postcard, new InterceptorCallback() {
                /**
                 * 继续执行下一个拦截器
                 *
                 * @param postcard 路由信息
                 */
                @Override
                public void onContinue(Postcard postcard) {
                    _navigation(fragment, postcard, requestCode, callback);
                }

                /**
                 * 拦截中断, 当该方法执行后，通道将会被销毁
                 *
                 * @param exception 中断的原因.
                 */
                @Override
                public void onInterrupt(Throwable exception) {
                    if (callback != null) {
                        callback.onInterrupt(postcard);
                    }
                    XRLog.i("Navigation failed, termination by interceptor : " + exception.getMessage());
                }
            });
        } else {
            return _navigation(fragment, postcard, requestCode, callback);
        }
        return null;
    }


    /**
     * 真正执行导航的方法 [Fragment]
     *
     * @param fragment
     * @param postcard    路由容器
     * @param requestCode 请求code
     * @param callback    导航回调
     * @return
     */
    private Object _navigation(final Fragment fragment, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        switch (postcard.getType()) {
            case ACTIVITY:
                // Build intent
                final Intent intent = new Intent(fragment.getActivity(), postcard.getDestination());
                intent.putExtras(postcard.getExtras());

                // Set flags.
                int flags = postcard.getFlags();
                if (flags != -1) {
                    intent.setFlags(flags);
                }
                // Navigation in main looper.
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (requestCode > 0) {  // Need start for result
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                fragment.startActivityForResult(intent, requestCode);
                            } else {
                                fragment.startActivityForResult(intent, requestCode, postcard.getOptionsBundle());
                            }
                        } else {
                            ActivityCompat.startActivity(fragment.getActivity(), intent, postcard.getOptionsBundle());
                        }

                        if ((-1 != postcard.getEnterAnim() && -1 != postcard.getExitAnim())) {    // Old version.
                            (fragment.getActivity()).overridePendingTransition(postcard.getEnterAnim(), postcard.getExitAnim());
                        }

                        if (callback != null) { // Navigation over.
                            callback.onArrival(postcard);
                        }
                    }
                });
                break;
            case PROVIDER:
                return postcard.getProvider();
            case BROADCAST:
            case CONTENT_PROVIDER:
            case FRAGMENT:
                Class fragmentMeta = postcard.getDestination();
                try {
                    Object instance = fragmentMeta.getConstructor().newInstance();
                    if (instance instanceof Fragment) {
                        ((Fragment) instance).setArguments(postcard.getExtras());
                    } else if (instance instanceof android.support.v4.app.Fragment) {
                        ((android.support.v4.app.Fragment) instance).setArguments(postcard.getExtras());
                    }
                    return instance;
                } catch (Exception ex) {
                    XRLog.e("Fetch fragment instance error, " + TextUtils.formatStackTrace(ex.getStackTrace()));
                }
            case METHOD:
            case SERVICE:
            default:
                return null;
        }

        return null;
    }
}
