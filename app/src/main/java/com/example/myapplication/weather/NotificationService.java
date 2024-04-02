package com.example.myapplication.weather;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.weather.WeatherFragment;

public class NotificationService extends IntentService {
    private static final String TAG = "NotificationService";

    private static final int NOTIFICATION_INTERVAL = 1000*60;

    public static Intent newIntent(Context context){
        return new Intent(context,NotificationService.class);
    }

    public NotificationService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG,"Intent there");

        Intent i = WeatherFragment.newIntent(this);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);

        Notification notification = null;
        SharedPreferences pref = this.getSharedPreferences("setting",Context.MODE_PRIVATE);
        String unit = pref.getString("unit","摄氏度");
        String unit_text = "°";
        if (unit == "华氏度"){
            unit_text = "℉";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                        .setTicker("ticker")
                                        .setSmallIcon(R.drawable.a100)
                                        .setContentTitle("今日天气")
                                        .setContentText(pref.getString("city","北京")+
                                                "  天气："+pref.getString("text","")+
                                                ", 最高温度："+pref.getString("max_temp","")+unit_text+
                                                ", 最低温度："+pref.getString("min_temp","")+unit_text)
                                        .setContentIntent(pi)
                                        .setAutoCancel(true);

        NotificationManager notificationManager =(NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        //一定要设置channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("to-do"
                    , "待办消息",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{500});
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("to-do");
            notification = builder.build();
        } else {
            notification = builder.build();
        }

        notificationManager.notify(0,notification);
        System.out.println(notification);
    }

    public static void setServiceAlarm(Context context,boolean isOn){
        Intent i = NotificationService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    NOTIFICATION_INTERVAL,pi);
        }else{
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
}
