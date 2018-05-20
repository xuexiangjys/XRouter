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

package com.xuexiang.xrouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由创建注解
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:32
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Router {

    /**
     * 路由的路径，必填
     */
    String path();

    /**
     * 路由所在的组
     */
    String group() default "";

    /**
     * 路由的名称
     */
    String name() default "undefined";
    
    /**
     * 路由的拓展属性，这个属性是一个int值，换句话说，单个int有4字节，也就是32位，可以配置32个开关。
     * Ps. U should use the integer num sign the switch, by bits. 10001010101010
     */
    int extras() default Integer.MIN_VALUE;

    /**
     * 路由的优先级
     */
    int priority() default -1;

}
