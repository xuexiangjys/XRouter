package com.xuexiang.xrouter.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.annotation.Router;
import com.xuexiang.xrouter.compiler.util.Consts;
import com.xuexiang.xrouter.compiler.util.Logger;
import com.xuexiang.xrouter.compiler.util.TypeUtils;
import com.xuexiang.xrouter.enums.RouteType;
import com.xuexiang.xrouter.model.RouteInfo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import javax.lang.model.util.Types;

import static com.xuexiang.xrouter.compiler.util.Consts.ACTIVITY;
import static com.xuexiang.xrouter.compiler.util.Consts.ANNOTATION_TYPE_AUTOWIRED;
import static com.xuexiang.xrouter.compiler.util.Consts.ANNOTATION_TYPE_ROUTE;
import static com.xuexiang.xrouter.compiler.util.Consts.FRAGMENT;
import static com.xuexiang.xrouter.compiler.util.Consts.IPROVIDER_GROUP;
import static com.xuexiang.xrouter.compiler.util.Consts.IROUTE_GROUP;
import static com.xuexiang.xrouter.compiler.util.Consts.ITROUTE_ROOT;
import static com.xuexiang.xrouter.compiler.util.Consts.KEY_MODULE_NAME;
import static com.xuexiang.xrouter.compiler.util.Consts.METHOD_LOAD_INTO;
import static com.xuexiang.xrouter.compiler.util.Consts.NAME_OF_GROUP;
import static com.xuexiang.xrouter.compiler.util.Consts.NAME_OF_PROVIDER;
import static com.xuexiang.xrouter.compiler.util.Consts.NAME_OF_ROOT;
import static com.xuexiang.xrouter.compiler.util.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.xuexiang.xrouter.compiler.util.Consts.PREFIX_OF_LOGGER;
import static com.xuexiang.xrouter.compiler.util.Consts.SERVICE;
import static com.xuexiang.xrouter.compiler.util.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Process the annotation of {@link Router}
 *
 * <p>自动生成路由组注册接口 XRouter$$Group$$[groupName] </p>
 * <p>自动生成根路由注册接口 XRouter$$Root$$[moduleName] </p>
 * <p>自动生成IProvider注册接口 XRouter$$Providers$$[moduleName] </p>
 *
 * @author xuexiang
 * @since 2018/5/20 上午12:19
 */
@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ANNOTATION_TYPE_ROUTE, ANNOTATION_TYPE_AUTOWIRED})
public class RouterProcessor extends AbstractProcessor {
    /**
     * 组路由表【GroupName and RouteInfos】
     */
    private Map<String, Set<RouteInfo>> groupMap = new HashMap<>();
    /**
     * 根路由表【GroupName and GroupFileName】，用于自动生成路由组注册信息
     */
    private Map<String, String> rootMap = new TreeMap<>();  // Map of root metas, used for generate class file in order.
    /**
     * 写class文件到disk
     */
    private Filer filer;
    /**
     * 日志打印工具
     */
    private Logger logger;
    /**
     * 类型工具
     */
    private Types types;
    /**
     * 获取类的工具
     */
    private Elements elements;
    private TypeUtils typeUtils;
    /**
     * 模块名，可以是'app'或者其他
     */
    private String moduleName = null;   // Module name, maybe
    private TypeMirror iProvider = null;

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
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.

        typeUtils = new TypeUtils(types, elements);
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        getModuleName(processingEnv);

        iProvider = elements.getTypeElement(Consts.IPROVIDER).asType();

        logger.info(">>> RouterProcessor init. <<<");
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
     * 扫描被{@link Router}注解所修饰的类
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> routeElements = roundEnv.getElementsAnnotatedWith(Router.class);
            try {
                logger.info(">>> Found Routers, start... <<<");
                this.parseRoutes(routeElements);

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    /**
     * 解析路由创建注解{@link Router}
     *
     * @param routeElements
     */
    private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            // Perpare the type an so on.
            logger.info(">>> Found routes, size is " + routeElements.size() + " <<<");
            rootMap.clear();

            TypeMirror type_Activity = elements.getTypeElement(ACTIVITY).asType();
            TypeMirror type_Service = elements.getTypeElement(SERVICE).asType();
            TypeMirror type_Fragment = elements.getTypeElement(FRAGMENT).asType();
            TypeMirror type_Fragment_V4 = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

            // Interface of XRouter
            TypeElement type_IRouteGroup = elements.getTypeElement(IROUTE_GROUP);
            TypeElement type_IProviderGroup = elements.getTypeElement(IPROVIDER_GROUP);
            ClassName routeInfoClassName = ClassName.get(RouteInfo.class);
            ClassName routeTypeClassName = ClassName.get(RouteType.class);

            /*
               Build input type, format as :  存放路由组的接口类（生成应用根路由）

               ```Map<String, Class<? extends IRouteGroup>>```
             */
            ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_IRouteGroup))
                    )
            );

            /*
                Build input type, format as : 存放路由表的接口类（生成路由组）
              ```Map<String, RouteInfo>```
             */
            ParameterizedTypeName inputMapTypeOfGroup = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouteInfo.class)
            );

            /*
              Build input param name.
             */
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "routeGroups").build();
            ParameterSpec groupParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "routeInfos").build();
            ParameterSpec providerParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "providers").build();  // Ps. its param type same as groupParamSpec!

            /*
              Build method : 'loadInto'
              @Override
              public void loadInto(Map<String, Class<? extends IRouteGroup>> routeGroups) {}
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);

            //扫描所有被@Router注解的类，构建RouteInfo，然后将他们进行分组，填充groupMap
            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                Router router = element.getAnnotation(Router.class);
                RouteInfo routeInfo = null;

                if (types.isSubtype(tm, type_Activity)) {                 // Activity
                    logger.info(">>> Found activity router: " + tm.toString() + " <<<");

                    // Get all fields annotation by @AutoWired
                    Map<String, Integer> paramsType = new HashMap<>();
                    for (Element field : element.getEnclosedElements()) {
                        if (field.getKind().isField() && field.getAnnotation(AutoWired.class) != null && !types.isSubtype(field.asType(), iProvider)) {
                            // It must be field, then it has annotation, but it not be provider.
                            AutoWired paramConfig = field.getAnnotation(AutoWired.class);
                            paramsType.put(StringUtils.isEmpty(paramConfig.name()) ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeExchange(field));
                        }
                    }
                    routeInfo = new RouteInfo(router, element, RouteType.ACTIVITY, paramsType);
                } else if (types.isSubtype(tm, iProvider)) {         // IProvider
                    logger.info(">>> Found provider router: " + tm.toString() + " <<<");
                    routeInfo = new RouteInfo(router, element, RouteType.PROVIDER, null);
                } else if (types.isSubtype(tm, type_Service)) {           // Service
                    logger.info(">>> Found service router: " + tm.toString() + " <<<");
                    routeInfo = new RouteInfo(router, element, RouteType.SERVICE, null);
                } else if (types.isSubtype(tm, type_Fragment) || types.isSubtype(tm, type_Fragment_V4)) {
                    logger.info(">>> Found fragment router: " + tm.toString() + " <<<");
                    routeInfo = new RouteInfo(router, element, RouteType.FRAGMENT, null);
                } else {
                    throw new RuntimeException(PREFIX_OF_LOGGER + ">>> Found unsupported class type, type = [" + types.toString() + "].");
                }

                categories(routeInfo);
                // if (StringUtils.isEmpty(moduleName)) {   // Hasn't generate the module name.
                //     moduleName = ModuleUtils.generateModuleName(element, logger);
                // }
            }

            /*
              Build method : 'loadInto'
              @Override
              public void loadInto(Map<String, RouteInfo> providers) {}
             */
            MethodSpec.Builder loadIntoMethodOfProviderBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(providerParamSpec);

            // 开始自动生成路由信息注册代码
            for (Map.Entry<String, Set<RouteInfo>> entry : groupMap.entrySet()) {
                String groupName = entry.getKey();

                /*
                  Build method : 'loadInto'
                  @Override
                  public void loadInto(Map<String, RouteInfo> routeInfos) {}
                 */
                MethodSpec.Builder loadIntoMethodOfGroupBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(groupParamSpec);

                // 填充构建XRouter$$Providers$$信息，生成对应代码
                Set<RouteInfo> groupData = entry.getValue();
                for (RouteInfo routeInfo : groupData) {
                    switch (routeInfo.getType()) {
                        case PROVIDER:  // Need cache provider's super class
                            List<? extends TypeMirror> interfaces = ((TypeElement) routeInfo.getRawType()).getInterfaces();
                            for (TypeMirror tm : interfaces) {
                                if (types.isSameType(tm, iProvider)) {
                                    // Its implements iProvider interface himself.（IProvider的实现类，就将该类的类名作为Key）
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + routeInfo.getType() + ", $T.class, $S, $S, null, " + routeInfo.getPriority() + ", " + routeInfo.getExtra() + "))",
                                            (routeInfo.getRawType()).toString(),
                                            routeInfoClassName,
                                            routeTypeClassName,
                                            ClassName.get((TypeElement) routeInfo.getRawType()),
                                            routeInfo.getPath(),
                                            routeInfo.getGroup());
                                } else if (types.isSubtype(tm, iProvider)) { //（接口是IProvider的继承类，就将该接口类的类名作为Key）
                                    // This interface extend the IProvider, so it can be used for mark provider
                                    loadIntoMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + routeInfo.getType() + ", $T.class, $S, $S, null, " + routeInfo.getPriority() + ", " + routeInfo.getExtra() + "))",
                                            tm.toString(),    // So stupid, will duplicate only save class name.
                                            routeInfoClassName,
                                            routeTypeClassName,
                                            ClassName.get((TypeElement) routeInfo.getRawType()),
                                            routeInfo.getPath(),
                                            routeInfo.getGroup());
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    // 填充构建XRouter$$Group$$信息，生成对应代码
                    StringBuilder mapBodyBuilder = new StringBuilder();
                    Map<String, Integer> paramsType = routeInfo.getParamsType();
                    if (MapUtils.isNotEmpty(paramsType)) {
                        for (Map.Entry<String, Integer> types : paramsType.entrySet()) {
                            mapBodyBuilder.append("put(\"").append(types.getKey()).append("\", ").append(types.getValue()).append("); ");
                        }
                    }
                    String mapBody = mapBodyBuilder.toString();

                    loadIntoMethodOfGroupBuilder.addStatement(
                            "routeInfos.put($S, $T.build($T." + routeInfo.getType() + ", $T.class, $S, $S, " + (StringUtils.isEmpty(mapBody) ? null : ("new java.util.HashMap<String, Integer>(){{" + mapBodyBuilder.toString() + "}}")) + ", " + routeInfo.getPriority() + ", " + routeInfo.getExtra() + "))",
                            routeInfo.getPath(),
                            routeInfoClassName,
                            routeTypeClassName,
                            ClassName.get((TypeElement) routeInfo.getRawType()),
                            routeInfo.getPath().toLowerCase(),
                            routeInfo.getGroup().toLowerCase());
                }

                // 生成XRouter$$Group$$[groupName] 路由注册接口类
                String groupFileName = NAME_OF_GROUP + groupName;
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(groupFileName)
                                .addJavadoc(WARNING_TIPS)
                                .addSuperinterface(ClassName.get(type_IRouteGroup))
                                .addModifiers(PUBLIC)
                                .addMethod(loadIntoMethodOfGroupBuilder.build())
                                .build()
                ).build().writeTo(filer);

                logger.info(">>> Generated group map, name is : " + groupName + "<<<");
                rootMap.put(groupName, groupFileName); //注册Group的信息，为下面构建根路由提供数据
            }

            if (MapUtils.isNotEmpty(rootMap)) {
                // 根据Group的信息，填充构建XRouter$$Root$$信息，生成对应代码
                for (Map.Entry<String, String> entry : rootMap.entrySet()) {
                    loadIntoMethodOfRootBuilder.addStatement("routeGroups.put($S, $T.class)",
                            entry.getKey(),  //路由组名
                            ClassName.get(PACKAGE_OF_GENERATE_FILE, entry.getValue())); //路由组注册接口的类名
                }
            }

            // 生成XRouter$$Providers$$[moduleName] provider注册接口类
            String providerMapFileName = NAME_OF_PROVIDER + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(providerMapFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(type_IProviderGroup))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfProviderBuilder.build())
                            .build()
            ).build().writeTo(filer);

            logger.info(">>> Generated provider map, name is : " + providerMapFileName + " <<<");

            // 生成XRouter$$Root$$[moduleName] 根路由注册接口类
            String rootFileName = NAME_OF_ROOT + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(rootFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(elements.getTypeElement(ITROUTE_ROOT)))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfRootBuilder.build())
                            .build()
            ).build().writeTo(filer);

            logger.info(">>> Generated root map, name is : " + rootFileName + " <<<");
        }
    }

    /**
     * 进行路由分组
     *
     * @param routeInfo 路由信息.
     */
    private void categories(RouteInfo routeInfo) {
        if (routeVerify(routeInfo)) {
            logger.info(">>> Start categories, group = " + routeInfo.getGroup() + ", path = " + routeInfo.getPath() + " <<<");
            Set<RouteInfo> routeInfos = groupMap.get(routeInfo.getGroup());
            if (CollectionUtils.isEmpty(routeInfos)) { //路由组的第一个路由，初始化路由集合
                Set<RouteInfo> routeInfoSet = new TreeSet<>(new Comparator<RouteInfo>() {
                    @Override
                    public int compare(RouteInfo r1, RouteInfo r2) {
                        try {
                            return r1.getPath().compareTo(r2.getPath());
                        } catch (NullPointerException npe) {
                            logger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                routeInfoSet.add(routeInfo);
                groupMap.put(routeInfo.getGroup(), routeInfoSet);
            } else {  //如果已经有该路由组，直接添加该路由
                routeInfos.add(routeInfo);
            }
        } else {
            logger.warning(">>> RouteInfo verify error, group is : " + routeInfo.getGroup() + " <<<");
        }
    }

    /**
     * 验证路由信息的有效性（没有填写路由组，就默认截取路由路径中的第一位"/"之前的字符作为组名
     *
     * @param info 路由信息
     */
    private boolean routeVerify(RouteInfo info) {
        String path = info.getPath();

        if (StringUtils.isEmpty(path) || !path.startsWith("/")) {   // The path must be start with '/' and not empty!
            return false;
        }

        if (StringUtils.isEmpty(info.getGroup())) { // Use default group(the first word in path)
            try {
                String defaultGroup = path.substring(1, path.indexOf("/", 1));
                if (StringUtils.isEmpty(defaultGroup)) {
                    return false;
                }

                info.setGroup(defaultGroup);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extract default group! " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}
