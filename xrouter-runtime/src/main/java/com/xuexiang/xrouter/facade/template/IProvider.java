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

import android.content.Context;

/**
 * 对外提供接口的基类接口
 *
 * @author xuexiang
 * @since 2018/5/16 下午11:51
 */
public interface IProvider {

    /**
     * 进程初始化的方法
     *
     * @param context 上下文
     */
    void init(Context context);
}
