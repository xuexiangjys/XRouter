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
 * 执行navigation（导航）后的回调
 *
 * @author xuexiang
 * @since 2018/5/16 下午11:45
 */
public interface NavigationCallback {

    /**
     * 发现导航目标的回调
     *
     * @param postcard 路由信息
     */
    void onFound(Postcard postcard);

    /**
     * 路由丢失（找不到）的回调
     *
     * @param postcard 路由信息
     */
    void onLost(Postcard postcard);

    /**
     * 导航到达的回调
     *
     * @param postcard 路由信息
     */
    void onArrival(Postcard postcard);

    /**
     * 被拦截的回调
     *
     * @param postcard 路由信息
     */
    void onInterrupt(Postcard postcard);
}
