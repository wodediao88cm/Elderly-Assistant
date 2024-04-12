package com.example.myapplication.Fall_fec;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.GlobalData;
import com.example.myapplication.R;

import java.util.Timer;
import java.util.TimerTask;



public class fall_detection extends AppCompatActivity implements SensorEventListener  {

    private SensorManager sensorManager;

    private Sensor sensor;
    private TextView tv1;
    private Button bt, bt1;
    private  float accX;
    private  float accY;
    private  float accZ;
    private static final String TAG = fall_detection.class.getSimpleName();
    AlertDialog.Builder builder;
    private Vibrator vibrate;
    private int i=0,j=0;
    public boolean starting=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.falldetection);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(this.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this,null);
        }
        else{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        bt = findViewById(R.id.button);
        bt1 = findViewById(R.id.button_call);
        tv1 = findViewById(R.id.textView1);



//拨打电话
        View view = getLayoutInflater().inflate(R.layout.emergency_number, null, false);
        EditText emergencyNumberEditText = view.findViewById(R.id.emergency_number);
        String emergencyNumber = emergencyNumberEditText.getText().toString();

        bt1.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(fall_detection.this, "请先输入有效的紧急电话号码", Toast.LENGTH_SHORT).show();
                }
            }
        });






        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (j==0){
                    sensorManager.registerListener(fall_detection.this, sensor,SensorManager.SENSOR_DELAY_FASTEST);
                    Toast.makeText(fall_detection.this,"跌倒检测已开启",Toast.LENGTH_SHORT).show();
                    bt.setText("暂停检测");
                    tv1.setText("检测已开启");
                    j++;


                }
                else{
                    sensorManager.unregisterListener(fall_detection.this);
                    Toast.makeText(fall_detection.this,"跌倒检测已关闭",Toast.LENGTH_SHORT).show();

                    bt.setText("继续检测");
                    tv1.setText("检测已关闭");
                    j=0;
                }
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        accX = sensorEvent.values[0]; // 获取X轴的加速度值
        accY = sensorEvent.values[1]; // 获取Y轴的加速度值
        accZ = sensorEvent.values[2]; // 获取Z轴的加速度值

        RunModel(accX,accY,accZ);

    }
    private void RunModel(float x,float y,float z){
        double SVM = Math.sqrt(x*x+y*y+z*z);
        if(SVM >110)
        {
            AlertSet();
            sensorManager.unregisterListener(this);}


    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(fall_detection.this, sensor,SensorManager.SENSOR_DELAY_FASTEST);
        bt.setText("暂停检测");
        j=1;
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @SuppressLint("HandlerLeak")
    private void AlertSet() {
        MediaPlayer player ;
        player = MediaPlayer.create(this,R.raw.ringtone);
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);//震动
        Timer timer = new Timer();
        if (starting == true  ){
            addRingtone(player);
            setVibrate();
        }

        builder = new AlertDialog.Builder(this);
        builder.setTitle("是否发生跌倒？");
        builder.setCancelable(false);
        builder.setPositiveButton("是", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                        vibrate.cancel();
                        timer.cancel();
                        stopRingtone(player);
                        SendSMS();
                        sensorManager.registerListener(fall_detection.this, sensor,SensorManager.SENSOR_DELAY_FASTEST);



            }
        });

        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                vibrate.cancel();
                timer.cancel();
                stopRingtone(player);
                dialogInterface.cancel();
                sensorManager.registerListener(fall_detection.this, sensor,SensorManager.SENSOR_DELAY_FASTEST);

            }
        });
        final AlertDialog alertDialog=builder.create();
        TimerTask timerTask = new TimerTask() {
            int countTime = 45;

            @Override

            public void run() {
                if (countTime > 0){

                    countTime --;

                }else{
                    timer.cancel();
                    alertDialog.cancel();
                    vibrate.cancel();
                    stopRingtone(player);
                    SendSMS();
                    sensorManager.registerListener(fall_detection.this, sensor,SensorManager.SENSOR_DELAY_FASTEST);
//                    sensorManager.unregisterListener(fall_detection.this);
                }
            }


        };
        //timerTask.cancel();
        timer.schedule(timerTask, 100, 1000);
        alertDialog.show();
    }









    private void SendSMS() {
        SharedPreferences preferences = getSharedPreferences("emergency_setting", MODE_PRIVATE);
        String savedEmergencyNumber = preferences.getString("emergency_number", "");
        try {
            String loca = GlobalData.getInstance().getLoca();
            SmsManager smsManager= SmsManager.getDefault();
            String message ="您家中的长辈疑似发生了跌倒情况，请尽快联系确认！地址为:"+loca;
            StringBuffer smsBody = new StringBuffer();
            //smsBody.append(Uri.parse(message));
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                // 发送短信
                smsManager.sendTextMessage(savedEmergencyNumber,null,message,null,null);
                Toast.makeText(this,"Message is sent!",Toast.LENGTH_SHORT).show();
            } else {
                // 未获得发送短信的授权
                Toast.makeText(this, "Permission to send SMS not granted", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Fail sending the message!",Toast.LENGTH_SHORT).show();
        }

    }


    public void addRingtone(MediaPlayer player){

        if (player == null){
            player = MediaPlayer.create(this,R.raw.ringtone);
        }
        player.start();


    }

    public void stopRingtone(MediaPlayer player){

        if (player != null){
            player.release();}
        player=null;

    }

    public void setVibrate(){

        long[] pattern = {100, 500, 100, 500};
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrate.vibrate(pattern, 2);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

}