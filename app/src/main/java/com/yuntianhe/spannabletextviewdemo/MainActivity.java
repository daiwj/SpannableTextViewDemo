package com.yuntianhe.spannabletextviewdemo;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_spanable_text_view);

        SpannableTextView text1 = (SpannableTextView) findViewById(R.id.stv_1);

        text1.setText("#红色#123#红色#变大%蓝色%456黄色可点击");
        text1.matchColors("\\d+", Color.GREEN)
                .buildColors("#", "#ff4040")
                .buildColors("%", Color.BLUE)
                .buildSize("变大", 50)
                .buildImage("应用图标1", BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .buildImage("应用图标2", R.mipmap.ic_launcher)
                .buildClick("黄色可点击", Color.YELLOW, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "黄色可点击", Toast.LENGTH_SHORT).show();
                    }
                })
                .apply();
    }
}

