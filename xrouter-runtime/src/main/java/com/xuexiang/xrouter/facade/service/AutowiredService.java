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


import com.xuexiang.xrouter.facade.template.IProvider;

/**
 * 实现自动装配（依赖注入）的服务
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:56
 */
public interface AutowiredService extends IProvider {

    /**
     * 自动装配
     * @param instance 自动装配的目标
     */
    void autowire(Object instance);
}
