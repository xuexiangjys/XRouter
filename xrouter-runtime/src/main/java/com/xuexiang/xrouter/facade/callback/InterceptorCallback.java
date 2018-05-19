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

package com.xuexiang.xrouter.facade.callback;


import com.xuexiang.xrouter.facade.Postcard;

/**
 * 拦截器的回调
 *
 * @author xuexiang
 * @since 2018/5/16 下午11:43
 */
public interface InterceptorCallback {

    /**
     * 继续执行下一个拦截器
     *
     * @param postcard 路由信息
     */
    void onContinue(Postcard postcard);

    /**
     * 拦截中断, 当该方法执行后，通道将会被销毁
     *
     * @param exception 中断的原因.
     */
    void onInterrupt(Throwable exception);
}
