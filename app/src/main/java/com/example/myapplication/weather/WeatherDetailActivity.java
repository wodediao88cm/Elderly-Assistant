package com.example.myapplication.weather;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class WeatherDetailActivity extends SingleFragmentActivity{
    public static final String EXTRA_WEATHER_ITEM = "com.example.weather.weatherItem";

    @Override
    protected Fragment createFragment() {
        WeatherItem weatherItem = (WeatherItem)getIntent().getSerializableExtra(EXTRA_WEATHER_ITEM);
        return WeatherDetailFragment.newInstance(weatherItem);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.weather_splicesheet;
    }

    @Override
    protected int getFragmentId() {
        return R.id.fragment_container;
    }

    public static Intent newIntent(Context packageContext, WeatherItem weatherItem){
        Intent intent = new Intent(packageContext,WeatherDetailActivity.class);
        intent.putExtra(EXTRA_WEATHER_ITEM,weatherItem);
        return intent;
    }
}
