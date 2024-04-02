package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.gesture.Gesture_MainActivity;

public class yijiantong extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yijiantong);
        if (shouldAskPermissions()) {
            askPermissions();
        }

        Button button4 = findViewById(R.id.button4);
        //点击
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });


        Button button5 = findViewById(R.id.button5);
        //点击
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri1 = Uri.parse("alipayqr://platformapi/startapp?saId=20000056");
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri1);
                startActivity(intent1);

            }
        });
        Button button6 = findViewById(R.id.button6);
        //点击
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri2 = Uri.parse("alipayqr://platformapi/startapp?saId=20000123");
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                startActivity(intent2);

            }
        });
        Button button8 = findViewById(R.id.button8);
        //点击
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent addIntent = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
//                addIntent.setType("vnd.android.cursor.dir/person");
//                addIntent.setType("vnd.android.cursor.dir/contact");
//                addIntent.setType("vnd.android.cursor.dir/raw_contact");
//                boolean number = false;
//                addIntent.putExtra(ContactsContract.Intents.Insert.NAME, number);
//
//                boolean numberForNewConstant = false;
//                addIntent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, numberForNewConstant);
//                startActivity(addIntent);
                Object packageContext;
                Intent intent3=new Intent(yijiantong.this,emergency_number.class);
                startActivity(intent3);
            }
        });
        View view = getLayoutInflater().inflate(R.layout.emergency_number, null, false);
        EditText emergencyNumberEditText = view.findViewById(R.id.emergency_number);
        String emergencyNumber = emergencyNumberEditText.getText().toString();


//// 保存电话号码到SharedPreferences
//        SharedPreferences.Editor editor = getSharedPreferences("emergency_setting", MODE_PRIVATE).edit();
//        editor.putString("emergency_number", emergencyNumber);
//        editor.apply();

// 绑定拨号按钮
        Button button7 = findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 从SharedPreferences中获取保存的电话号码
                SharedPreferences preferences = getSharedPreferences("emergency_setting", MODE_PRIVATE);
                String savedEmergencyNumber = preferences.getString("emergency_number", "");

                if (!savedEmergencyNumber.isEmpty()) {
                    // 调用系统方法拨打电话
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + savedEmergencyNumber));
                    startActivity(intent);
                }else {
                    // 提示用户输入有效的紧急电话号码
                    Toast.makeText(yijiantong.this, "请先输入有效的紧急电话号码", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button button9 = findViewById(R.id.button9);
        //点击
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object packageContext;
                Intent intent1=new Intent(yijiantong.this, Gesture_MainActivity.class);
                startActivity(intent1);

            }
        });


    }

    private boolean askPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private boolean shouldAskPermissions() {
        String[] permissions = {
                "android.permission.CALL_PHONE"
        };
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
        return true;
    }
}