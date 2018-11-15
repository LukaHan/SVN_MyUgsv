package com.dzh.ugsv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tencent.qcloud.xiaoshipin.Ugsv;
import com.tencent.qcloud.xiaoshipin.mainui.TCMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tvGoUgsv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ugsv.init(getApplication());
                startActivity(new Intent(MainActivity.this, TCMainActivity.class));
            }
        });
    }
}
