package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class Map_Activity extends Activity {
    private MapView MapView = null;
    private BaiduMap BaiduMap;
    private LocationClient LocationClient;
    private BitmapDescriptor mMarker;
    private boolean ifFrist = true;
    TextView tv_Lat;  //纬度
    TextView tv_Lon;  //经度
    TextView tv_Add;  //地址
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        MapView = findViewById(R.id.bmapview);
        BaiduMap = MapView.getMap();
        LocationClient = new LocationClient(this);

        MyLocationListener myLocationListener = new MyLocationListener();
        LocationClient.registerLocationListener(myLocationListener);
        tv_Lat = findViewById(R.id.tv_Lat);
        tv_Lon = findViewById(R.id.tv_Lon);
        tv_Add = findViewById(R.id.tv_Add);
        //覆盖物 用于显示当前位置
        mMarker = BitmapDescriptorFactory.fromResource(com.baidu.bikenavi.R.drawable.icon_start_walk);
        BaiduMap.setMyLocationEnabled(true);
        LocationClientOption option = new LocationClientOption();

        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        LocationClient.setLocOption(option);
        LocationClient.start();

    }

//    public static String loca;
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置

            tv_Lat.setText(location.getLatitude()+"");
            tv_Lon.setText(location.getLongitude()+"");
            tv_Add.setText(location.getAddrStr());

            GlobalData.getInstance().setLoca(location.getAddrStr());

            if (location == null || MapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .direction(location.getDirection())
                    .build();
            BaiduMap.setMyLocationData(locData);

            MyLocationConfiguration configuration = new MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, false, mMarker);

            BaiduMap.setMyLocationConfiguration(configuration);

            if (ifFrist) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll);
                builder.zoom(18.0f);
                BaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                ifFrist = false;
            }

        }

    }


//    @Override
//    protected void onResume() {
//        MapView.onResume();
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        MapView.onPause();
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        LocationClient.stop();
//        BaiduMap.setMyLocationEnabled(false);
//        MapView.onDestroy();
//        MapView = null;
//        super.onDestroy();
//    }
}
