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

package com.xuexiang.xrouterdemo.kotlin

import android.app.Activity
import android.os.Bundle
import com.xuexiang.xrouter.annotation.AutoWired
import com.xuexiang.xrouter.annotation.Router
import com.xuexiang.xrouter.launcher.XRouter
import kotlinx.android.synthetic.main.activity_kotlin_test.*

@Router(path = "/kotlin/test")
class KotlinTestActivity : Activity() {

    @AutoWired
    @JvmField var name: String? = null
    @AutoWired
    @JvmField var age: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        XRouter.getInstance().inject(this)  // Start auto inject.

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_test)

        content.text = "这里是kotlin页面，传递的内容：" + "name = $name, age = $age"
    }
}
