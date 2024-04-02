package com.example.myapplication.weather;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class MainActivity1 extends SingleFragmentActivity implements WeatherFragment.Callbacks{
    @Override
    protected Fragment createFragment() {
        return WeatherFragment.newInstance();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected int getFragmentId() {
        return R.id.fragment_container;
    }

    public void onWeatherSelected(WeatherItem weatherItem){   //平板中选中天气
        if(findViewById(R.id.detail_container)==null){
            Intent intent = WeatherDetailActivity.newIntent(this,weatherItem);
            startActivity(intent);
        }else{
            Fragment newDetail = WeatherDetailFragment.newInstance(weatherItem);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container,newDetail)
                    .commit();
        }
    }
}
