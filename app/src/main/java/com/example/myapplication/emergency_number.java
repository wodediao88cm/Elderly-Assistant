package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class emergency_number extends AppCompatActivity {
    private EditText emergencyNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_number);

        // 初始化EditText
        emergencyNumberEditText = findViewById(R.id.emergency_number);

        // 从SharedPreferences中获取保存的电话号码并显示在EditText中
        SharedPreferences preferences = getSharedPreferences("emergency_setting", MODE_PRIVATE);
        String savedEmergencyNumber = preferences.getString("emergency_number", "");
        emergencyNumberEditText.setText(savedEmergencyNumber);

        emergencyNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 保存用户输入的电话号码到SharedPreferences
                String emergencyNumber = s.toString();
                SharedPreferences.Editor editor = getSharedPreferences("emergency_setting", MODE_PRIVATE).edit();
                editor.putString("emergency_number", emergencyNumber);
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }}
