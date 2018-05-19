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

package com.xuexiang.xrouter.core;

import android.content.Context;
import android.net.Uri;

import com.xuexiang.xrouter.enums.TypeKind;
import com.xuexiang.xrouter.exception.HandlerException;
import com.xuexiang.xrouter.exception.NoRouteFoundException;
import com.xuexiang.xrouter.facade.Postcard;
import com.xuexiang.xrouter.facade.template.IInterceptorGroup;
import com.xuexiang.xrouter.facade.template.IProvider;
import com.xuexiang.xrouter.facade.template.IProviderGroup;
import com.xuexiang.xrouter.facade.template.IRouteGroup;
import com.xuexiang.xrouter.facade.template.IRouteRoot;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xrouter.logs.XRLog;
import com.xuexiang.xrouter.model.RouteInfo;
import com.xuexiang.xrouter.utils.ClassUtils;
import com.xuexiang.xrouter.utils.MapUtils;
import com.xuexiang.xrouter.utils.PackageUtils;
import com.xuexiang.xrouter.utils.TextUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.xuexiang.xrouter.utils.Consts.DOT;
import static com.xuexiang.xrouter.utils.Consts.ROUTE_ROOT_PAKCAGE;
import static com.xuexiang.xrouter.utils.Consts.SDK_NAME;
import static com.xuexiang.xrouter.utils.Consts.SEPARATOR;
import static com.xuexiang.xrouter.utils.Consts.SUFFIX_INTERCEPTORS;
import static com.xuexiang.xrouter.utils.Consts.SUFFIX_PROVIDERS;
import static com.xuexiang.xrouter.utils.Consts.SUFFIX_ROOT;
import static com.xuexiang.xrouter.utils.Consts.TAG;
import static com.xuexiang.xrouter.utils.Consts.XROUTER_SP_CACHE_KEY;
import static com.xuexiang.xrouter.utils.Consts.XROUTER_SP_KEY_MAP;

/**
 * 路由中心，存放并操作所有的路由信息
 * <p>实质是操作{@link Warehouse}路由信息仓库的中心，类似物流中心和物流仓库的关系</p>
 * @author xuexiang
 * @since 2018/5/17 下午11:22
 */
public class LogisticsCenter {

    private static Context mContext;
    static ThreadPoolExecutor executor;
    private static boolean registerByPlugin;

    /**
     * xrouter-plugin 将自动生成代码到该方法进行路由信息的注册
     * 该方法注册所有的路由信息、拦截器、接口服务
     */
    private static void loadRouterMap() {
        registerByPlugin = false;
        //auto generate register code by gradle plugin: xrouter-plugin
        // looks like below:
        // registerRouteRoot(new ARouter..Root..modulejava());
        // registerRouteRoot(new ARouter..Root..modulekotlin());
    }

    /**
     * 通过类名注册路由信息
     *
     * @param className 类名，必须实现{@link IRouteRoot}/{@link IProviderGroup}/{@link IInterceptorGroup}中任意一个接口
     */
    private static void register(String className) {
        if (!TextUtils.isEmpty(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.getConstructor().newInstance();
                if (obj instanceof IRouteRoot) {
                    registerRouteRoot((IRouteRoot) obj);
                } else if (obj instanceof IProviderGroup) {
                    registerProvider((IProviderGroup) obj);
                } else if (obj instanceof IInterceptorGroup) {
                    registerInterceptor((IInterceptorGroup) obj);
                } else {
                    XRLog.i("register failed, class name: " + className
                            + " should implements one of IRouteRoot/IProviderGroup/IInterceptorGroup.");
                }
            } catch (Exception e) {
                XRLog.e("register class error:" + className, e);
            }
        }
    }

    /**
     * 供xrouter-plugin注册路由信息的方法
     * @param routeRoot {@link IRouteRoot} implementation class in the package: com.xuexiang.xrouter.routers
     * @author xuexiang
     * @since 2018/5/17 下午11:30
     */
    private static void registerRouteRoot(IRouteRoot routeRoot) {
        markRegisteredByPlugin();
        if (routeRoot != null) {
            routeRoot.loadInto(Warehouse.groupsIndex);
        }
    }
    /**
     * 供xrouter-plugin注册拦截器的方法
     * @param interceptorGroup {@link IInterceptorGroup} implementation class in the package: com.xuexiang.xrouter.routers
     * @author xuexiang
     * @since 2018/5/17 下午11:35
     */
    private static void registerInterceptor(IInterceptorGroup interceptorGroup) {
        markRegisteredByPlugin();
        if (interceptorGroup != null) {
            interceptorGroup.loadInto(Warehouse.interceptorsIndex);
        }
    }

    /**
     * 供xrouter-plugin注册服务提供者的方法
     * @param providerGroup {@link IProviderGroup} implementation class in the package: com.xuexiang.xrouter.routers
     * @author xuexiang
     * @since 2018/5/17 下午11:38
     */
    private static void registerProvider(IProviderGroup providerGroup) {
        markRegisteredByPlugin();
        if (providerGroup != null) {
            providerGroup.loadInto(Warehouse.providersIndex);
        }
    }

    /**
     * 标记是通过xrouter-plugin进行注册
     */
    private static void markRegisteredByPlugin() {
        if (!registerByPlugin) {
            registerByPlugin = true;
        }
    }

    /**
     * 初始化路由中心， 加载所有的路由数据到内存中
     */
    public synchronized static void init(Context context, ThreadPoolExecutor tpe) throws HandlerException {
        mContext = context;
        executor = tpe;

        try {
            long startInit = System.currentTimeMillis();
            loadRouterMap();
            if (registerByPlugin) {
                XRLog.i("Load router map by xrouter-plugin.");
            } else {   //非xrouter-plugin加载，就扫描"com.xuexiang.xrouter.routes"包手动加载路由信息表
                Set<String> routerMap;
                // 只有当应用是调试模式或者是新版本时，才会加载路由表到内存中.
                if (XRouter.debuggable() || PackageUtils.isNewVersion(context)) {
                    XRLog.i("Run with debug mode or new install, rebuild router map.");
                    // These class was generated by xrouter-compiler.
                    routerMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);
                    if (!routerMap.isEmpty()) {
                        //将扫描到的路由表的class文件名储存在SP中，下次进来直接从SP里面读
                        context.getSharedPreferences(XROUTER_SP_CACHE_KEY, Context.MODE_PRIVATE).edit().putStringSet(XROUTER_SP_KEY_MAP, routerMap).apply();
                    }

                    PackageUtils.updateVersion(context);    // 路由表更新后，更新版本
                } else {
                    XRLog.i("Load router map from cache[SP].");
                    routerMap = new HashSet<>(context.getSharedPreferences(XROUTER_SP_CACHE_KEY, Context.MODE_PRIVATE).getStringSet(XROUTER_SP_KEY_MAP, new HashSet<String>()));
                }

                XRLog.i("Find router map finished, map size = " + routerMap.size() + ", cost " + (System.currentTimeMillis() - startInit) + " ms.");
                startInit = System.currentTimeMillis();

                for (String className : routerMap) {
                    //com.xuexiang.xrouter.routes.XRouter$$Root
                    if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                        // This one of root elements, load root.
                        ((IRouteRoot) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.groupsIndex);

                    //com.xuexiang.xrouter.routes.XRouter$$Interceptors
                    } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
                        // Load interceptorMeta
                        ((IInterceptorGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.interceptorsIndex);

                    //com.xuexiang.xrouter.routes.XRouter$$Providers
                    } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                        // Load providerIndex
                        ((IProviderGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.providersIndex);
                    }
                }
            }

            XRLog.i("Load root element finished, cost " + (System.currentTimeMillis() - startInit) + " ms.");

            if (Warehouse.groupsIndex.size() == 0) {
                XRLog.e("No mapping files were found, check your configuration please!");
            }

            if (XRouter.debuggable()) {
                XRLog.d(String.format(Locale.getDefault(), "LogisticsCenter has already been loaded, GroupIndex[%d], InterceptorIndex[%d], ProviderIndex[%d]", Warehouse.groupsIndex.size(), Warehouse.interceptorsIndex.size(), Warehouse.providersIndex.size()));
            }
        } catch (Exception e) {
            throw new HandlerException(TAG + "XRouter init logistics center exception! [" + e.getMessage() + "]");
        }
    }

    /**
     * 通过服务名构建Provider
     *
     * @param serviceName 服务名
     * @return postcard
     */
    public static Postcard buildProvider(String serviceName) {
        RouteInfo info = Warehouse.providersIndex.get(serviceName);
        if (info == null) {
            return null;
        } else {
            return new Postcard(info.getPath(), info.getGroup());
        }
    }

    /**
     * 通过存放在内存中的路由表信息组装postcard【加载路由表（将路由组IRouteGroup里的路由信息加入到路由表中）（初始化IProvider并加入到路由表中）】
     *
     * @param postcard Incomplete postcard, should complete by this method.
     */
    public synchronized static void completion(Postcard postcard) {
        if (postcard == null) {
            throw new NoRouteFoundException(TAG + "No postcard!");
        }

        RouteInfo routeInfo = Warehouse.routes.get(postcard.getPath());
        if (routeInfo == null) {   // 路由信息不存在内存中的话，先加载
            Class<? extends IRouteGroup> groupInfo = Warehouse.groupsIndex.get(postcard.getGroup());  // Load routeInfo.
            if (groupInfo == null) {
                throw new NoRouteFoundException(TAG + "There is no route match the path [" + postcard.getPath() + "], in group [" + postcard.getGroup() + "]");
            } else {
                // Load route and cache it into memory, then delete it.
                try {
                    if (XRouter.debuggable()) {
                        XRLog.d(String.format(Locale.getDefault(), "The group [%s] starts loading, trigger by [%s]", postcard.getGroup(), postcard.getPath()));
                    }

                    //将路由组里的路由信息加载至内存中，然后从内存中删除路由组
                    IRouteGroup iGroupInstance = groupInfo.getConstructor().newInstance();
                    iGroupInstance.loadInto(Warehouse.routes);
                    Warehouse.groupsIndex.remove(postcard.getGroup());

                    if (XRouter.debuggable()) {
                        XRLog.d(String.format(Locale.getDefault(), "The group [%s] has already been loaded, trigger by [%s]", postcard.getGroup(), postcard.getPath()));
                    }
                } catch (Exception e) {
                    throw new HandlerException(TAG + "Fatal exception when loading group info. [" + e.getMessage() + "]");
                }

                completion(postcard);   // Reload
            }
        } else {
            //构建postcard
            postcard.setDestination(routeInfo.getDestination());
            postcard.setType(routeInfo.getType());
            postcard.setPriority(routeInfo.getPriority());
            postcard.setExtra(routeInfo.getExtra());

            Uri rawUri = postcard.getUri();
            if (rawUri != null) {   // Try to set params into bundle.
                Map<String, String> resultMap = TextUtils.splitQueryParameters(rawUri);
                Map<String, Integer> paramsType = routeInfo.getParamsType();

                if (MapUtils.isNotEmpty(paramsType)) {
                    // Set value by its type, just for params which annotation by @Param
                    for (Map.Entry<String, Integer> params : paramsType.entrySet()) {
                        setValue(postcard,
                                params.getValue(),
                                params.getKey(),
                                resultMap.get(params.getKey()));
                    }

                    // Save params name which need auto inject.
                    postcard.getExtras().putStringArray(XRouter.AUTO_INJECT, paramsType.keySet().toArray(new String[]{}));
                }
                // Save raw uri
                postcard.withString(XRouter.RAW_URI, rawUri.toString());
            }

            switch (routeInfo.getType()) {
                case PROVIDER:  // 如果路由是服务接口IProvider，就需要找到他的实例
                    // Its provider, so it must implement IProvider
                    Class<? extends IProvider> providerInfo = (Class<? extends IProvider>) routeInfo.getDestination();
                    IProvider instance = Warehouse.providers.get(providerInfo);
                    if (instance == null) { // There's no instance of this provider
                        IProvider provider;
                        try {
                            //没有的话就新建接口，并调用初始化方法，然后加入到内存中
                            provider = providerInfo.getConstructor().newInstance();
                            provider.init(mContext);
                            Warehouse.providers.put(providerInfo, provider);
                            instance = provider;
                        } catch (Exception e) {
                            throw new HandlerException("Init provider failed! " + e.getMessage());
                        }
                    }
                    postcard.setProvider(instance);
                    postcard.greenChannel();    // Provider should skip all of interceptors
                    break;
                case FRAGMENT:
                    postcard.greenChannel();    // Fragment needn't interceptors
                default:
                    break;
            }
        }
    }

    /**
     * 根据类型设置字段值
     *
     * @param postcard postcard
     * @param typeDef  type 字段类型
     * @param key      key
     * @param value    value
     */
    private static void setValue(Postcard postcard, Integer typeDef, String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        try {
            if (typeDef != null) {
                if (typeDef == TypeKind.BOOLEAN.ordinal()) {
                    postcard.withBoolean(key, Boolean.parseBoolean(value));
                } else if (typeDef == TypeKind.BYTE.ordinal()) {
                    postcard.withByte(key, Byte.valueOf(value));
                } else if (typeDef == TypeKind.SHORT.ordinal()) {
                    postcard.withShort(key, Short.valueOf(value));
                } else if (typeDef == TypeKind.INT.ordinal()) {
                    postcard.withInt(key, Integer.valueOf(value));
                } else if (typeDef == TypeKind.LONG.ordinal()) {
                    postcard.withLong(key, Long.valueOf(value));
                } else if (typeDef == TypeKind.FLOAT.ordinal()) {
                    postcard.withFloat(key, Float.valueOf(value));
                } else if (typeDef == TypeKind.DOUBLE.ordinal()) {
                    postcard.withDouble(key, Double.valueOf(value));
                } else if (typeDef == TypeKind.STRING.ordinal()) {
                    postcard.withString(key, value);
                } else if (typeDef == TypeKind.PARCELABLE.ordinal()) {
                    // TODO : How to description parcelable value with string?
                } else if (typeDef == TypeKind.OBJECT.ordinal()) {
                    postcard.withString(key, value);
                } else {    // Compatible compiler sdk 1.0.3, in that version, the string type = 18
                    postcard.withString(key, value);
                }
            } else {
                postcard.withString(key, value);
            }
        } catch (Throwable ex) {
            XRLog.e("LogisticsCenter setValue failed! " + ex.getMessage(), ex);
        }
    }

    /**
     * 暂停业务, 清理缓存.
     */
    public static void suspend() {
        Warehouse.clear();
    }
}
