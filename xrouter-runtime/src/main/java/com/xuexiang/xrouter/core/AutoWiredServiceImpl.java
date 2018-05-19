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

import android.content.Context;
import android.util.LruCache;

import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.facade.service.AutoWiredService;
import com.xuexiang.xrouter.facade.template.ISyringe;

import java.util.ArrayList;
import java.util.List;

import static com.xuexiang.xrouter.utils.Consts.ROUTE_SERVICE_AUTOWIRED;
import static com.xuexiang.xrouter.utils.Consts.SUFFIX_AUTOWIRED;

/**
 * 全局自动注入属性服务
 *
 * @author xuexiang
 * @since 2018/5/19 下午9:13
 */
@Router(path = ROUTE_SERVICE_AUTOWIRED)
public class AutoWiredServiceImpl implements AutoWiredService {
    /**
     * 存放自动注入属性的注射器缓存
     */
    private LruCache<String, ISyringe> mClassCache;
    /**
     * 存放不需要自动注入属性的类类名
     */
    private List<String> mBlackList;

    @Override
    public void init(Context context) {
        mClassCache = new LruCache<>(66);
        mBlackList = new ArrayList<>();
    }

    @Override
    public void autoWire(Object instance) {
        String className = instance.getClass().getName();
        try {
            if (!mBlackList.contains(className)) {
                ISyringe autoWiredSyringe = mClassCache.get(className);
                if (autoWiredSyringe == null) {  // No cache.
                    //根据生成规则反射生成APT自动生成的自动依赖注入注射器，如果没有对应的类可生成，证明该类无需自动注入属性
                    autoWiredSyringe = (ISyringe) Class.forName(instance.getClass().getName() + SUFFIX_AUTOWIRED).getConstructor().newInstance();
                }
                autoWiredSyringe.inject(instance);
                mClassCache.put(className, autoWiredSyringe);
            }
        } catch (Exception ex) {
            mBlackList.add(className);    // 反射生成自动依赖注入注射器失败，证明该类无需自动注入属性
        }
    }
}
