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

package com.xuexiang.xrouter.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.xuexiang.xrouter.annotation.Interceptor;
import com.xuexiang.xrouter.compiler.util.Logger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.xuexiang.xrouter.compiler.util.Consts.ANNOTATION_TYPE_INTECEPTOR;
import static com.xuexiang.xrouter.compiler.util.Consts.IINTERCEPTOR;
import static com.xuexiang.xrouter.compiler.util.Consts.IINTERCEPTOR_GROUP;
import static com.xuexiang.xrouter.compiler.util.Consts.KEY_MODULE_NAME;
import static com.xuexiang.xrouter.compiler.util.Consts.METHOD_LOAD_INTO;
import static com.xuexiang.xrouter.compiler.util.Consts.NAME_OF_INTERCEPTOR;
import static com.xuexiang.xrouter.compiler.util.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.xuexiang.xrouter.compiler.util.Consts.PREFIX_OF_LOGGER;
import static com.xuexiang.xrouter.compiler.util.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Process the annotation of {@link Interceptor}
 * <p>自动生成IInterceptor注册接口 XRouter$$Interceptors$$[moduleName] </p>
 *
 * @author xuexiang
 * @since 2018/5/20 上午12:15
 */
@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ANNOTATION_TYPE_INTECEPTOR)
public class InterceptorProcessor extends AbstractProcessor {
    /**
     * 拦截器表【拦截器的优先级 and 拦截器的类】
     */
    private Map<Integer, Element> interceptors = new TreeMap<>();
    /**
     * 写class文件到disk
     */
    private Filer filer;
    /**
     * 日志打印工具
     */
    private Logger logger;
    /**
     * 获取类的工具
     */
    private Elements elements;
    /**
     * 模块名，可以是'app'或者其他
     */
    private String moduleName = null;
    private TypeMirror iInterceptor = null;

    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     *                      provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();                  // Generate class.
        elements = processingEnv.getElementUtils();      // Get class meta.
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        getModuleName(processingEnv);

        iInterceptor = elements.getTypeElement(IINTERCEPTOR).asType();

        logger.info(">>> InterceptorProcessor init. <<<");
    }

    /**
     * 获取用户在annotationProcessorOptions中定义的[moduleName]
     *
     * @param processingEnv
     */
    private void getModuleName(ProcessingEnvironment processingEnv) {
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }
        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error("These no module name, at 'build.gradle', like :\n" +
                    "apt {\n" +
                    "    arguments {\n" +
                    "        moduleName project.getName();\n" +
                    "    }\n" +
                    "}\n");
            throw new RuntimeException(PREFIX_OF_LOGGER + ">>> No module name, for more information, look at gradle log.");
        }
    }

    /**
     * 扫描被{@link Interceptor}注解所修饰的类
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
            try {
                parseInterceptors(elements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    /**
     * 解析路由拦截器注解{@link Interceptor}
     *
     * @param elements elements of Interceptor.
     */
    private void parseInterceptors(Set<? extends Element> elements) throws IOException, IllegalArgumentException {
        if (CollectionUtils.isNotEmpty(elements)) {
            logger.info(">>> Found Interceptors, size is " + elements.size() + " <<<");

            // Verify and cache, sort incidentally.
            for (Element element : elements) {
                if (verifyInterceptor(element)) {  // 验证拦截器类的有效性
                    logger.info("A interceptor verify over, its " + element.asType());
                    Interceptor interceptor = element.getAnnotation(Interceptor.class);

                    Element lastInterceptor = interceptors.get(interceptor.priority());
                    if (lastInterceptor != null) { // 拦截器的优先级不能相同，否则无法存放至TreeMap中，因为Key存放的是拦截器的优先级
                        throw new IllegalArgumentException(
                                String.format(Locale.getDefault(), "More than one interceptors use same priority [%d], They are [%s] and [%s].",
                                        interceptor.priority(),
                                        lastInterceptor.getSimpleName(),
                                        element.getSimpleName())
                        );
                    }

                    interceptors.put(interceptor.priority(), element);
                } else {
                    logger.error("A interceptor verify failed, its " + element.asType());
                }
            }

            // Interface of XRouter.
            TypeElement type_IInterceptor = this.elements.getTypeElement(IINTERCEPTOR);
            TypeElement type_IInterceptorGroup = this.elements.getTypeElement(IINTERCEPTOR_GROUP);

            /*
                Build input type, format as : 存放拦截器的接口类

                ```Map<Integer, Class<? extends IInterceptor>>```
             */
            ParameterizedTypeName inputMapTypeOfIInterceptor = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(Integer.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_IInterceptor))
                    )
            );

            // Build input param name.
            ParameterSpec IInterceptorParamSpec = ParameterSpec.builder(inputMapTypeOfIInterceptor, "interceptors").build();

            /*
              Build method : 'loadInto'
              @Override
              public void loadInto(Map<Integer, Class<? extends IInterceptor>> interceptors) {}
             */
            MethodSpec.Builder loadIntoMethodOfIInterceptorBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(IInterceptorParamSpec);

            // 填充构建XRouter$$Interceptors$$信息，生成对应代码
            if (interceptors != null && interceptors.size() > 0) {
                // Build method body
                for (Map.Entry<Integer, Element> entry : interceptors.entrySet()) {
                    loadIntoMethodOfIInterceptorBuilder.addStatement("interceptors.put(" + entry.getKey() + ", $T.class)", ClassName.get((TypeElement) entry.getValue()));
                }
            }

            // 生成XRouter$$Interceptors$$[moduleName] 拦截器组注册接口类
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(NAME_OF_INTERCEPTOR + moduleName)
                            .addModifiers(PUBLIC)
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadIntoMethodOfIInterceptorBuilder.build())
                            .addSuperinterface(ClassName.get(type_IInterceptorGroup))
                            .build()
            ).build().writeTo(filer);

            logger.info(">>> Interceptor group write over. <<<");
        }
    }

    /**
     * 验证被@Interceptor标注类的有效性
     *
     * @param element Interceptor taw type
     * @return 是否有效
     */
    private boolean verifyInterceptor(Element element) {
        Interceptor interceptor = element.getAnnotation(Interceptor.class);
        //该类必须被 @Interceptor 标注，并且必须实现 IInterceptor 接口
        return interceptor != null && ((TypeElement) element).getInterfaces().contains(iInterceptor);
    }
}
