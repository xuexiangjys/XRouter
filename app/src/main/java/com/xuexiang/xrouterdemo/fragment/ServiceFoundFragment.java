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
import com.xuexiang.xrouterdemo.service.HelloService;
import com.xuexiang.xrouterdemo.service.SingleService;

import java.util.List;

/**
 * @author xuexiang
 * @since 2018/5/22 下午1:43
 */
@Page(name = "服务发现（请先初始化）")
public class ServiceFoundFragment extends SimpleListFragment {
    /**
     * 初始化例子
     *
     * @param lists
     * @return
     */
    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("By Name 调用服务");
        lists.add("By Type 调用服务");
        lists.add("调用单类");
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
                ((HelloService) XRouter.getInstance().build("/service/hello")
                        .navigation())
                        .sayHello("mike~~");
                break;
            case 1:
                XRouter.getInstance().navigation(HelloService.class)
                        .sayHello("mike");
                break;
            case 2:
                XRouter.getInstance().navigation(SingleService.class)
                        .sayHello("Mike");
                break;
            default:
                break;
        }
    }
}
