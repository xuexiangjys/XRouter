package com.xuexiang.xrouter.model;


import com.xuexiang.xrouter.annotation.Route;
import com.xuexiang.xrouter.enums.RouteType;

import java.util.Map;

import javax.lang.model.element.Element;

/**
 * 路由信息
 *
 * @author xuexiang
 * @date 2018/4/1 下午11:40
 */
public class RouteInfo {
    private RouteType type;         // Type of route
    private Element rawType;        // Raw type of route
    private Class<?> destination;   // Destination
    private String path;            // Path of route
    private String group;           // Group of route
    private int priority = -1;      // The smaller the number, the higher the priority
    private int extra;              // Extra data
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
     * @param route       route
     * @param destination destination
     * @param type        type
     */
    public RouteInfo(Route route, Class<?> destination, RouteType type) {
        this(type, null, destination, route.path(), route.group(), null, route.extras());
    }

    /**
     * Type
     *
     * @param route      route
     * @param rawType    rawType
     * @param type       type
     * @param paramsType paramsType
     */
    public RouteInfo(Route route, Element rawType, RouteType type, Map<String, Integer> paramsType) {
        this(type, rawType, null, route.path(), route.group(), paramsType, route.extras());
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