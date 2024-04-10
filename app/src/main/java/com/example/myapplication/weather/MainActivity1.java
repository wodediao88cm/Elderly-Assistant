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
        return R.layout.weather_splicesheet;
    }

    @Override
    protected int getFragmentId() {
        return R.id.fragment_container;
    }

    public void onWeatherSelected(WeatherItem weatherItem){
        Intent intent = WeatherDetailActivity.newIntent(this, weatherItem);
        startActivity(intent);
    }
}
