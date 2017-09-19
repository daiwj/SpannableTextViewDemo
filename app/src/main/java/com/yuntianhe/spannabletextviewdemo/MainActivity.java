package com.yuntianhe.spannabletextviewdemo;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_spanable_text_view);

        SpannableTextView text1 = (SpannableTextView) findViewById(R.id.stv_1);

        text1.setText("#红色#123#红色#变大%蓝色%456黄色可点击");
        text1
                .matchColor("^[0-9]\\d*$", Color.GREEN)
                .setMultipleColor('#', Color.RED)
                .setMultipleColor('%', Color.BLUE)
                .setSingleSize("变大", 50)
                .setSingleImage("应用图标1", BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSingleImage("应用图标2", R.mipmap.ic_launcher)
                .setTextClick("黄色可点击", Color.YELLOW, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("SpannableTextView", "黄色可点击");
                    }
                })
                .apply();
    }
}

