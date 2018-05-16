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

package com.xuexiang.xrouter.facade.template;

import com.xuexiang.xrouter.facade.Postcard;
import com.xuexiang.xrouter.facade.callback.InterceptorCallback;

/**
 * 路由拦截器，在路由导航时可注入一些自定义逻辑
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:13
 */
public interface IInterceptor extends IProvider {

    /**
     * 拦截器的执行操作
     *
     * @param postcard 路由信息
     * @param callback 拦截回调
     */
    void process(Postcard postcard, InterceptorCallback callback);
}
