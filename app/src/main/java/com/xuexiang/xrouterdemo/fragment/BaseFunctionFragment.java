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

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.SimpleListFragment;
import com.xuexiang.xrouter.launcher.XRouter;

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

                break;
            case 2:  //跳转ForResult

                break;
            case 3:  //获取Fragment实例

                break;
            case 4:  //携带参数的应用内跳转

                break;
            case 5:  //旧版本转场动画

                break;
            case 6:  //新版本转场动画

                break;
            default:
                break;
        }
    }
}
