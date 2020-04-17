package com.lmw.fastbus;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.lmw.fastbus.lib.FastBus;

import java.util.ArrayList;

public class SecondActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        findViewById(R.id.btSendEvent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> event2 = new ArrayList<>();
                event2.add(Key.EVENT2);

                FastBus.post(Key.EVENT1, new MyBo());
                FastBus.post(Key.EVENT2, event2);
            }
        });
    }
}
