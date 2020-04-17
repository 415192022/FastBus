package com.lmw.fastbus.lib;



import com.lmw.fastbus.lib.contrace.FastBusBinder;
import com.lmw.fastbus.lib.contrace.FastBusBinderGenerator;
import com.lmw.fastbus.lib.entity.Event;
import com.lmw.fastbus.lib.entity.FastBusBinderImpl;
import com.lmw.fastbus.lib.entity.Null;
import com.lmw.fastbus.lib.entity.SchedulerProvider;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class FastBus {

    private static volatile FastBus mInstance;
    private static String[] sModules = new String[]{};
    private static String sMainModule;

    private final FlowableProcessor<Event> mBus;
    private Map<String, FastBusBinderGenerator> mGeneratorMap = new HashMap<>();
    private SchedulerProvider mSchedulerProvider;

    private FastBus() {
        PublishProcessor<Event> objectPublishProcessor = PublishProcessor.create();
        mBus = objectPublishProcessor.toSerialized();
        for (String model : sModules) {
            generator(model);
        }
        generator(sMainModule);
    }

    @SuppressWarnings("unchecked")
    private void generator(String modelName) {
        try {
            Class<FastBusBinderGenerator> generatorClass = (Class<FastBusBinderGenerator>) Class.forName("com.lmw.demo.fastbus.FastBusBinderGeneratorImpl_" + modelName);
            Method method = generatorClass.getMethod("instance");
            FastBusBinderGenerator mGenerator = (FastBusBinderGenerator) method.invoke(null);
            mGeneratorMap.put(modelName, mGenerator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FastBus getDefault() {
        if (mInstance == null) {
            synchronized (FastBus.class) {
                if (mInstance == null) {
                    mInstance = new FastBus();
                }
            }
        }
        return mInstance;
    }

    public static void init(Scheduler scheduler, String mainModule, String... modules) {
        if (mainModule == null || mainModule.length() == 0) {
            throw new RuntimeException("mainModule must has");
        }
        sMainModule = mainModule;
        sModules = modules;
        FastBus.getDefault().mSchedulerProvider = SchedulerProvider.create(scheduler);
    }

    public static SchedulerProvider getSchedulerProvider() {
        if (FastBus.getDefault().mSchedulerProvider == null) {
            throw new RuntimeException("FastBus must be init");
        }
        return FastBus.getDefault().mSchedulerProvider;
    }

    public static FastBusBinder bind(Object o) {
        if (null == o) {
            throw new RuntimeException("object to subscribe must not be null");
        }
        FastBusBinderGenerator generator = null;
        String packageName = o.getClass().getPackage().getName();
        for (String module : sModules) {
            if (packageName.contains("." + module)) {
                generator = FastBus.getDefault().mGeneratorMap.get(module);
                break;
            }
        }
        if (generator == null) {
            generator = FastBus.getDefault().mGeneratorMap.get(sMainModule);
        }
        if (generator == null) {
            return new FastBusBinderImpl();
        }
        return generator.generate(o);
    }

    public static void unBind(FastBusBinder bind) {
        if (bind != null) {
            bind.unbind();
        }
    }

    public static void post(String tag) {
        FastBus.getDefault().mBus.onNext(new Event(tag, null));
    }

    public static void post(String tag, Object actual) {
        FastBus.getDefault().mBus.onNext(new Event(tag, actual));
    }

    public static <T> Flowable<T> toObservable(final String[] tags, final Class<T> eventType) {
        return FastBus.getDefault().mBus.filter(new Predicate<Event>() {
            @Override
            public boolean test(Event event) {
                return Arrays.asList(tags).contains(event.getTag())
                        && ((eventType.isInstance(event.getData()) || event.getData() == null));
            }
        }).flatMap(new Function<Event, Publisher<T>>() {
            @Override
            public Publisher<T> apply(Event event) {
                return Flowable.just(eventType.cast(event.getData() != null ? event.getData() : new Null()));
            }
        });
    }
}