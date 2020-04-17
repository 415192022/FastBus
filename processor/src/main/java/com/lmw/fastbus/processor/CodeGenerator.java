package com.lmw.fastbus.processor;

import com.lmw.fastbus.lib.FastBus;
import com.lmw.fastbus.lib.contrace.FastBusBinder;
import com.lmw.fastbus.lib.contrace.FastBusBinderGenerator;
import com.lmw.fastbus.lib.entity.BackpressureMode;
import com.lmw.fastbus.lib.entity.FastBusBinderImpl;
import com.lmw.fastbus.lib.entity.Null;
import com.lmw.fastbus.lib.entity.ThreadMode;
import com.lmw.fastbus.lib.utils.FastBusUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.reactivex.subscribers.DisposableSubscriber;

class CodeGenerator {

    private static final String GENERATE_PACKAGE_NAME = "com.lmw.demo.fastbus";
    private static final String GENERATE_CLASS_NAME = "FastBusBinderGeneratorImpl";
    private static final String GPOLLO_BINDER_NAME = "fastbusBinder";
    private static final String ACTION1_CALL_PARAM = "callParam";
    private static final String GENERATE_PARAM = "bindObject";

    private List<FastBusDescriptor> mGpolloDescriptors;
    private Filer mFiler;
    private String mTag;

    private CodeGenerator(ArrayList<FastBusDescriptor> gpolloDescriptors, Filer filer, String tag) {
        this.mGpolloDescriptors = gpolloDescriptors;
        this.mFiler = filer;
        this.mTag = tag;
    }

    static CodeGenerator create(ArrayList<FastBusDescriptor> gpolloDescriptors, Filer filer, String tag) {
        return new CodeGenerator(gpolloDescriptors, filer, tag);
    }

    void createJavaFile() {
        try {
            getBinderGeneratorJavaFile().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JavaFile getBinderGeneratorJavaFile() {
        return JavaFile.builder(GENERATE_PACKAGE_NAME, getGeneratorTypeSpec())
                .addStaticImport(ThreadMode.MAIN)
                .addStaticImport(ThreadMode.IO)
                .addStaticImport(ThreadMode.SINGLE)
                .addStaticImport(ThreadMode.NEW)
                .build();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * public final class FastBusBinderGeneratorImpl_app implements FastBusBinderGenerator {...}
     */
    private TypeSpec getGeneratorTypeSpec() {
        return TypeSpec.classBuilder(GENERATE_CLASS_NAME + "_" + mTag)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(FastBusBinderGenerator.class)
                .addField(getSingleInstanceFileSpec())
                .addMethod(getSingleInstanceMethodSpec())
                .addMethod(getGenerateFunctionMethodSpec())
                .build();
    }

    /**
     * private static FastBusBinderGenerator sInstance;
     */
    private FieldSpec getSingleInstanceFileSpec() {
        return FieldSpec.builder(FastBusBinderGenerator.class, "instance", Modifier.PRIVATE, Modifier.STATIC)
                .build();
    }

    /**
     * public static synchronized FastBusBinderGenerator instance() {
     * if (null == sInstance) {
     * sInstance = new FastBusBinderGenerator();
     * }
     * return sInstance;
     * }
     */
    private MethodSpec getSingleInstanceMethodSpec() {
        return MethodSpec.methodBuilder("instance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
                .returns(FastBusBinderGenerator.class)
                .beginControlFlow("if (instance == null)")
                .addStatement("instance = new " + GENERATE_CLASS_NAME + "_" + mTag + "()")
                .endControlFlow()
                .addStatement("return instance")
                .build();
    }

    /**
     * public FastBusBinder generate(final Object bindObject) {...}
     */
    private MethodSpec getGenerateFunctionMethodSpec() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("generate")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(FastBusBinder.class)
                .addParameter(Object.class, GENERATE_PARAM, Modifier.FINAL)
                .addStatement("$T " + GPOLLO_BINDER_NAME + " = new $T()", FastBusBinderImpl.class, FastBusBinderImpl.class);

        if (mGpolloDescriptors != null) {
            for (FastBusDescriptor gpolloDescriptor : mGpolloDescriptors) {
                getSingleBinderStatement(builder, gpolloDescriptor);
            }
        }
        return builder.addStatement("return " + GPOLLO_BINDER_NAME).build();
    }

    /**
     * FastBusBinderImpl gpolloBinder = new FastBusBinderImpl();
     * if (MainActivity.class.isAssignableFrom(bindObject.getClass())) {
     * gpolloBinder.add(FastBus.getDefault().toObservable(new String[]{...}, ....class).subscribe(new Action1<...>() {
     * ......
     * }
     */
    private void getSingleBinderStatement(MethodSpec.Builder builder, FastBusDescriptor gpolloDescriptors) {
        List<? extends VariableElement> parameters = gpolloDescriptors.methodElement.getParameters();
        TypeMirror typeMirror = null;
        if (parameters.size() > 1) {
            throw new RuntimeException("Gpollp error : receive event method can only have one parameter");
        }
        if (parameters.size() == 1) {
            typeMirror = parameters.get(0).asType();
        }
        boolean canReceiveNull = gpolloDescriptors.canReceiveNull;
        ThreadMode subscribeOn = gpolloDescriptors.subscribeOn;
        ThreadMode observeOn = gpolloDescriptors.observeOn;
        BackpressureMode backpressure = gpolloDescriptors.backpressure;
        String methodName = gpolloDescriptors.methodElement.getSimpleName().toString();
        String clazzType = gpolloDescriptors.methodElement.getEnclosingElement().asType().toString().replaceAll("<.*>", "");
        builder.beginControlFlow("if (" + clazzType + ".class.isAssignableFrom(" + GENERATE_PARAM + ".getClass()))")
                .addStatement(GPOLLO_BINDER_NAME + ".add($T.toObservable(new String[]{"
                        + FastBusUtil.split(gpolloDescriptors.tags, ",") + "}, $T.class)"
                        + getBackpressureMethodCode(backpressure)
                        + getSubscribeOnMethodCode(subscribeOn)
                        + getObserveOnMethodCode(observeOn)
                        + getSubscribeWithCode(typeMirror, clazzType, methodName, canReceiveNull) + ")", FastBus.class, Object.class)
                .endControlFlow();
    }

    /**
     * .onBackpressurebuffer()
     */
    private String getBackpressureMethodCode(BackpressureMode backpressureMode) {
        switch (backpressureMode) {
            case BUFFER:
                return ".onBackpressureBuffer()";
            case DROP:
                return ".onBackpressureDrop()";
            case LATEST:
                return ".onBackpressureLatest()";
        }
        return "";
    }

    /**
     * .subscribeOn(FastBus.getSchedulerProvider().get(ThreadMode.IO))
     */
    private String getSubscribeOnMethodCode(ThreadMode subscribeOn) {
        return ".subscribeOn(FastBus.getSchedulerProvider().get(" + subscribeOn.name() + "))";
    }

    /**
     * .observeOn(FastBus.getSchedulerProvider().get(ThreadMode.MAIN))
     */
    private String getObserveOnMethodCode(ThreadMode observeOn) {
        return ".observeOn(FastBus.getSchedulerProvider().get(" + observeOn.name() + "))";
    }

    /**
     * .subscribeWith(new DisposableSubscriber<Object>(){
     * });
     */
    private CodeBlock getSubscribeWithCode(TypeMirror typeMirror, String clazzType, String methodName, boolean canReceiveNull) {
        return CodeBlock.builder().add(".subscribeWith(new $T<Object>(){"
                + getOnCompleteMethodCode() + getOnErrorMethodCode()
                + getOnNextMethodCode(typeMirror, clazzType, methodName, canReceiveNull) + "})", DisposableSubscriber.class)
                .build();
    }

    /**
     * public void onComplete(Object o) {
     * }
     */
    private MethodSpec getOnCompleteMethodCode() {
        return MethodSpec.methodBuilder("onComplete")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .build();
    }

    /**
     * public void onError(java.lang.Throwable t) {
     * }
     */
    private MethodSpec getOnErrorMethodCode() {
        return MethodSpec.methodBuilder("onError")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(Throwable.class, "t")
                .addCode("t.printStackTrace();")
                .build();
    }

    /**
     * public void onNext(Object callParam) {
     * }
     */
    private MethodSpec getOnNextMethodCode(TypeMirror typeMirror, String clazzType, String methodName, boolean canReceiveNull) {
        return MethodSpec.methodBuilder("onNext")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(Object.class, ACTION1_CALL_PARAM)
                .addCode(getCallMethodCode(typeMirror, clazzType, methodName, canReceiveNull))
                .build();
    }

    /**
     * MainActivity subscribe = (MainActivity) bindObject;
     * subscribe.add(callParam);
     */
    private CodeBlock getCallMethodCode(TypeMirror typeMirror, String clazzType, String methodName, boolean canReceiveNull) {
        CodeBlock.Builder builder = CodeBlock.builder().addStatement(clazzType + " subscribe = (" + clazzType + ") " + GENERATE_PARAM);
        if (typeMirror != null) {
            if (canReceiveNull) {
                builder = builder.beginControlFlow("if(" + ACTION1_CALL_PARAM + " instanceof $T)", Null.class)
                        .addStatement("subscribe." + methodName + "(null)")
                        .endControlFlow();
            }
            builder = builder.beginControlFlow("if(" + ACTION1_CALL_PARAM + " instanceof " + FastBusUtil.parseVariableType(typeMirror) + ")")
                    .addStatement("subscribe." + methodName + "(($T)" + ACTION1_CALL_PARAM + ")", typeMirror)
                    .endControlFlow();
        } else {
            builder.addStatement("subscribe." + methodName + "()");
        }
        return builder.build();
    }
}