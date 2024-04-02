package com.example.myapplication.weather;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class WeatherDetailFragment extends Fragment {
    private WeatherItem mWeatherItem;  //用来设置UI

    private TextView mDeatilDate;
    private TextView mDetailMaxTemp;
    private TextView mDetailMinTemp;
    private ImageView mDetialIcon;
    private TextView mDetailDesc;
    private TextView mDetailHumidity;
    private TextView mDetailPressure;
    private TextView mDetailWind;

    private static final String TAG = "WeatherDetailFragment";
    private static final String ARG_ITEM = "args_item";
    private String unit_text = "°";

    public static WeatherDetailFragment newInstance(WeatherItem weatherItem) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM,weatherItem);
        WeatherDetailFragment fragment =  new WeatherDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //获取drawable图标资源的id
    public int getIconId(Context mContext, String icon){
        int i=  getResources().getIdentifier(icon, "drawable", mContext.getPackageName()) ;
        if(i>0){
            Log.i(TAG,"Success to get drawable resoure");
        }else{
            Log.i(TAG,"Fail to get drawable resoure");
        }
        return i;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mWeatherItem = (WeatherItem) getArguments().getSerializable(ARG_ITEM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_detail,container,false);
        if(getActivity().getSharedPreferences("setting",Context.MODE_PRIVATE).getString("unit","摄氏度")=="华氏度"){
            unit_text = "℉";
        }

        mDeatilDate = (TextView) v.findViewById(R.id.detail_date);
        mDetailMaxTemp = (TextView) v.findViewById(R.id.detail_max_temp);
        mDetailMinTemp = (TextView) v.findViewById(R.id.detail_min_temp);
        mDetialIcon = (ImageView) v.findViewById(R.id.detail_icon);
        mDetailHumidity = (TextView) v.findViewById(R.id.detail_humidity);
        mDetailPressure = (TextView) v.findViewById(R.id.detail_pressure);
        mDetailWind = (TextView) v.findViewById(R.id.detail_wind);
        mDetailDesc = (TextView) v.findViewById(R.id.detail_desc);

        mDeatilDate.setText(mWeatherItem.getData());
        mDetailMaxTemp.setText(mWeatherItem.getMax_temp()+unit_text);
        mDetailMinTemp.setText(mWeatherItem.getMin_temp()+unit_text);
        String icon = "a"+mWeatherItem.getIcon();
        int id = getIconId(getContext(),icon);
        Drawable drawable = getResources().getDrawable(id);
        mDetialIcon.setImageDrawable(drawable);
        mDetailHumidity.setText("Humidity: "+mWeatherItem.getHumidity()+" %");
        mDetailPressure.setText("Pressure: "+mWeatherItem.getPressure()+" hPa");
        mDetailWind.setText("Wind: "+mWeatherItem.getWind()+" km/h SE");
        mDetailDesc.setText(mWeatherItem.getText());

        return v;
    }

    public String createMessage(){
        String message = "";
        message += "今天的天气状况为："+mWeatherItem.getText();
        message += "    今天的最高温度是： "+mWeatherItem.getMax_temp();
        message += "    今天的最低温度是： "+mWeatherItem.getMin_temp();
        message += "    今天的湿度为： "+mWeatherItem.getHumidity();
        message += "    今天的风速为："+mWeatherItem.getWind();
        message += "    今天的气压为："+mWeatherItem.getPressure();
        message += "    希望您拥有美好的一天!";
        return message;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(getActivity().findViewById(R.id.fragment_container)==null){    //是手机模式
            inflater.inflate(R.menu.fragment_detail,menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_setting:
                Intent intent = new Intent(getActivity(),SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT,createMessage());
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
