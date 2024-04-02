package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

public class GlobalData extends AppCompatActivity {
    private static GlobalData instance;

    private String loca;
    private String min_temp;
    private String max_temp;
    private String IconDesc;
    private GlobalData() {
        // 私有构造函数
    }

    public static GlobalData getInstance() {
        if (instance == null) {
            instance = new GlobalData();
        }
        return instance;
    }

    public String getLoca() {
        return loca;
    }

    public void setLoca(String loca) {
        this.loca = loca;
    }

    public String getmin_temp() {
        return min_temp;
    }

    public void setmin_temp(String min_temp) {
        this.min_temp = min_temp;
    }

    public String getmax_temp() {
        return max_temp;
    }

    public void setmax_temp(String max_temp) {
        this.max_temp = max_temp;
    }

    public String getIconDesc() {
        return IconDesc;
    }

    public void setIconDesc(String IconDesc) {
        this.IconDesc = IconDesc;
    }
}