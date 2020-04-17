package com.lmw.fastbus.lib.entity;


import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class SchedulerProvider {

    private Scheduler main;
    private Scheduler io = Schedulers.io();
    private Scheduler single = Schedulers.single();
    private Scheduler newThread = Schedulers.newThread();

    private SchedulerProvider(Scheduler main) {
        this.main = main;
    }

    public static SchedulerProvider create(Scheduler main) {
        if (null == main) {
            throw new RuntimeException("the scheduler main must be not null");
        }
        return new SchedulerProvider(main);
    }

    public Scheduler get(ThreadMode mode) {
        switch (mode) {
            case MAIN:
                return main;
            case IO:
                return io;
            case SINGLE:
                return single;
            case NEW:
                return newThread;
            default:
                return main;
        }
    }
}
