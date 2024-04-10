package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Fall_fec.fall_detection;
import com.example.myapplication.voiceassisant1.MainActivity_yuyin;
import com.example.myapplication.weather.MainActivity1;

public class MainActivity extends AppCompatActivity {

    public static final String APP_PACKAGE_NAME = "com.example.app_fall";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button_main_1=findViewById(R.id.button_main_1);
        //点击
        button_main_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(MainActivity.this, quickoperation.class);
                startActivity(intent1);

            }
        });
        //点击
        Button button_main_2=findViewById(R.id.button_main_2);
        button_main_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent(MainActivity.this, MainActivity1.class);
                startActivity(intent2);

            }
        });
        Button button_main_3=findViewById(R.id.button_main_3);
        //点击
        button_main_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent3=new Intent(MainActivity.this,MapActivity1.class);
                startActivity(intent3);

            }
        });


        Button button_main_5=findViewById(R.id.button_main_5);
        //点击
        button_main_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;

                Intent intent1=new Intent(MainActivity.this, fall_detection.class);
                startActivity(intent1);
                //Intent intent = new Intent("com.example.app_fall.MainActivity2");
                //intent.setClassName(MainActivity.this,"com.example.app_fall.MainActivity2.class");
                //startActivity(intent);
//                Intent intent = new Intent();
//                intent.setAction("SHIQJ");//这个值一定要和B应用的action一致，否则会报错
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
            }
        });
        Button button_main_6=findViewById(R.id.button_main_6);
        //点击
        button_main_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent1=new Intent(MainActivity.this,step.class);
                startActivity(intent1);

            }
        });
        Button button_main_7=findViewById(R.id.button_main_7);
        //点击
        button_main_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent1=new Intent(MainActivity.this,xunfei.class);
                startActivity(intent1);

            }
        });
//        Button button_main_8=findViewById(R.id.button_main_8);
//        //点击
//        button_main_8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Object packageContext;
//                Intent intent1=new Intent(MainActivity.this,Gesture_MainActivity.class);
//                startActivity(intent1);
//
//            }
//        });
        Button button_main_9=findViewById(R.id.button_main_9);
        //点击
        button_main_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent1=new Intent(MainActivity.this, MainActivity_yuyin.class);
                startActivity(intent1);

            }
        });
//        Button button_main_10=findViewById(R.id.button_main_10);
//        //点击
//        button_main_10.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Object packageContext;
//                Intent intent1=new Intent(MainActivity.this,TtsDemo.class);
//                startActivity(intent1);
//
//            }
//        });
    }
}