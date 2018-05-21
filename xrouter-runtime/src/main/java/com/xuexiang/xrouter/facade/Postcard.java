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

package com.xuexiang.xrouter.facade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.xuexiang.xrouter.facade.callback.NavigationCallback;
import com.xuexiang.xrouter.facade.service.SerializationService;
import com.xuexiang.xrouter.facade.template.IProvider;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xrouter.model.RouteInfo;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * 路由路标的容器，存放了路由信息、数据内容、切换动画等
 *
 * @author xuexiang
 * @since 2018/5/17 上午1:07
 */
public final class Postcard extends RouteInfo {
    /**
     * 统一资源标识符
     */
    private Uri uri;
    /**
     * 暂时存放出错的信息
     */
    private Object tag;
    /**
     * 路由携带的数据
     */
    private Bundle mBundle;
    /**
     * 路由的flags
     */
    private int flags = -1;
    /**
     * 路由超时时间（包括拦截器执行的时间，单位: 秒）
     */
    private int timeout = 300;      // Navigation timeout, TimeUnit.Second
    /**
     * RouteType = PROVIDER 时，将会被赋值
     */
    private IProvider provider;
    /**
     * 绿色通道，跳过所有的拦截器
     */
    private boolean greenChannel;
    /**
     * 序列化服务
     */
    private SerializationService serializationService;

    /**
     * activity的切换动画
     */
    private Bundle optionsCompat;
    /**
     * 进入动画
     */
    private int enterAnim = -1;
    /**
     * 退出动画
     */
    private int exitAnim = -1;

    public Postcard() {
        this(null, null);
    }

    public Postcard(String path, String group) {
        this(path, group, null, null);
    }

    public Postcard(String path, String group, Uri uri, Bundle bundle) {
        setPath(path);
        setGroup(group);
        setUri(uri);
        this.mBundle = (null == bundle ? new Bundle() : bundle);
    }

    /**
     * 设置错误信息
     *
     * @param tag
     * @return
     */
    public Postcard setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public Uri getUri() {
        return uri;
    }

    public Bundle getExtras() {
        return mBundle;
    }

    public Object getTag() {
        return tag;
    }

    public int getTimeout() {
        return timeout;
    }

    public IProvider getProvider() {
        return provider;
    }

    public boolean isGreenChannel() {
        return greenChannel;
    }

    public Bundle getOptionsBundle() {
        return optionsCompat;
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public Postcard setProvider(IProvider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * 设置导航的超时时间
     *
     * @param timeout timeout
     * @return this
     */
    public Postcard setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置统一资源标识符
     *
     * @param uri
     * @return
     */
    public Postcard setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    /**
     * 路由导航
     */
    public Object navigation() {
        return navigation(null);
    }

    /**
     * 路由导航
     *
     * @param context Activity and so on.
     */
    public Object navigation(Context context) {
        return navigation(context, null);
    }

    /**
     * 路由导航
     *
     * @param context Activity and so on.
     */
    public Object navigation(Context context, NavigationCallback callback) {
        return XRouter.getInstance().navigation(context, this, -1, callback);
    }

    /**
     * 路由导航（startActivityForResult）
     *
     * @param context     Activity and so on.
     * @param requestCode startActivityForResult's param
     */
    public void navigation(Activity context, int requestCode) {
        navigation(context, requestCode, null);
    }

    /**
     * 路由导航（startActivityForResult）
     *
     * @param context     Activity and so on.
     * @param requestCode startActivityForResult's param
     */
    public void navigation(Activity context, int requestCode, NavigationCallback callback) {
        XRouter.getInstance().navigation(context, this, requestCode, callback);
    }

    /**
     * 路由导航（startActivityForResult）
     *
     * @param fragment     fragment
     * @param requestCode startActivityForResult's param
     */
    public void navigation(Fragment fragment, int requestCode) {
        navigation(fragment, requestCode, null);
    }

    /**
     * 路由导航（startActivityForResult）
     *
     * @param fragment    fragment
     * @param requestCode startActivityForResult's param
     */
    public void navigation(Fragment fragment, int requestCode, NavigationCallback callback) {
        XRouter.getInstance().navigation(fragment, this, requestCode, callback);
    }

    /**
     * 设置绿色通道，跳过所有的拦截器
     *
     * @return this
     */
    public Postcard greenChannel() {
        this.greenChannel = true;
        return this;
    }

    /**
     * 设置Bundle
     */
    public Postcard with(Bundle bundle) {
        if (bundle != null) {
            mBundle = bundle;
        }
        return this;
    }

    @IntDef({
            Intent.FLAG_ACTIVITY_SINGLE_TOP,
            Intent.FLAG_ACTIVITY_NEW_TASK,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            Intent.FLAG_DEBUG_LOG_RESOLUTION,
            Intent.FLAG_FROM_BACKGROUND,
            Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT,
            Intent.FLAG_ACTIVITY_CLEAR_TASK,
            Intent.FLAG_ACTIVITY_CLEAR_TOP,
            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
            Intent.FLAG_ACTIVITY_FORWARD_RESULT,
            Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY,
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
            Intent.FLAG_ACTIVITY_NO_ANIMATION,
            Intent.FLAG_ACTIVITY_NO_USER_ACTION,
            Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP,
            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            Intent.FLAG_ACTIVITY_TASK_ON_HOME,
            Intent.FLAG_RECEIVER_REGISTERED_ONLY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlagInt {
    }

    /**
     * Set special flags controlling how this intent is handled.  Most values
     * here depend on the type of component being executed by the Intent,
     * specifically the FLAG_ACTIVITY_* flags are all for use with
     * {@link Context#startActivity Context.startActivity()} and the
     * FLAG_RECEIVER_* flags are all for use with
     * {@link Context#sendBroadcast(Intent) Context.sendBroadcast()}.
     */
    public Postcard withFlags(@FlagInt int flag) {
        this.flags = flag;
        return this;
    }

    public int getFlags() {
        return flags;
    }

    /**
     * 设置Object参数【使用前需要设置序列化服务 {@link SerializationService}】
     *
     * @param key   a String, or null
     * @param value a Object, or null
     * @return current
     */
    public Postcard withObject(@Nullable String key, @Nullable Object value) {
        serializationService = XRouter.getInstance().navigation(SerializationService.class);
        mBundle.putString(key, serializationService.object2Json(value));
        return this;
    }

    // Follow api copy from #{Bundle}

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a String, or null
     * @return current
     */
    public Postcard withString(@Nullable String key, @Nullable String value) {
        mBundle.putString(key, value);
        return this;
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a boolean
     * @return current
     */
    public Postcard withBoolean(@Nullable String key, boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a short
     * @return current
     */
    public Postcard withShort(@Nullable String key, short value) {
        mBundle.putShort(key, value);
        return this;
    }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value an int
     * @return current
     */
    public Postcard withInt(@Nullable String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a long
     * @return current
     */
    public Postcard withLong(@Nullable String key, long value) {
        mBundle.putLong(key, value);
        return this;
    }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a double
     * @return current
     */
    public Postcard withDouble(@Nullable String key, double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a byte
     * @return current
     */
    public Postcard withByte(@Nullable String key, byte value) {
        mBundle.putByte(key, value);
        return this;
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a char
     * @return current
     */
    public Postcard withChar(@Nullable String key, char value) {
        mBundle.putChar(key, value);
        return this;
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a float
     * @return current
     */
    public Postcard withFloat(@Nullable String key, float value) {
        mBundle.putFloat(key, value);
        return this;
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence, or null
     * @return current
     */
    public Postcard withCharSequence(@Nullable String key, @Nullable CharSequence value) {
        mBundle.putCharSequence(key, value);
        return this;
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Parcelable object, or null
     * @return current
     */
    public Postcard withParcelable(@Nullable String key, @Nullable Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key   a String, or null
     * @param value an array of Parcelable objects, or null
     * @return current
     */
    public Postcard withParcelableArray(@Nullable String key, @Nullable Parcelable[] value) {
        mBundle.putParcelableArray(key, value);
        return this;
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList of Parcelable objects, or null
     * @return current
     */
    public Postcard withParcelableArrayList(@Nullable String key, @Nullable ArrayList<? extends Parcelable> value) {
        mBundle.putParcelableArrayList(key, value);
        return this;
    }

    /**
     * Inserts a SparceArray of Parcelable values into the mapping of this
     * Bundle, replacing any existing value for the given key.  Either key
     * or value may be null.
     *
     * @param key   a String, or null
     * @param value a SparseArray of Parcelable objects, or null
     * @return current
     */
    public Postcard withSparseParcelableArray(@Nullable String key, @Nullable SparseArray<? extends Parcelable> value) {
        mBundle.putSparseParcelableArray(key, value);
        return this;
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public Postcard withIntegerArrayList(@Nullable String key, @Nullable ArrayList<Integer> value) {
        mBundle.putIntegerArrayList(key, value);
        return this;
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public Postcard withStringArrayList(@Nullable String key, @Nullable ArrayList<String> value) {
        mBundle.putStringArrayList(key, value);
        return this;
    }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     * @return current
     */
    public Postcard withCharSequenceArrayList(@Nullable String key, @Nullable ArrayList<CharSequence> value) {
        mBundle.putCharSequenceArrayList(key, value);
        return this;
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Serializable object, or null
     * @return current
     */
    public Postcard withSerializable(@Nullable String key, @Nullable Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a byte array object, or null
     * @return current
     */
    public Postcard withByteArray(@Nullable String key, @Nullable byte[] value) {
        mBundle.putByteArray(key, value);
        return this;
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a short array object, or null
     * @return current
     */
    public Postcard withShortArray(@Nullable String key, @Nullable short[] value) {
        mBundle.putShortArray(key, value);
        return this;
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a char array object, or null
     * @return current
     */
    public Postcard withCharArray(@Nullable String key, @Nullable char[] value) {
        mBundle.putCharArray(key, value);
        return this;
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a float array object, or null
     * @return current
     */
    public Postcard withFloatArray(@Nullable String key, @Nullable float[] value) {
        mBundle.putFloatArray(key, value);
        return this;
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence array object, or null
     * @return current
     */
    public Postcard withCharSequenceArray(@Nullable String key, @Nullable CharSequence[] value) {
        mBundle.putCharSequenceArray(key, value);
        return this;
    }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Bundle object, or null
     * @return current
     */
    public Postcard withBundle(@Nullable String key, @Nullable Bundle value) {
        mBundle.putBundle(key, value);
        return this;
    }

    /**
     * 设置切换动画
     *
     * @param enterAnim 进入的动画
     * @param exitAnim  退出的动画
     * @return current
     */
    public Postcard withTransition(int enterAnim, int exitAnim) {
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        return this;
    }

    /**
     * Set options compat
     *
     * @param compat compat
     * @return this
     */
    @RequiresApi(16)
    public Postcard withOptionsCompat(ActivityOptionsCompat compat) {
        if (null != compat) {
            this.optionsCompat = compat.toBundle();
        }
        return this;
    }

    @Override
    public String toString() {
        return "Postcard{" +
                "uri=" + uri +
                ", tag=" + tag +
                ", mBundle=" + mBundle +
                ", flags=" + flags +
                ", timeout=" + timeout +
                ", provider=" + provider +
                ", greenChannel=" + greenChannel +
                ", optionsCompat=" + optionsCompat +
                ", enterAnim=" + enterAnim +
                ", exitAnim=" + exitAnim +
                "}\n" +
                super.toString();
    }
}
