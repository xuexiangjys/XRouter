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

package com.xuexiang.xrouter.util

import org.gradle.api.Project

/**
 * 日志记录
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:53
 */
class Logger {
    static org.gradle.api.logging.Logger logger

    static void make(Project project) {
        logger = project.getLogger()
    }

    static void i(String info) {
        if (null != info && null != logger) {
            logger.info("XRouter::Register >>> " + info)
        }
    }

    static void e(String error) {
        if (null != error && null != logger) {
            logger.error("XRouter::Register >>> " + error)
        }
    }

    static void w(String warning) {
        if (null != warning && null != logger) {
            logger.warn("XRouter::Register >>> " + warning)
        }
    }
}
