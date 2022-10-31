package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button_main_1=findViewById(R.id.button_main_1);
        //点击
        button_main_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(MainActivity.this,yijiantong.class);
                startActivity(intent1);

            }
        });
        //点击
        Button button_main_2=findViewById(R.id.button_main_2);
        button_main_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent(MainActivity.this,jiankang.class);
                startActivity(intent2);

            }
        });
        Button button_main_3=findViewById(R.id.button_main_3);
        //点击
        button_main_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent3=new Intent(MainActivity.this,zhaoxun.class);
                startActivity(intent3);

            }
        });
        Button button_main_4=findViewById(R.id.button_main_4);
        //点击
        button_main_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent1=new Intent(MainActivity.this,yuyin.class);
                startActivity(intent1);

            }
        });




    }
}