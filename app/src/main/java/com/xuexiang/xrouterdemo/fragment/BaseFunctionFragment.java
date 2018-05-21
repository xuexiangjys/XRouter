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

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.SimpleListFragment;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xrouterdemo.R;
import com.xuexiang.xutil.tip.ToastUtils;

import java.util.List;

/**
 * @author xuexiang
 * @since 2018/5/22 上午12:45
 */
@Page(name = "基础功能（请先初始化)")
public class BaseFunctionFragment extends SimpleListFragment {
    /**
     * 初始化例子
     *
     * @param lists
     * @return
     */
    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("简单的应用内跳转");
        lists.add("跳转到kotlin页面");
        lists.add("跳转ForResult");
        lists.add("获取Fragment实例");
        lists.add("携带参数的应用内跳转");
        lists.add("旧版本转场动画");
        lists.add("新版本转场动画");
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
            case 0:  //简单的应用内跳转
                XRouter.getInstance().build("/test/activity2").navigation();
                break;
            case 1:  //跳转到kotlin页面
                XRouter.getInstance()
                        .build("/kotlin/test")
                        .withString("name", "xuexiang")
                        .withInt("age", 24)
                        .navigation();
                break;
            case 2:  //跳转ForResult
                XRouter.getInstance()
                        .build("/test/activity2")
                        .navigation(this, 666);
                break;
            case 3:  //获取Fragment实例
                Fragment fragment = (Fragment) XRouter.getInstance().build("/test/fragment").navigation();
                ToastUtils.toast("找到Fragment:" + fragment.toString());
                break;
            case 4:  //携带参数的应用内跳转
                Uri testUriMix = Uri.parse("xrouter://xuexiangjys.github.io/test/activity2");
                XRouter.getInstance().build(testUriMix)
                        .withString("key1", "value1")
                        .navigation();
                break;
            case 5:  //旧版本转场动画
                XRouter.getInstance()
                        .build("/test/activity2")
                        .withTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                        .navigation(getActivity());
                break;
            case 6:  //新版本转场动画
                if (Build.VERSION.SDK_INT >= 16) {
                    ActivityOptionsCompat compat = ActivityOptionsCompat.
                            makeScaleUpAnimation(getListView(), getListView().getWidth() / 2, getListView().getHeight() / 2, 0, 0);

                    XRouter.getInstance()
                            .build("/test/activity2")
                            .withOptionsCompat(compat)
                            .navigation();
                } else {
                    ToastUtils.toast("API < 16,不支持新版本动画");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ToastUtils.toast("onActivityResult， requestCode：" + requestCode);
    }

}
