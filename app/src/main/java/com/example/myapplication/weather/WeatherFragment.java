package com.example.myapplication.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.GlobalData;
import com.example.myapplication.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherFragment extends Fragment {
    private static final String TAG="WeatherFragment";
    private RecyclerView mWeatherRecyclerView;
    //今日天气板块
    private TextView mTodayTime;
    private TextView mTodayMaxTemp;
    private TextView mTodayMinTemp;
    private ImageView mTodayIcon;
    private TextView mTodayIconDesc;

    private String location = "天津";       //记录当前的城市ID，如果有变化，需要刷新你页面
    private String temp_unit = "摄氏度";
    private String unit_text = "°";
    private List<WeatherItem> mItems = new ArrayList<>();   //七天的天气详情对象

    private Callbacks mCallbacks;

    private SQLiteDatabase mDatabase;

    public static WeatherFragment newInstance(){
        return new WeatherFragment();
    }

    /*回调函数，fragment调用activity的函数，这样点击列表后，方便出发事件，不应该用fragment执行其他fragment的细节*/
    public interface Callbacks{
        void onWeatherSelected(WeatherItem weatherItem);
    }

    public static Intent newIntent(Context context){
        Intent i =  new Intent(context, MainActivity1.class);
        return i;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting",Context.MODE_PRIVATE).edit();
        //设置其他选项，让manager知道fragment替activity进行处理
        setHasOptionsMenu(true);
        //创建数据库
        mDatabase = new DatabaseHelper(getActivity()).getWritableDatabase();
        new FetchItemsTask();  //异步执行，获取网站上的json内容
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather,container,false);

        mWeatherRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_weather_recycler_view);
        mWeatherRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //绑定今日天气模块
        mTodayTime = (TextView)v.findViewById(R.id.today_time);
        mTodayMaxTemp = (TextView)v.findViewById(R.id.today_max_temp);
        mTodayMinTemp = (TextView)v.findViewById(R.id.today_min_temp);
        mTodayIcon = (ImageView)v.findViewById(R.id.today_icon);
        mTodayIconDesc = (TextView)v.findViewById(R.id.today_icon_desc);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = getActivity().getSharedPreferences("setting",Context.MODE_PRIVATE);
        String city = pref.getString("city","北京");
        String unit = pref.getString("unit","摄氏度");
        String send = pref.getString("send","是");
        if(city!=location){     //在重新启动页面的时候，如果地址改变了，需要刷新
            location = city;
            new FetchItemsTask().execute();  //异步执行，获取网站上的json内容
            return;
        }
        if(unit!=temp_unit){     //在重新启动页面的时候，如果地址改变了，需要刷新
            temp_unit = unit;
            new FetchItemsTask().execute();  //异步执行，获取网站上的json内容
            return;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_weather,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences pref = getActivity().getSharedPreferences("setting",Context.MODE_PRIVATE);
        switch (item.getItemId()){
            case R.id.menu_setting:
                Intent intent = new Intent(getActivity(),SettingActivity.class);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //获取drawable图标资源的id
    public int getIconId(Context mContext, String icon){
        int i=  getResources().getIdentifier(icon, "drawable", mContext.getPackageName()) ;
        return i;
    }

    //    异步从URL获取天气对象
    private class FetchItemsTask extends AsyncTask<Void,Void, List<WeatherItem>> {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting",Context.MODE_PRIVATE).edit();
        SharedPreferences pref = getActivity().getSharedPreferences("setting",Context.MODE_PRIVATE);
        String city = pref.getString("city","天津");
        String unit = pref.getString("unit","摄氏度");
        String city_url = "https://geoapi.qweather.com/v2/city/lookup?location="+city+"&key=6b01a87a7f3347a19c7b6b84f2ba35e3";

        @SuppressLint("Range")
        @Override
        protected List<WeatherItem> doInBackground(Void... voids) {
            List<WeatherItem> weatherItems = new ArrayList<>();
            unit_text = "°";
            if(unit=="华氏度"){
                unit_text = "℉";
            }

            System.out.println("network is :"+isNetworkConnected(getActivity().getApplicationContext()));
            //如果没有网络连接，从数据库获取信息
            if(!isNetworkConnected(getActivity().getApplicationContext())){
                Cursor cursor = mDatabase.query ("weather",null,null,null,null,null,null);
                cursor.moveToFirst();
                for(int i=0;i<cursor.getCount();i++){
                    WeatherItem item = new WeatherItem();
                    item.setData(cursor.getString(cursor.getColumnIndex("date")));
                    item.setMax_temp(cursor.getString(cursor.getColumnIndex("max_temp")));
                    item.setMin_temp(cursor.getString(cursor.getColumnIndex("min_temp")));
                    item.setText(cursor.getString(cursor.getColumnIndex("text")));
                    item.setHumidity(cursor.getString(cursor.getColumnIndex("humidity")));
                    item.setPressure(cursor.getString(cursor.getColumnIndex("pressure")));
                    item.setWind(cursor.getString(cursor.getColumnIndex("wind")));
                    item.setIcon(cursor.getString(cursor.getColumnIndex("icon")));
                    weatherItems.add(item);
                    cursor.moveToNext();
                }
                return weatherItems;
            }

            //有网络
            JSONObject jsonObject = new FlickrFetcher().fetchCity(city_url);
            String locationID = "";
            try{
                locationID = jsonObject.getString("id");
                editor.putString("lat",jsonObject.getString("lat"));
                System.out.println("input lat is: "+jsonObject.getString("lat"));
                editor.putString("lon",jsonObject.getString("lon"));
                editor.commit();
            }catch (Exception e){}
            String url = "https://devapi.qweather.com/v7/weather/7d?location="+locationID+"&key=6b01a87a7f3347a19c7b6b84f2ba35e3";
            if(unit=="华氏度"){
                url = "https://devapi.qweather.com/v7/weather/7d?location="+locationID+"&key=6b01a87a7f3347a19c7b6b84f2ba35e3&unit=i";
            }
            weatherItems =  new FlickrFetcher().fetchItems(url);

            //删除原来的数据库，将获取的数据放入数据库
            mDatabase.execSQL("drop table weather");
            mDatabase.execSQL("create table weather (_id INTEGER PRIMARY KEY AUTOINCREMENT,date text,max_temp text,min_temp text,text text,humidity text,pressure text,wind text,icon text)");
            for(int i=0;i<weatherItems.size();i++){
                WeatherItem item = weatherItems.get(i);
                ContentValues values = new ContentValues();
                values.put("date",item.getData());
                values.put("max_temp",item.getMax_temp());
                values.put("min_temp",item.getMin_temp());
                values.put("text",item.getText());
                values.put("humidity",item.getHumidity());
                values.put("pressure",item.getPressure());
                values.put("wind",item.getWind());
                values.put("icon",item.getIcon());
                mDatabase.insert("weather",null,values);
            }
            return weatherItems;
        }

        @Override
        protected void onPostExecute(List<WeatherItem> weatherItems) {
            mItems = weatherItems;

            SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE).edit();

            //实现今天天气部分的UI
            WeatherItem item = mItems.get(0);
            mTodayTime.setText("Today,"+location);
            mTodayMaxTemp.setText(item.getMax_temp()+unit_text);
            editor.putString("max_temp",item.getMax_temp());
            mTodayMinTemp.setText(item.getMin_temp()+unit_text);
            editor.putString("min_temp",item.getMin_temp());
            mTodayIconDesc.setText(item.getText());
            editor.putString("text",item.getText());
            String icon = "a"+item.getIcon();
            int id = getIconId(getContext(),icon);
            Drawable drawable = getResources().getDrawable(id);
            mTodayIcon.setImageDrawable(drawable);

            //传递数据
            GlobalData.getInstance().setmin_temp(item.getMin_temp());
            GlobalData.getInstance().setmax_temp(item.getMax_temp());
            GlobalData.getInstance().setIconDesc(item.getText());

            editor.commit();

            //实现recyclerview部分的UI
            setupAdapter();

        }
    }

    public class WeatherHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private WeatherItem mWeatherItem;
        private TextView mItemMaxWeather;
        private TextView mItemMinWeather;
        private ImageView mItemIcon;
        private TextView mItemDate;
        private TextView mItemDesc;

        public void setDay(String day){
            mItemDate.setText(day);
        }

        public WeatherHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            mItemMaxWeather = (TextView) itemView.findViewById(R.id.item_max_weather);
            mItemMinWeather = (TextView) itemView.findViewById(R.id.item_min_weather);
            mItemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            mItemDate = (TextView) itemView.findViewById(R.id.item_date);
            mItemDesc = (TextView) itemView.findViewById(R.id.item_desc);
        }

        public void bindWeatherItem(WeatherItem weatherItem){
            mWeatherItem = weatherItem;
            mItemMaxWeather.setText(weatherItem.getMax_temp()+unit_text);
            mItemMinWeather.setText(weatherItem.getMin_temp()+unit_text);
            String icon = "a"+weatherItem.getIcon();
            int id = getIconId(getContext(),icon);
            Drawable drawable = getResources().getDrawable(id);
            mItemIcon.setImageDrawable(drawable);
            mItemDate.setText(weatherItem.getData());
            mItemDesc.setText(weatherItem.getText());
        }

        //点击列表，跳转到详情页面
        @Override
        public void onClick(View v) {

            Log.i(TAG,"click");
            mCallbacks.onWeatherSelected(mWeatherItem);
        }
    }

    private class WeatherAdaper extends RecyclerView.Adapter<WeatherHolder>{
        private List<WeatherItem> mWeatherList = new ArrayList<>();

        public WeatherAdaper(List<WeatherItem> items){
            mWeatherList = items;
        }

        @Override
        public int getItemCount() {
            return mWeatherList.size();
        }

        @Override
        public WeatherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //实例化mars_item布局
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.weather_item,parent,false);
            return new WeatherHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WeatherHolder holder, int position) {
            WeatherItem weatherItem = mWeatherList.get(position);
            holder.bindWeatherItem(weatherItem);
            if(position==0){
                holder.setDay("今天");
            }
            if(position==1){
                holder.setDay("明天");
            }
        }
    }

    private void setupAdapter(){
        if(isAdded()){
            mWeatherRecyclerView.setAdapter(new WeatherAdaper(mItems));
        }
    }

    //判断网络是否连接
    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetwork() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
