package com.lmw.fastbus.lib.entity;


import com.lmw.fastbus.lib.contrace.FastBusBinder;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class FastBusBinderImpl implements FastBusBinder {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void add(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void unbind() {
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    public boolean isUnbind() {
        return mCompositeDisposable.isDisposed();
    }
}
