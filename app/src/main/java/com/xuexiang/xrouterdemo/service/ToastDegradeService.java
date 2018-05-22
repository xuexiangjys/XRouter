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

package com.xuexiang.xrouterdemo.service;

import android.content.Context;

import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.facade.Postcard;
import com.xuexiang.xrouter.facade.service.DegradeService;
import com.xuexiang.xutil.tip.ToastUtils;

/**
 * @author xuexiang
 * @since 2018/5/22 下午2:55
 */
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
