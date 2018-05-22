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

package com.xuexiang.xrouterdemo.fragment;

import android.util.Log;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.SimpleListFragment;
import com.xuexiang.xrouter.facade.Postcard;
import com.xuexiang.xrouter.facade.callback.NavCallback;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xutil.tip.ToastUtils;

import java.util.List;

/**
 * @author xuexiang
 * @since 2018/5/22 下午2:50
 */
@Page(name = "路由导航测试")
public class NavigationTestFragment extends SimpleListFragment {
    /**
     * 初始化例子
     *
     * @param lists
     * @return
     */
    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("跳转失败，单独降级");
        lists.add("跳转失败，全局降级");
        lists.add("服务调用失败");
        return lists;
    }

    /**
     * 条目点击
     *
     * @param position
     */
    @Override
    protected void onItemClick(int position) {
        switch(position) {
            case 0:
                XRouter.getInstance().build("/xxx/xxx").navigation(getContext(), new NavCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        Log.d("XRouter", "找到了");
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        Log.d("XRouter", "找不到了");
                        ToastUtils.toast("进行局部的降级~~");
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        Log.d("XRouter", "跳转完了");
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        Log.d("XRouter", "被拦截了");
                    }
                });
                break;
            case 1:
                XRouter.getInstance().build("/xxx/xxx").navigation();
                break;
            case 2:
                XRouter.getInstance().navigation(NavigationTestFragment.class);
                break;
            default:
                break;
        }
    }
}
