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
 * 实现自动装配（依赖注入）的注解
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:15
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface AutoWired {

    /**
     * @return 参数的字段名／服务名, 默认是字段的参数名
     */
    String name() default "";

    /**
     * @return 是否是非空字段，If required, app will be crash when value is null.
     */
    boolean required() default false;

    /**
     * @return 字段的描述
     */
    String desc() default "No desc.";
}
