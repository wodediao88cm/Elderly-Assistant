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

/**
 * 根据您提供的代码，这是一个用于从 Flickr API 获取天气数据的 FlickFetcher 类。让我来帮助您解释一下这段代码：
 *
 * getUrlBytes 方法用于从指定的 URL 地址获取数据并以字节数组的形式返回。它使用了HttpURLConnection` 来进行网络连接，并处理了连接状态等操作。
 * getUrlString 方法则是将 getUrlBytes 方法获取的字节数组转换为字符串并返回。
 * fetchItems 方法用于从指定的 URL 获取天气数据，并将返回的 JSON 数据解析成 WeatherItem 对象的 List。它使用了 getUrlString 方法来获取 JSON 数据，并调用 parseItems 方法将 JSON 数据解析成 WeatherItem 对象。
 * fetchCity 方法似乎用于获取城市的一些信息，它也使用了 getUrlString 方法来获取 JSON 数据，并解析出城市的信息。
 * 总体而言，这段代码看起来是在使用 Flickr API 获取天气数据，并且将 JSON 数据解析成 WeatherItem 对象。同时也包含了一些日志记录和异常处理。
 *
 * 如果您有任何其他问题，或者需要进一步解释，请随时告诉我。我会很乐意为您提供帮助。
 */
public class FlickrFetcher {
    public static final String TAG = "FlickrFetcher";

    public byte[] getUrlBytes(String urlSpec) throws Exception{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
