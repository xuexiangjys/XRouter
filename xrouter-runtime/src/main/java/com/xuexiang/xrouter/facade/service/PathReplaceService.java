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

package com.xuexiang.xrouter.facade.service;

import android.net.Uri;

import com.xuexiang.xrouter.facade.template.IProvider;

/**
 * 路由路径重定向
 *
 * @author xuexiang
 * @since 2018/5/17 上午1:01
 */
public interface PathReplaceService extends IProvider {

    /**
     * 重定向普通String类型的路由路径
     *
     * @param path raw path
     */
    String forString(String path);

    /**
     * 重定向资源uri类型的路由路径
     *
     * @param uri raw uri
     */
    Uri forUri(Uri uri);
}
