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

package com.xuexiang.xrouter.model;


import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.enums.RouteType;

import java.util.Map;

import javax.lang.model.element.Element;

/**
 * 路由信息
 *
 * @author xuexiang
 * @since 2018/5/17 上午12:32
 */
public class RouteInfo {
    /**
     * 路由的类型
     */
    private RouteType type;
    /**
     *
     */
    private Element rawType;        // Raw type of route
    /**
     * 路由描述信息
     */
    private Class<?> destination;
    /**
     * 路由路径
     */
    private String path;
    /**
     * 路由所在的组
     */
    private String group;
    /**
     * 路由的优先级【数字越小，优先级越高】
     */
    private int priority = -1;
    /**
     * 拓展属性
     */
    private int extra;              // Extra data
    /**
     * 参数类型
     */
    private Map<String, Integer> paramsType;  // Param type

    public RouteInfo() {
    }

    /**
     * For versions of 'compiler' less than 1.0.7, contain 1.0.7
     *
     * @param type        type
     * @param destination destination
     * @param path        path
     * @param group       group
     * @param extra       extra
     * @return this
     */
    public static RouteInfo build(RouteType type, Class<?> destination, String path, String group, int extra) {
        return new RouteInfo(type, null, destination, path, group, null, extra);
    }

    /**
     * For versions of 'compiler' greater than 1.0.7
     *
     * @param type        type
     * @param destination destination
     * @param path        path
     * @param group       group
     * @param paramsType  paramsType
     * @param extra       extra
     * @return this
     */
    public static RouteInfo build(RouteType type, Class<?> destination, String path, String group, Map<String, Integer> paramsType, int extra) {
        return new RouteInfo(type, null, destination, path, group, paramsType, extra);
    }

    /**
     * Type
     *
     * @param router       router
     * @param destination destination
     * @param type        type
     */
    public RouteInfo(Router router, Class<?> destination, RouteType type) {
        this(type, null, destination, router.path(), router.group(), null, router.extras());
    }

    /**
     * Type
     *
     * @param router      router
     * @param rawType    rawType
     * @param type       type
     * @param paramsType paramsType
     */
    public RouteInfo(Router router, Element rawType, RouteType type, Map<String, Integer> paramsType) {
        this(type, rawType, null, router.path(), router.group(), paramsType, router.extras());
    }

    /**
     * Type
     *
     * @param type        type
     * @param rawType     rawType
     * @param destination destination
     * @param path        path
     * @param group       group
     * @param paramsType  paramsType
     * @param extra       extra
     */
    public RouteInfo(RouteType type, Element rawType, Class<?> destination, String path, String group, Map<String, Integer> paramsType, int extra) {
        this.type = type;
        this.destination = destination;
        this.rawType = rawType;
        this.path = path;
        this.group = group;
        this.paramsType = paramsType;
        this.extra = extra;
    }

    public Map<String, Integer> getParamsType() {
        return paramsType;
    }

    public RouteInfo setParamsType(Map<String, Integer> paramsType) {
        this.paramsType = paramsType;
        return this;
    }

    public Element getRawType() {
        return rawType;
    }

    public RouteInfo setRawType(Element rawType) {
        this.rawType = rawType;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public RouteInfo setType(RouteType type) {
        this.type = type;
        return this;
    }

    public Class<?> getDestination() {
        return destination;
    }

    public RouteInfo setDestination(Class<?> destination) {
        this.destination = destination;
        return this;
    }

    public String getPath() {
        return path;
    }

    public RouteInfo setPath(String path) {
        this.path = path;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public RouteInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public RouteInfo setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getExtra() {
        return extra;
    }

    public RouteInfo setExtra(int extra) {
        this.extra = extra;
        return this;
    }

    @Override
    public String toString() {
        return "RouteInfo{" +
                "type=" + type +
                ", rawType=" + rawType +
                ", destination=" + destination +
                ", path='" + path + '\'' +
                ", group='" + group + '\'' +
                ", priority=" + priority +
                ", extra=" + extra +
                '}';
    }
}