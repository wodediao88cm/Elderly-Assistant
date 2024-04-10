package com.example.myapplication.weather;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class SettingActivity extends AppCompatActivity {
    private EditText location;
    private TextView unit_text;
    private LinearLayout page;
    private LinearLayout unit;
    private String city;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_setting);

        location = (EditText) findViewById(R.id.location);
        unit = (LinearLayout)findViewById(R.id.unit);
        page = (LinearLayout)findViewById(R.id.page);
        unit_text = (TextView)findViewById(R.id.unit_text);


        SharedPreferences pref = getSharedPreferences("setting",MODE_PRIVATE);
        location.setText(pref.getString("city","天津"));
        unit_text.setText(pref.getString("unit","摄氏度"));


        page.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                location.clearFocus();
            }
        });

        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                city = s.toString();
                SharedPreferences.Editor editor = getSharedPreferences("setting",MODE_PRIVATE).edit();
                editor.putString("city",city);
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        unit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                click();
            }
        });

    }

    //点击按钮弹出一个单选对话框
    public void click() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择温度单位");
        final String items[] = {"摄氏度","华氏度"};

//-1代表没有条目被选中
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //1.把选中的条目取出来
                String item = items[which];
                Toast.makeText(getApplicationContext(),item.toString(),Toast.LENGTH_LONG).show();
                unit_text.setText(item.toString());
                SharedPreferences.Editor editor = getSharedPreferences("setting",MODE_PRIVATE).edit();
                editor.putString("unit",item.toString());
                editor.commit();
                //2.然后把对话框关闭
                dialog.dismiss();
            }
        });
//一样要show
        builder.show();
    }


}
