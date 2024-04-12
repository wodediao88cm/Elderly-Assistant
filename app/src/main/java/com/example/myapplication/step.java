package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class step extends AppCompatActivity {
    //动态申请健康运动权限
    private static final String[] ACTIVITY_RECOGNITION_PERMISSION = {Manifest.permission.ACTIVITY_RECOGNITION};

    private TextView mStepText;
    private SensorManager mSensorManager;
    private MySensorEventListener mListener;
    private int mStepDetector = 0;  // 自应用运行以来STEP_DETECTOR检测到的步数
    private int mStepCounter = 0;   // 自系统开机以来STEP_COUNTER检测到的步数
    private Button Button_reset;
    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, step.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step);
        Button_reset = findViewById(R.id.Button_reset);
        mStepText = (TextView) findViewById(R.id.stepText);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mListener = new MySensorEventListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 检查该权限是否已经获取
            int get = ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION_PERMISSION[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (get != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求自动开启权限
                ActivityCompat.requestPermissions(this, ACTIVITY_RECOGNITION_PERMISSION, 321);
            }
        }
        Button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStepDetector = 0;
                GlobalData.getInstance().setStepCount(mStepDetector);
//                String desc = String.format(Locale.CHINESE, "设备检测到您当前走了%d步,现在走了：%d步", mStepCounter, mStepDetector);
                String desc = String.format(Locale.CHINESE, "您已经走了：%d步 \n  路程：" +
                        "%.3f km  \n  消耗卡路里：%.3f ", mStepDetector,0.5*mStepDetector*0.001,0.5*mStepDetector*0.001*67);
                mStepText.setText(desc);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        mSensorManager.unregisterListener(mListener);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //提示用户手动开启权限
                    new AlertDialog.Builder(this)
                            .setTitle("健康运动权限")
                            .setMessage("健康运动权限不可用")
                            .setPositiveButton("立即开启", (dialog12, which) -> {
                                // 跳转到应用设置界面
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 123);
                            })
                            .setNegativeButton("取消", (dialog1, which) -> {
                                Toast.makeText(getApplicationContext(), "没有获得权限，应用无法运行！", Toast.LENGTH_SHORT).show();
                                finish();
                            }).setCancelable(false).show();
                }
            }
        }
    }

    private class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mStepDetector = GlobalData.getInstance().getStepcount();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                System.out.println("@@@:" + event.sensor.getType() + "--" + Sensor.TYPE_STEP_DETECTOR + "--" + Sensor.TYPE_STEP_COUNTER);

                if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    if (event.values[0] == 1.0f) {
                        mStepDetector++;
                    }
                } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    mStepCounter = (int) event.values[0];
                }
                String desc = String.format(Locale.CHINESE, "您已经走了：%d步 \n  路程：" +
                        "%.3f km  \n  消耗卡路里：%.3f ", mStepDetector,0.5*mStepDetector*0.001,0.5*mStepDetector*0.001*67);
                mStepText.setText(desc);
            }
            GlobalData.getInstance().setStepCount(mStepDetector);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
