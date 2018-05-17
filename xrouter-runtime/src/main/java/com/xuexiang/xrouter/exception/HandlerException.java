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

package com.xuexiang.xrouter.exception;

/**
 * 主流程的处理异常
 *
 * @author xuexiang
 * @since 2018/5/17 下午11:11
 */
public class HandlerException extends RuntimeException {
    /**
     * 构造方法
     * @param detailMessage 主流程处理相关的错误信息
     */
    public HandlerException(String detailMessage) {
        super(detailMessage);
    }
}
