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
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.launcher.XRouter;
import com.xuexiang.xrouterdemo.R;
import com.xuexiang.xrouterdemo.entity.TestObj;
import com.xuexiang.xrouterdemo.entity.TestParcelable;
import com.xuexiang.xrouterdemo.interceptor.HelloService;

import java.util.List;
import java.util.Map;


/**
 * https://m.aliyun.com/test/activity1?name=老王&age=23&boy=true&high=180
 */
@Router(path = "/test/activity1")
public class Test1Activity extends AppCompatActivity {

    @AutoWired(required = true)
    String name = "jack";

    @AutoWired
    int age = 10;

    @AutoWired
    int height = 175;

    @AutoWired(name = "boy")
    boolean girl;

    @AutoWired
    char ch = 'A';

    @AutoWired
    float fl = 12.00f;

    @AutoWired
    double dou = 12.01d;

    @AutoWired
    TestParcelable pac;

    @AutoWired
    TestObj obj;

    @AutoWired
    List<TestObj> objList;

    @AutoWired
    Map<String, List<TestObj>> map;

    private long high;

    @AutoWired
    String url;

    @AutoWired(required = true)
    HelloService helloService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);

        XRouter.getInstance().inject(this);

        // No more getter ...
        // name = getIntent().getStringExtra("name");
        // age = getIntent().getIntExtra("age", 0);
        // girl = getIntent().getBooleanExtra("girl", false);
        // high = getIntent().getLongExtra("high", 0);
        // url = getIntent().getStringExtra("url");

        String params = String.format(
                "name=%s,\n age=%s, \n height=%s,\n girl=%s,\n high=%s,\n url=%s,\n pac=%s,\n obj=%s \n ch=%s \n fl = %s, \n dou = %s, \n objList=%s, \n map=%s",
                name,
                age,
                height,
                girl,
                high,
                url,
                pac,
                obj,
                ch,
                fl,
                dou,
                objList,
                map
        );
        helloService.sayHello("Hello moto.");

        ((TextView) findViewById(R.id.test)).setText("I am " + Test1Activity.class.getName());
        ((TextView) findViewById(R.id.test2)).setText(params);
    }
}
