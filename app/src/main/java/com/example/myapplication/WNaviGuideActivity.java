package com.example.myapplication;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener;
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener;
import com.baidu.mapapi.walknavi.adapter.IWTTSPlayer;
import com.baidu.mapapi.walknavi.model.RouteGuideKind;
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener;
import com.baidu.platform.comapi.walknavi.widget.ArCameraView;

public class WNaviGuideActivity extends Activity {
    private final static String TAG = WNaviGuideActivity.class.getSimpleName();

    private WalkNavigateHelper mNaviHelper;

    private boolean isPreSPEAKtotal = true;
    private String orient = "";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviHelper.quit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviHelper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();
    }


    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x001) {
                ChangeState();
                handler.sendEmptyMessageDelayed(0x001, 45000);
            }
            if (msg.what == 0x002) {
                handler.sendEmptyMessageDelayed(0x002, 30000);
            }
            if (msg.what == 0x003) {

                startActivity(new Intent(WNaviGuideActivity.this, MainActivity.class));
            }
        }
    };

    private void ChangeState() {
        isPreSPEAKtotal = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNaviHelper = WalkNavigateHelper.getInstance();
        try {
            View view = mNaviHelper.onCreate(WNaviGuideActivity.this);
            if (view != null) {
                setContentView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置步行导航状态监听
        mNaviHelper.setWalkNaviStatusListener(new IWNaviStatusListener() {
            @Override
            public void onWalkNaviModeChange(int mode, WalkNaviModeSwitchListener listener) {
                Log.d("======", "onWalkNaviModeChange : " + mode);
                mNaviHelper.switchWalkNaviMode(WNaviGuideActivity.this, mode, listener);
            }

            /* @Description: 这个是在退出导航时自动调用的方法，在这里要把对象进行释放，避免空对象的产生
             * @Author: LiY                                                                                                                                                                         ue
             */
            @Override
            public void onNaviExit() {
                Log.d("======", "onNaviExit");
                handler.removeMessages(0x001);
                handler.removeMessages(0x002);
                handler.removeMessages(0x003);
            }
        });


        mNaviHelper.setTTsPlayer(new IWTTSPlayer() {
            @Override
            public int playTTSText(final String s, boolean b) {
                Log.d(TAG, "tts: " + s);
                return 0;
            }
        });


        boolean startResult = mNaviHelper.startWalkNavi(WNaviGuideActivity.this);
        Log.e(TAG, "startWalkNavi result : " + startResult);
        //设置路线指引监听
        mNaviHelper.setRouteGuidanceListener(this, new

                IWRouteGuidanceListener() {

                    @Override
                    public void onRouteGuideIconUpdate(Drawable icon) {

                    }

                    @Override
                    public void onRouteGuideKind(RouteGuideKind routeGuideKind) {
                        Log.d("======", "onRouteGuideKind: " + routeGuideKind);
                        if (routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_PassRoad_Left || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_PassRoad_Right || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_Right_PassRoad_Front || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_Right_PassRoad_UTurn)

                            if (routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_RightDiagonal_PassRoad_Front || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_RightDiagonal_PassRoad_Left || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_RightDiagonal_PassRoad_Left_Front || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_RightDiagonal_PassRoad_Right || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_RightDiagonal_PassRoad_Right_Back || routeGuideKind == RouteGuideKind.NE_Maneuver_Kind_RightDiagonal_PassRoad_Right_Front){
                                Log.d("========", "onRouteGuideKind: aaaaaa");
                            }

                    }

                    /**
                     * @Description: 诱导信息
                     */
                    @Override
                    public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence
                            charSequence1) {
                        Log.d(TAG, "onRoadGuideTextUpdate   charSequence=: " + charSequence + "   charSequence1 = : " +
                                charSequence1);
                        orient = charSequence.toString() + charSequence1.toString();

                    }


                    @Override
                    public void onRemainDistanceUpdate(CharSequence charSequence) {
                        Log.d(TAG, "onRemainDistanceUpdate: charSequence = :" + charSequence);
                        if (isPreSPEAKtotal) {
                        }
                    }


                    @Override
                    public void onRemainTimeUpdate(CharSequence charSequence) {
                        Log.d(TAG, "onRemainTimeUpdate: charSequence = :" + charSequence);
                        if (isPreSPEAKtotal) {
                            isPreSPEAKtotal = false;
                        }
                    }


                    @Override
                    public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onGpsStatusChange: charSequence = :" + charSequence);

                    }


                    @Override
                    public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onRouteFarAway: charSequence = :" + charSequence);
                    }


                    @Override
                    public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onRoutePlanYawing: charSequence = :" + charSequence);

                    }


                    @Override
                    public void onReRouteComplete() {
                    }

                    @Override
                    public void onArriveDest() {
                        handler.sendEmptyMessageDelayed(0x003, 6000);
                    }

                    @Override
                    public void onIndoorEnd(Message msg) {

                    }

                    @Override
                    public void onFinalEnd(Message msg) {

                    }

                    @Override
                    public void onVibrate() {

                    }
                });
        handler.sendEmptyMessage(0x001);
        handler.sendEmptyMessage(0x002);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ArCameraView.WALK_AR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(WNaviGuideActivity.this, "没有相机权限,请打开后重试", Toast.LENGTH_SHORT).show();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaviHelper.startCameraAndSetMapView(WNaviGuideActivity.this);
            }
        }
    }
}


