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

package com.xuexiang.xrouterdemo.activity.test;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xrouterdemo.R;

/**
 * 自动注入的测试用例
 */
@Router(path = "/test/activity3")
public class Test3Activity extends AppCompatActivity {

    @AutoWired
    String name;

    @AutoWired
    int age;

    @AutoWired(name = "boy")
    boolean girl;

    // 这个字段没有注解，是不会自动注入的
    private long high;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);
        XRouter.getInstance().inject(this);

        String params = String.format("name=%s, age=%s, girl=%s, high=%s", name, age, girl, high);

        ((TextView)findViewById(R.id.test)).setText("I am " + Test3Activity.class.getName());
        ((TextView)findViewById(R.id.test2)).setText(params);
    }
}
