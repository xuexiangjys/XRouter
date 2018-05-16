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

import java.util.TreeMap;

/**
 * 存放唯一KEY的TreeMap
 *
 * @author xuexiang
 * @since 2018/5/16 下午11:41
 */
public class UniqueKeyTreeMap<K, V> extends TreeMap<K, V> {
    private String tipText;

    public UniqueKeyTreeMap(String exceptionText) {
        super();
        tipText = exceptionText;
    }

    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw new RuntimeException(String.format(tipText, key));
        } else {
            return super.put(key, value);
        }
    }
}
