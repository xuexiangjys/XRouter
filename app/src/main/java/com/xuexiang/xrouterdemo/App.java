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

package com.xuexiang.xrouterdemo;

import android.app.Application;

import com.xuexiang.xpage.PageConfig;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xutil.XUtil;

/**
 * @author xuexiang
 * @since 2018/5/21 下午10:57
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        XUtil.init(this);

        initXRouter();

        initXPage();
        
    }

    private void initXPage() {
        PageConfig.getInstance().debug("PageLog").init(this);
    }

    private void initXRouter() {
        if (isDebug()) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            XRouter.openLog();     // 打印日志
            XRouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        XRouter.init(this);
    }

    private boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
