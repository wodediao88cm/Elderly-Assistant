package com.example.myapplication.weather;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//获取数据的二进制数组/字符串形式
//获取JSON数组
//解析JSON数组，变成WeatherItem的数组形式

public class FlickrFetcher {
    public static final String TAG = "FlickrFetcher";

    public byte[] getUrlBytes(String urlSpec) throws Exception{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setInstanceFollowRedirects(false);    //跟踪重定向
//        String redirect = connection.getHeaderField("Location");
//        if (redirect != null){
//            connection = (HttpURLConnection)new URL(redirect).openConnection();
//        }

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + " :with "+urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec)throws Exception{
        return new String(getUrlBytes(urlSpec));
    }

    public List<WeatherItem> fetchItems(String urlSpec){
        List<WeatherItem> items = new ArrayList<>();
        try{
            String jsonString = getUrlString(urlSpec);
            Log.i(TAG,"result: "+jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items,jsonBody);
        }catch (Exception e){
            Log.i(TAG,"Failed!"+e);
        }
        return items;
    }

    public JSONObject fetchCity(String urlSpec){
        String locationID = "";
        JSONObject jsonObject = null;
        try{
            String jsonString = getUrlString(urlSpec);
            Log.i(TAG,"result: "+jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONArray locationJsonArray = jsonBody.getJSONArray("location");
            jsonObject = locationJsonArray.getJSONObject(0);
//            locationID = locationJsonArray.getJSONObject(0).getString("id");
//            String lat = locationJsonArray.getJSONObject(0).getString("lat");
//            String lon = locationJsonArray.getJSONObject(0).getString("lon");
        }catch (Exception e){
            Log.i(TAG,"Failed!"+e);
        }
        return jsonObject;
    }

    private void parseItems(List<WeatherItem> items,JSONObject jsonBody) throws Exception{   //将从url获得的json转换成MarsItem
        JSONArray weatherJsonArray = jsonBody.getJSONArray("daily");
        for(int i=0;i<weatherJsonArray.length();i++){
            JSONObject weatherJsonObject = weatherJsonArray.getJSONObject(i);
            WeatherItem item = new WeatherItem();
            item.setData(weatherJsonObject.getString("fxDate"));
            item.setMax_temp(weatherJsonObject.getString("tempMax"));
            item.setMin_temp(weatherJsonObject.getString("tempMin"));
            item.setText(weatherJsonObject.getString("textDay"));
            item.setHumidity(weatherJsonObject.getString("humidity"));
            item.setPressure(weatherJsonObject.getString("pressure"));
            item.setWind(weatherJsonObject.getString("windSpeedDay"));
            item.setIcon(weatherJsonObject.getString("iconDay"));
            items.add(item);
        }
    }
}
