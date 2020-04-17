package com.lmw.fastbus;

import android.app.Application;

import com.lmw.fastbus.lib.FastBus;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initFastBus();
    }

    private void initFastBus() {
        FastBus.init(AndroidSchedulers.mainThread(), "app");
    }
}
