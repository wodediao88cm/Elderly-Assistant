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
import android.location.LocationRequest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.myapplication.ml.FallModel1;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;



public class fall_detection extends AppCompatActivity implements SensorEventListener  {

    private SensorManager sensorManager;
    //public Adapter mAdapter;
    private LocationRequest locationRequest;
    private Sensor sensor;
    private TextView tv,tv1,tv2;
    private Button bt, bt1;
    private  float accX;
    private  float accY;
    private  float accZ;
    private static final String TAG = fall_detection.class.getSimpleName();
    float[] values = new float[1203];
    AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private Vibrator vibrate;
    private long lastAccelTime = 0;
    private Timer timer;
    private Handler handler;
    private int count=1;
    private int i=0,j=0;
    public boolean starting=true;
    private Dbhelper Dbhelper;
    private BottomNavigationView bottomNavigationView;




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
        //tv2 = findViewById(R.id.txt4);
        bottomNavigationView=findViewById(R.id.bottom_navigation);


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

        values[i] = accX; // 将X轴加速度值存入values数组
        values[i + 1] = accY; // 将Y轴加速度值存入values数组
        values[i + 2] = accZ; // 将Z轴加速度值存入values数组
        i += 3; // 更新索引值

        if (i >= 1202) { // 如果已经收集了足够多的数据
            i = 0; // 重置索引值
            RunModel(values); // 传入收集的加速度值数组，执行模型运行方法
        }
    }

    private void RunModel(float[] values) {
        try {
            FallModel1 model = FallModel1.newInstance(fall_detection.this);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 401 * 3);

            byteBuffer.order(ByteOrder.nativeOrder());
            for (int f=0; f<1200;f+=3){
                //an error here to fix related to java.nio.BufferOverflowException
                byteBuffer.putFloat(values[f]);
                byteBuffer.putFloat(values[1+f]);
                byteBuffer.putFloat(values[2+f]);
            }
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 401, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            FallModel1.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            float[] output = outputFeature0.getFloatArray();
            model.close();
            //TextView t = findViewById(R.id.txt);



            //TextView tx = findViewById(R.id.text);
            if (output[0]<0.6){

                AlertSet();

                sensorManager.unregisterListener(this);



            }



        } catch ( IOException e) {
            // TODO Handle the exception
        }
    }




    @SuppressLint("HandlerLeak")
    private void AlertSet() {
        MediaPlayer player ;
        player = MediaPlayer.create(this,R.raw.ringtone);
        //isAlertset();
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);//震动
        Timer timer = new Timer();
        if (starting == true  ){
            addRingtone(player);
            setVibrate();
            //starting = false;
        }



        builder = new AlertDialog.Builder(this);
        builder.setTitle("是否发生跌倒？");
        builder.setCancelable(false);
        builder.setPositiveButton("是", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Call the contact

                        vibrate.cancel();
                        timer.cancel();
                        stopRingtone(player);
                        //getLocation();
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

                    alertDialog.setMessage("拒绝或确认发生跌倒: \n" +
                            "倒计时结束后将自动发送短信!!\n"+ countTime );
                    countTime --;


                }else{

                    //getLocation();
                    timer.cancel();
                    alertDialog.cancel();
                    vibrate.cancel();
                    stopRingtone(player);

                    sensorManager.unregisterListener(fall_detection.this);



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