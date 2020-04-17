package com.lmw.fastbus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.lmw.fastbus.lib.annotations.Backpressure;
import com.lmw.fastbus.lib.annotations.ObserveOn;
import com.lmw.fastbus.lib.annotations.Receive;
import com.lmw.fastbus.lib.annotations.SubscribeOn;
import com.lmw.fastbus.lib.entity.BackpressureMode;
import com.lmw.fastbus.lib.entity.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private TextView mTvEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvEvents = findViewById(R.id.tvEvents);
        findViewById(R.id.btAct2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    @Receive(value = Key.EVENT1, canNull = true)
    @ObserveOn(ThreadMode.MAIN)
    @SubscribeOn(ThreadMode.IO)
    @Backpressure(BackpressureMode.DROP)
    public void onReceive1(MyBo i) {
        mTvEvents.setText(mTvEvents.getText() + "" + i + "\n");
    }

    @Receive(Key.EVENT2)
    public void onReceive2(ArrayList<String> event2) {
        mTvEvents.setText(mTvEvents.getText() + "" + event2 + "\n");
    }
}
