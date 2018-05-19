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

package com.xuexiang.xrouter.utils;

/**
 * XRouter的常量
 *
 * @author xuexiang
 * @since 2018/5/17 下午11:44
 */
public final class Consts {
    public static final String SDK_NAME = "XRouter";
    public static final String TAG = SDK_NAME + "::";
    public static final String SEPARATOR = "$$";
    public static final String SUFFIX_ROOT = "Root";
    public static final String SUFFIX_INTERCEPTORS = "Interceptors";
    public static final String SUFFIX_PROVIDERS = "Providers";
    public static final String SUFFIX_AUTOWIRED = SEPARATOR + SDK_NAME + SEPARATOR + "AutoWired";
    public static final String DOT = ".";
    public static final String ROUTE_ROOT_PAKCAGE = "com.xuexiang.xrouter.routes";

    public static final String ROUTE_SERVICE_INTERCEPTORS = "/xrouter/service/interceptor";
    public static final String ROUTE_SERVICE_AUTOWIRED = "/xrouter/service/autowired";
    /**
     * 路由缓存
     */
    public static final String XROUTER_SP_CACHE_KEY = "SP_XROUTER_CACHE";
    public static final String XROUTER_SP_KEY_MAP = "SP_XROUTER_MAP";
    public static final String LAST_VERSION_NAME = "LAST_VERSION_NAME";
    public static final String LAST_VERSION_CODE = "LAST_VERSION_CODE";
}
