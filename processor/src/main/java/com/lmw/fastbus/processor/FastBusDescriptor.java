package com.lmw.fastbus.processor;


import com.lmw.fastbus.lib.entity.BackpressureMode;
import com.lmw.fastbus.lib.entity.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

class FastBusDescriptor {

    List<String> tags = new ArrayList<>();
    boolean canReceiveNull = false;
    ThreadMode observeOn = ThreadMode.MAIN;
    ThreadMode subscribeOn = ThreadMode.MAIN;
    BackpressureMode backpressure = BackpressureMode.NORMAL;
    ExecutableElement methodElement;

    FastBusDescriptor(ExecutableElement element) {
        methodElement = element;
    }
}
