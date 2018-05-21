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

package com.xuexiang.xrouter.util

import com.xuexiang.xrouter.core.RegisterTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * 扫描 com/xuexiang/xrouter/ 所有的class文件
 * <p>寻找到自动生成的路由注册接口：routers、interceptors、providers</p>
 * <p>接口包括：IRouteRoot、IInterceptorGroup、IProviderGroup</p>
 *
 * @author xuexiang
 * @since 2018/5/21 下午9:44
 */
class ScanUtils {

    /**
     * 扫描jar文件
     * @param jarFile 所有被打包依赖进apk的jar文件
     * @param destFile dest file after this transform
     */
    static void scanJar(File jarFile, File destFile) {
        if (jarFile) {
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)) {
                    InputStream inputStream = file.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                } else if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                    // 标记这个jar文件中是否存在 LogisticsCenter.class -- 需要动态注入注册代码的类
                    // 在扫描完成后,将向 LogisticsCenter.class 的loadRouterMap方法中注入注册代码
                    RegisterTransform.fileContainsInitClass = destFile
                }
            }
            file.close()
        }
    }

    /**
     * 判断jar文件是否可能注册了路由【android的library可以直接排除】
     * @param jarFilepath jar文件的路径
     */
    static boolean shouldProcessPreDexJar(String jarFilepath) {
        return !jarFilepath.contains("com.android.support") && !jarFilepath.contains("/android/m2repository")
    }

    /**
     * 判断扫描的类的包名是否是 annotationProcessor自动生成路由代码的包名：com/xuexiang/xrouter/routes/
     * @param classFilePath 扫描的class文件的路径
     */
    static boolean shouldProcessClass(String classFilePath) {
        return classFilePath != null && classFilePath.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)
    }

    /**
     * 扫描class文件
     * @param file class文件
     */
    static void scanClass(File file) {
        scanClass(new FileInputStream(file))
    }

    /**
     * 扫描class文件
     * @param inputStream 文件流
     */
    static void scanClass(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            RegisterTransform.registerList.each { ext ->
                if (ext.interfaceName && interfaces != null) {
                    interfaces.each { itName ->
                        if (itName == ext.interfaceName) {
                            //搜索所有实现接口是IRouteRoot、IInterceptorGroup、IProviderGroup的类
                            ext.classList.add(name)
                        }
                    }
                }
            }
        }
    }

}