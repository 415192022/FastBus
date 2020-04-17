package com.lmw.fastbus;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lmw.fastbus.lib.FastBus;
import com.lmw.fastbus.lib.contrace.FastBusBinder;


public abstract class BaseActivity extends AppCompatActivity {

    private FastBusBinder mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = FastBus.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FastBus.unBind(mBind);
    }
}
