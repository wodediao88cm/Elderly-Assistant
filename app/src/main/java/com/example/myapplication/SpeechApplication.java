package com.example.myapplication;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class SpeechApplication extends Application {

    @Override
    public void onCreate() {

        //   5ef048e1  为在开放平台注册的APPID  注意没有空格，直接替换即可
        SpeechUtility.createUtility(SpeechApplication.this, "appid=85f780f1");

        super.onCreate();
    }
}

