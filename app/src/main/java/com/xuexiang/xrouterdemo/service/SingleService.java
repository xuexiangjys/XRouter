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
import com.xuexiang.xrouter.facade.template.IProvider;
import com.xuexiang.xutil.tip.ToastUtils;

/**
 * 测试单类注入
 *
 * @author xuexiang
 * @since 2018/5/22 下午1:51
 */
@Router(path = "/service/single")
public class SingleService implements IProvider {

    public void sayHello(String name) {
        ToastUtils.toast("Hello " + name);
    }

    @Override
    public void init(Context context) {
    }
}
