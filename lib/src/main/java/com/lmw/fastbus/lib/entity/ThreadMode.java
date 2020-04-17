package com.lmw.fastbus.lib.entity;

public enum ThreadMode {
    /**
     * 拥有一个线程单例，所有的任务都在这一个线程中执行
     */
    SINGLE,

    /**
     * 事件的处理开启一个新的线程
     */
    NEW,

    /**
     * 事件的处理会在UI线程中执行
     */
    MAIN,

    /**
     * 事件处理会在单独的线程中执行
     */
    IO
}
