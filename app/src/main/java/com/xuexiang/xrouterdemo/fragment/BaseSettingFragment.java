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
 * @since 2018/5/21 下午11:39
 */
@Page(name = "基础设置")
public class BaseSettingFragment extends SimpleListFragment {
    /**
     * 初始化例子
     *
     * @param lists
     * @return
     */
    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("打开日志");
        lists.add("开启调试模式(InstantRun需要开启)");
        lists.add("初始化XRouter");
        lists.add("关闭XRouter");
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
                XRouter.openLog();
                break;
            case 1:
                XRouter.openDebug();
                break;
            case 2:
                // 调试模式不是必须开启，但是为了防止有用户开启了InstantRun，但是
                // 忘了开调试模式，导致无法使用Demo，如果使用了InstantRun，必须在
                // 初始化之前开启调试模式，但是上线前需要关闭，InstantRun仅用于开
                // 发阶段，线上开启调试模式有安全风险，可以使用BuildConfig.DEBUG
                // 来区分环境
                XRouter.openDebug();
                XRouter.init(getActivity().getApplication());
                break;
            case 3:
                XRouter.getInstance().destroy();
                break;
            default:
                break;
        }
    }
}
