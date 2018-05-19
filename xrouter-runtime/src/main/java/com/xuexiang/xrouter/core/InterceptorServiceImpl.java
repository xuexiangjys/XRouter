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

import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.exception.HandlerException;
import com.xuexiang.xrouter.facade.Postcard;
import com.xuexiang.xrouter.facade.callback.InterceptorCallback;
import com.xuexiang.xrouter.facade.service.InterceptorService;
import com.xuexiang.xrouter.facade.template.IInterceptor;
import com.xuexiang.xrouter.logs.XRLog;
import com.xuexiang.xrouter.thread.CancelableCountDownLatch;
import com.xuexiang.xrouter.utils.MapUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xuexiang.xrouter.utils.Consts.ROUTE_SERVICE_INTERCEPTORS;
import static com.xuexiang.xrouter.utils.Consts.TAG;

/**
 * 拦截器服务的实现类，实现全局路由拦截服务
 *
 * @author xuexiang
 * @since 2018/5/19 下午7:14
 */
@Router(path = ROUTE_SERVICE_INTERCEPTORS)
public class InterceptorServiceImpl implements InterceptorService {
    private static boolean interceptorHasInit;
    private static final Object interceptorInitLock = new Object();

    /**
     * 执行拦截器
     *
     * @param postcard
     * @param callback 拦截器的回调监听
     */
    @Override
    public void doInterceptions(final Postcard postcard, final InterceptorCallback callback) {
        if (Warehouse.interceptors != null && Warehouse.interceptors.size() > 0) {

            checkInterceptorsInitStatus();

            if (!interceptorHasInit) {
                callback.onInterrupt(new HandlerException("Interceptors initialization takes too much time."));
                return;
            }

            LogisticsCenter.executor.execute(new Runnable() {
                @Override
                public void run() {
                    //并发锁，保证所有的拦截器按顺序执行
                    CancelableCountDownLatch interceptorCounter = new CancelableCountDownLatch(Warehouse.interceptors.size());
                    try {
                        _execute(0, interceptorCounter, postcard);
                        interceptorCounter.await(postcard.getTimeout(), TimeUnit.SECONDS);
                        if (interceptorCounter.getCount() > 0) {    // Cancel the navigation this time, if it hasn't return anythings.
                            callback.onInterrupt(new HandlerException("The interceptor processing timed out."));
                        } else if (null != postcard.getTag()) {    // Maybe some exception in the tag.
                            callback.onInterrupt(new HandlerException(postcard.getTag().toString()));
                        } else {
                            callback.onContinue(postcard);
                        }
                    } catch (Exception e) {
                        callback.onInterrupt(e);
                    }
                }
            });
        } else {
            callback.onContinue(postcard);
        }
    }

    /**
     * 执行拦截器
     *
     * @param index    档期执行拦截器的索引
     * @param counter  拦截器的执行计数锁
     * @param postcard 路由信息
     */
    private static void _execute(final int index, final CancelableCountDownLatch counter, final Postcard postcard) {
        if (index < Warehouse.interceptors.size()) {
            IInterceptor iInterceptor = Warehouse.interceptors.get(index);
            iInterceptor.process(postcard, new InterceptorCallback() { //执行拦截器的process方法
                @Override
                public void onContinue(Postcard postcard) {
                    // Last interceptor execute over with no exception.
                    counter.countDown();
                    _execute(index + 1, counter, postcard);  // When counter is down, it will be execute continue ,but index bigger than interceptors size, then U know.
                }

                @Override
                public void onInterrupt(Throwable exception) {
                    // Last interceptor execute over with fatal exception.
                    postcard.setTag(null == exception ? new HandlerException("No message.") : exception.getMessage());    // save the exception message for backup.
                    counter.cancel();
                    // Be attention, maybe the thread in callback has been changed,
                    // then the catch block(L207) will be invalid.
                    // The worst is the thread changed to main thread, then the app will be crash, if you throw this exception!
//                    if (!Looper.getMainLooper().equals(Looper.myLooper())) {    // You shouldn't throw the exception if the thread is main thread.
//                        throw new HandlerException(exception.getMessage());
//                    }
                }
            });
        }
    }

    /**
     * 初始化所有的拦截器，并将其加入到路由表中
     *
     * @param context 上下文
     */
    @Override
    public void init(final Context context) {
        LogisticsCenter.executor.execute(new Runnable() {
            @Override
            public void run() {
                if (MapUtils.isNotEmpty(Warehouse.interceptorsIndex)) {
                    for (Map.Entry<Integer, Class<? extends IInterceptor>> entry : Warehouse.interceptorsIndex.entrySet()) {
                        Class<? extends IInterceptor> interceptorClass = entry.getValue();
                        try {
                            //初始化所有的拦截器，并将其加入到路由表中
                            IInterceptor iInterceptor = interceptorClass.getConstructor().newInstance();
                            iInterceptor.init(context);
                            Warehouse.interceptors.add(iInterceptor);
                        } catch (Exception ex) {
                            throw new HandlerException(TAG + "XRouter init interceptor error! name = [" + interceptorClass.getName() + "], reason = [" + ex.getMessage() + "]");
                        }
                    }

                    interceptorHasInit = true;

                    XRLog.i("XRouter interceptors init over.");

                    synchronized (interceptorInitLock) {
                        interceptorInitLock.notifyAll();
                    }
                }
            }
        });
    }

    /**
     * 检查拦截器的初始化状态
     */
    private static void checkInterceptorsInitStatus() {
        synchronized (interceptorInitLock) {
            while (!interceptorHasInit) {
                try {
                    interceptorInitLock.wait(10 * 1000);
                } catch (InterruptedException e) {
                    throw new HandlerException(TAG + "Interceptor init cost too much time error! reason = [" + e.getMessage() + "]");
                }
            }
        }
    }
}
