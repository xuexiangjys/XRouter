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


import com.xuexiang.xrouter.facade.template.IInterceptor;
import com.xuexiang.xrouter.facade.template.IProvider;
import com.xuexiang.xrouter.facade.template.IRouteGroup;
import com.xuexiang.xrouter.model.RouteInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存放路由信息的仓库
 *
 * @author xuexiang
 * @since 2018/5/16 下午11:40
 */
class Warehouse {
    // Cache route and metas
    static Map<String, Class<? extends IRouteGroup>> groupsIndex = new HashMap<>();
    static Map<String, RouteInfo> routes = new HashMap<>();

    // Cache provider
    static Map<Class, IProvider> providers = new HashMap<>();
    static Map<String, RouteInfo> providersIndex = new HashMap<>();

    // Cache interceptor，因为使用的是TreeMap，且Key为Integer,从而实现拦截器的优先级
    static Map<Integer, Class<? extends IInterceptor>> interceptorsIndex = new UniqueKeyTreeMap<>("More than one interceptors use same priority [%s]");
    static List<IInterceptor> interceptors = new ArrayList<>();

    static void clear() {
        routes.clear();
        groupsIndex.clear();
        providers.clear();
        providersIndex.clear();
        interceptors.clear();
        interceptorsIndex.clear();
    }
}
