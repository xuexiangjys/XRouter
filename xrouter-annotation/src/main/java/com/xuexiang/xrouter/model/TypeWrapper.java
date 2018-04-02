package com.xuexiang.xrouter.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Used for get type of target object.
 * @author xuexiang
 * @date 2018/4/1 下午11:44
 */
public class TypeWrapper<T> {
    protected final Type type;

    protected TypeWrapper() {
        Type superClass = getClass().getGenericSuperclass();

        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
