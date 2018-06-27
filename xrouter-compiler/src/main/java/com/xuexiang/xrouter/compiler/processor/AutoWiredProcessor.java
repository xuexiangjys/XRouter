package com.xuexiang.xrouter.compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.compiler.util.Consts;
import com.xuexiang.xrouter.compiler.util.Logger;
import com.xuexiang.xrouter.compiler.util.TypeUtils;
import com.xuexiang.xrouter.enums.TypeKind;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.xuexiang.xrouter.compiler.util.Consts.ANNOTATION_TYPE_AUTOWIRED;
import static com.xuexiang.xrouter.compiler.util.Consts.ISYRINGE;
import static com.xuexiang.xrouter.compiler.util.Consts.SERIALIZATION_SERVICE;
import static com.xuexiang.xrouter.compiler.util.Consts.KEY_MODULE_NAME;
import static com.xuexiang.xrouter.compiler.util.Consts.METHOD_INJECT;
import static com.xuexiang.xrouter.compiler.util.Consts.NAME_OF_AUTOWIRED;
import static com.xuexiang.xrouter.compiler.util.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Process the annotation of {@link AutoWired}
 *
 * <p>自动生成依赖注入的辅助类 [ClassName]$$XRouter$$AutoWired </p>
 *
 * @author xuexiang
 * @since 2018/5/20 上午12:02
 */
@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ANNOTATION_TYPE_AUTOWIRED})
public class AutoWiredProcessor extends AbstractProcessor {
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
    private TypeUtils typeUtils;
    /**
     * 获取类的工具
     */
    private Elements elements;
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();   // Contain field need autowired and his super class.
    private static final ClassName XRouterClassName = ClassName.get("com.xuexiang.xrouter.launcher", "XRouter");
    private static final ClassName XRLogClassName = ClassName.get("com.xuexiang.xrouter.logs", "XRLog");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnv.getFiler();                  // Generate class.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.

        typeUtils = new TypeUtils(types, elements);

        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        logger.info(">>> AutoWiredProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isNotEmpty(set)) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AutoWired.class);
            try {
                parseAutoWireds(elements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }

    /**
     * 解析自动依赖注入的注解{@link AutoWired}
     *
     * @param elements 被@AutoWired修饰的字段
     */
    private void parseAutoWireds(Set<? extends Element> elements) throws IOException, IllegalAccessException {
        logger.info(">>> Found AutoWired fields, start... <<<");
        categories(elements); //对AutoWired字段按所在包装类的类名进行分类
        generateInjectCode(); //生成对应的依赖注入代码
    }

    /**
     * 生成依赖注入的代码
     *
     * @throws IOException
     * @throws IllegalAccessException
     */
    private void generateInjectCode() throws IOException, IllegalAccessException {
        TypeElement type_ISyringe = elements.getTypeElement(ISYRINGE);
        TypeMirror type_SerializationService = elements.getTypeElement(SERIALIZATION_SERVICE).asType();
        TypeMirror type_IProvider = elements.getTypeElement(Consts.IPROVIDER).asType();
        TypeMirror type_Activity = elements.getTypeElement(Consts.ACTIVITY).asType();
        TypeMirror type_Fragment = elements.getTypeElement(Consts.FRAGMENT).asType();
        TypeMirror type_Fragment_V4 = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

        /* Build input param name.
            Object target
        */
        ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

        if (MapUtils.isNotEmpty(parentAndChild)) {
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {

                TypeElement parent = entry.getKey();  //封装字段的最里层类
                List<Element> childs = entry.getValue();

                //获得包名和文件名
                String qualifiedName = parent.getQualifiedName().toString();
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                String fileName = parent.getSimpleName() + NAME_OF_AUTOWIRED;

                logger.info(">>> Start process " + childs.size() + " field in " + parent.getSimpleName() + " ... <<<");

                //构建自动依赖注入代码的文件
                TypeSpec.Builder injectHelper = TypeSpec.classBuilder(fileName)
                        .addJavadoc(WARNING_TIPS)
                        .addSuperinterface(ClassName.get(type_ISyringe))
                        .addModifiers(PUBLIC);

                /*
                    private SerializationService serializationService;
                 */
                FieldSpec serializationServiceField = FieldSpec.builder(TypeName.get(type_SerializationService), "serializationService", Modifier.PRIVATE).build();
                injectHelper.addField(serializationServiceField);

                /*
                    Build method : 'inject'
                    @Override
                    public void inject(Object target) {
                        serializationService = XRouter.getInstance().navigation(SerializationService.class);
                        T substitute = (T)target;
                    }
                 */
                MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(METHOD_INJECT)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(objectParamSpec)
                        .addStatement("serializationService = $T.getInstance().navigation($T.class)", XRouterClassName, ClassName.get(type_SerializationService))
                        .addStatement("$T substitute = ($T)target", ClassName.get(parent), ClassName.get(parent));

                // 生成依赖注入方法的主体, 开始实现依赖注入的方法.
                for (Element element : childs) {
                    AutoWired fieldConfig = element.getAnnotation(AutoWired.class);
                    String fieldName = element.getSimpleName().toString();
                    // It's provider
                    if (types.isSubtype(element.asType(), type_IProvider)) {
                        if (StringUtils.isEmpty(fieldConfig.name())) {    // 没有设置服务provider的路径，直接使用类名寻找
                            // Getter
                            injectMethodBuilder.addStatement(
                                    "substitute." + fieldName + " = $T.getInstance().navigation($T.class)",
                                    XRouterClassName,
                                    ClassName.get(element.asType())
                            );
                        } else {     // 设置类服务provider的路径，使用路径寻找
                            // Getter
                            injectMethodBuilder.addStatement(
                                    "substitute." + fieldName + " = ($T)$T.getInstance().build($S).navigation();",
                                    ClassName.get(element.asType()),
                                    XRouterClassName,
                                    fieldConfig.name()
                            );
                        }

                        // 增加校验"字段是否为NULL"的判断代码
                        if (fieldConfig.required()) {
                            injectMethodBuilder.beginControlFlow("if (substitute." + fieldName + " == null)");
                            injectMethodBuilder.addStatement(
                                    "throw new RuntimeException(\"The field '" + fieldName + "' is null, in class '\" + $T.class.getName() + \"!\")", ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        }
                    } else {    // It's normal intent value
                        String originalValue = "substitute." + fieldName;
                        String statement = "substitute." + fieldName + " = substitute.";
                        boolean isActivity = false;

                        // Activity, then use getIntent()
                        if (types.isSubtype(parent.asType(), type_Activity)) {
                            isActivity = true;
                            statement += "getIntent().";

                            // Fragment, then use getArguments()
                        } else if (types.isSubtype(parent.asType(), type_Fragment)
                                || types.isSubtype(parent.asType(), type_Fragment_V4)) {
                            statement += "getArguments().";
                        } else {
                            throw new IllegalAccessException("The field [" + fieldName + "] need autowired from intent, its parent must be activity or fragment!");
                        }

                        statement = buildStatement(originalValue, statement, typeUtils.typeExchange(element), isActivity);

                        if (statement.startsWith("serializationService.")) {   // 如果参数是Object，需要反序列化
                            injectMethodBuilder.beginControlFlow("if (serializationService != null)");
                            injectMethodBuilder.addStatement(
                                    "substitute." + fieldName + " = " + statement,
                                    StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name(),
                                    ClassName.get(element.asType())
                            );
                            injectMethodBuilder.nextControlFlow("else");
                            injectMethodBuilder.addStatement(
                                    "$T.e(\"You want automatic inject the field '" + fieldName + "' in class '$T' , then you should implement 'SerializationService' to support object auto inject!\")", XRLogClassName, ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        } else {
                            injectMethodBuilder.addStatement(statement,
                                    StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name()
                            );
                        }

                        // 增加校验"字段是否为NULL"的判断代码
                        if (fieldConfig.required() && !element.asType().getKind().isPrimitive()) {  // Primitive wont be check.
                            injectMethodBuilder.beginControlFlow("if (null == substitute." + fieldName + ")");
                            injectMethodBuilder.addStatement(
                                    "$T.e(\"The field '" + fieldName + "' is null, in class '\" + $T.class.getName() + \"!\")", XRLogClassName, ClassName.get(parent));
                            injectMethodBuilder.endControlFlow();
                        }
                    }
                }

                //添加依赖注入的方法
                injectHelper.addMethod(injectMethodBuilder.build());

                // 生成自动依赖注入的类文件[ClassName]$$XRouter$$AutoWired
                JavaFile.builder(packageName, injectHelper.build()).build().writeTo(filer);

                logger.info(">>> " + parent.getSimpleName() + " has been processed, " + fileName + " has been generated. <<<");
            }

            logger.info(">>> AutoWired processor stop. <<<");
        }
    }

    /**
     * 构建普通字段赋值[intent]的表达式
     *
     * @param originalValue 默认值
     * @param statement     表达式
     * @param type          值的类型
     * @param isActivity    是否是activity
     * @return
     */
    private String buildStatement(String originalValue, String statement, int type, boolean isActivity) {
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement += (isActivity ? ("getBooleanExtra($S, " + originalValue + ")") : ("getBoolean($S)"));
        } else if (type == TypeKind.BYTE.ordinal()) {
            statement += (isActivity ? ("getByteExtra($S, " + originalValue + ")") : ("getByte($S)"));
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement += (isActivity ? ("getShortExtra($S, " + originalValue + ")") : ("getShort($S)"));
        } else if (type == TypeKind.INT.ordinal()) {
            statement += (isActivity ? ("getIntExtra($S, " + originalValue + ")") : ("getInt($S)"));
        } else if (type == TypeKind.LONG.ordinal()) {
            statement += (isActivity ? ("getLongExtra($S, " + originalValue + ")") : ("getLong($S)"));
        } else if (type == TypeKind.CHAR.ordinal()) {
            statement += (isActivity ? ("getCharExtra($S, " + originalValue + ")") : ("getChar($S)"));
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement += (isActivity ? ("getFloatExtra($S, " + originalValue + ")") : ("getFloat($S)"));
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement += (isActivity ? ("getDoubleExtra($S, " + originalValue + ")") : ("getDouble($S)"));
        } else if (type == TypeKind.STRING.ordinal()) {
            statement += (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statement += (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
        } else if (type == TypeKind.OBJECT.ordinal()) {
            statement = "serializationService.parseObject(substitute." + (isActivity ? "getIntent()." : "getArguments().") + (isActivity ? "getStringExtra($S)" : "getString($S)") + ", new com.xuexiang.xrouter.model.TypeWrapper<$T>(){}.getType())";
        }
        return statement;
    }

    /**
     * 分类字段，寻找他们所在的类（按父类进行分类）
     *
     * @param elements 被@AutoWired修饰的字段
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                //getEnclosingElement--返回封装此元素的最里层元素, 即该字段所在的类。这里一般是Activity/Fragment。
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The inject fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }
            logger.info("categories finished.");
        }
    }
}
