package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

public class yijiantong extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yijiantong);
        if (shouldAskPermissions()) {
            askPermissions();
        }


        Button button1 = findViewById(R.id.button1);
        //点击
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=20000067&chInfo=ch_desktop&url=https%3A%2F%2F68687564.h5app.alipay.com%2Fwww%2Findex.html");
                Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
                startActivity(intent10);

            }
        });

        Button button2 = findViewById(R.id.button2);
        //点击
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=2021002170600786");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });
        Button button3 = findViewById(R.id.button3);
        //点击
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=2021001123608001");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });
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
        Button button7 = findViewById(R.id.button8);
        //点击
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
                addIntent.setType("vnd.android.cursor.dir/person");
                addIntent.setType("vnd.android.cursor.dir/contact");
                addIntent.setType("vnd.android.cursor.dir/raw_contact");
                boolean number = false;
                addIntent.putExtra(ContactsContract.Intents.Insert.NAME, number);

                boolean numberForNewConstant = false;
                addIntent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, numberForNewConstant);
                startActivity(addIntent);

            }
        });
        Button button8 = findViewById(R.id.button7);
        //点击
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用系统方法拨打电话
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "15607989193"));
                startActivity(intent);
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