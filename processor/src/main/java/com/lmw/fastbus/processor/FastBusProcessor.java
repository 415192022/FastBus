package com.lmw.fastbus.processor;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.lmw.fastbus.lib.annotations.Backpressure;
import com.lmw.fastbus.lib.annotations.ObserveOn;
import com.lmw.fastbus.lib.annotations.Receive;
import com.lmw.fastbus.lib.annotations.SubscribeOn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class FastBusProcessor extends AbstractProcessor {

    private Map<Element, FastBusDescriptor> mDescriptorMap = new HashMap<>();
    private String mModuleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            mModuleName = options.get("libName");
        }
        if (mModuleName == null) {
            throw new RuntimeException("gradle must config arguments = [libName: \"XXX\"]");
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //支持的注解
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Receive.class.getCanonicalName());
        annotations.add(ObserveOn.class.getCanonicalName());
        annotations.add(SubscribeOn.class.getCanonicalName());
        annotations.add(Backpressure.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return true;
        }
        // 遍历所有被注解了@Receive的元素
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(Receive.class)) {
            if (annotatedElement.getKind() != ElementKind.METHOD) {
                throw new RuntimeException("Only method can be annotated with @Receive");
            }
            FastBusDescriptor descriptor = new FastBusDescriptor((ExecutableElement) annotatedElement);
            ExecutableElement executableElement = MoreElements.asExecutable(annotatedElement);
            Receive annotation = executableElement.getAnnotation(Receive.class);
            descriptor.tags = Arrays.asList(annotation.value());
            descriptor.canReceiveNull = annotation.canNull();
            mDescriptorMap.put(annotatedElement, descriptor);
        }
        // 遍历所有被注解了@ObserveOn的元素
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(ObserveOn.class)) {
            FastBusDescriptor descriptor = mDescriptorMap.get(annotatedElement);
            if (descriptor == null) {
                throw new RuntimeException("@ObserveOn must be used with @Receive");
            }
            descriptor.observeOn = MoreElements.asExecutable(annotatedElement).getAnnotation(ObserveOn.class).value();
        }
        // 遍历所有被注解了@SubscribeOn的元素
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(SubscribeOn.class)) {
            FastBusDescriptor descriptor = mDescriptorMap.get(annotatedElement);
            if (descriptor == null) {
                throw new RuntimeException("@SubscribeOn must be used with @Receive");
            }
            descriptor.subscribeOn = MoreElements.asExecutable(annotatedElement).getAnnotation(SubscribeOn.class).value();
        }
        // 遍历所有被注解了@Backpressure的元素
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(Backpressure.class)) {
            FastBusDescriptor descriptor = mDescriptorMap.get(annotatedElement);
            if (descriptor == null) {
                throw new RuntimeException("@SubscribeOn must be used with @Receive");
            }
            descriptor.backpressure = MoreElements.asExecutable(annotatedElement).getAnnotation(Backpressure.class).value();
        }
        //解析，并生成代码
        CodeGenerator.create(new ArrayList<>(mDescriptorMap.values()), processingEnv.getFiler(), mModuleName).createJavaFile();
        return true;
    }
}
