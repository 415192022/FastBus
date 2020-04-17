package com.lmw.fastbus.lib.contrace;


import io.reactivex.disposables.Disposable;
public interface FastBusBinder {

    void add(Disposable disposable);

    void unbind();

    boolean isUnbind();
}
