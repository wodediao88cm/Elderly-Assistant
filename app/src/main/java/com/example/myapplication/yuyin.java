package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class yuyin extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private SharedPreferences mSharedPreferences;//缓存

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String language = "zh_cn";//识别语言

    private TextView tvResult;//识别结果
    private Button btnStart;//开始识别
    private String resultType = "json";//结果内容数据格式
//    private Button go;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuyin);
        tvResult = findViewById(R.id.tv_result);
        btnStart = findViewById(R.id.btn_start);
//        go = findViewById(R.id.go);
        btnStart.setOnClickListener(this);
        initPermission();//权限请求
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(yuyin.this, mInitListener);
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(yuyin.this, mInitListener);
        mSharedPreferences = getSharedPreferences("ASR",
                Activity.MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        if( null == mIat ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            showMsg( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
            return;
        }

        mIatResults.clear();//清除数据
        setParam(); // 设置参数
        mIatDialog.setListener(mRecognizerDialogListener);//设置监听
        mIatDialog.show();// 显示对话框

    }


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showMsg("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };


    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {

            printResult(results);//结果数据解析
//            printButton(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showMsg(error.getPlainDescription(true));
        }

    };

    /**
     * 数据解析
     *
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        tvResult.setText(resultBuffer.toString());//听写结果显示
        if(resultBuffer.toString().contains("扫码")){
            Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=10000007");
            Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
            startActivity(intent10);
        }
        else if(resultBuffer.toString().contains("付款")){
            Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=20000056");
            Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
            startActivity(intent10);

        }
        else if(resultBuffer.toString().contains("收款")){
            Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=20000123");
            Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
            startActivity(intent10);

        }
        else if(resultBuffer.toString().contains("紧急")){
            // 从SharedPreferences中获取保存的电话号码
            SharedPreferences preferences = getSharedPreferences("emergency_setting", MODE_PRIVATE);
            String savedEmergencyNumber = preferences.getString("emergency_number", "");

            if (!savedEmergencyNumber.isEmpty()) {
                // 调用系统方法拨打电话
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + savedEmergencyNumber));
                startActivity(intent);}
        }
        else if(resultBuffer.toString().contains("抖音")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("snssdk1128://feed?refer=web&gd_label={{gd_label}}"));
            startActivity(intent);
        }
        else if(resultBuffer.toString().contains("电话")) {
            Intent intent = new Intent(Intent.ACTION_CALL_BUTTON);
            startActivity(intent);
        }
        else if(resultBuffer.toString().contains("浏览器")) {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            startActivity(intent);
        }
        else if(resultBuffer.toString().contains("微信")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("weixin://"));
            startActivity(intent);
        }
    }



//    private void printButton(RecognizerResult results) {
//        String text = JsonParser.parseIatResult(results.getResultString());
//
//        String sn = null;
//        // 读取json结果中的sn字段
//        try {
//            JSONObject resultJson = new JSONObject(results.getResultString());
//            sn = resultJson.optString("sn");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        mIatResults.put(sn, text);
//
//        StringBuffer resultBuffer = new StringBuffer();
//        for (String key : mIatResults.keySet()) {
//            resultBuffer.append(mIatResults.get(key));
//        }
//
////        go.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
//                if(resultBuffer.toString().contains("扫码")){
//                    Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=10000007");
//                    Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
//                    startActivity(intent10);
//                }
//                else if(resultBuffer.toString().contains("付款")){
//                    Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=20000056");
//                    Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
//                    startActivity(intent10);
//
//                }
//                else if(resultBuffer.toString().contains("收款")){
//                    Uri uri10 = Uri.parse("alipayqr://platformapi/startapp?saId=20000123");
//                    Intent intent10 = new Intent(Intent.ACTION_VIEW, uri10);
//                    startActivity(intent10);
//
//                }
//                else if(resultBuffer.toString().contains("打电话")){
//                    // 从SharedPreferences中获取保存的电话号码
//                    SharedPreferences preferences = getSharedPreferences("emergency_setting", MODE_PRIVATE);
//                    String savedEmergencyNumber = preferences.getString("emergency_number", "");
//
//                    if (!savedEmergencyNumber.isEmpty()) {
//                        // 调用系统方法拨打电话
//                        Intent intent = new Intent();
//                        intent.setAction(Intent.ACTION_CALL);
//                        intent.setData(Uri.parse("tel:" + savedEmergencyNumber));
//                        startActivity(intent);}
//                }
//                else if(resultBuffer.toString().contains("便携") || resultBuffer.toString().contains("操作") ){
//                    Intent intent1=new Intent(yuyin.this,yijiantong.class);
//                    startActivity(intent1);
//
//                }
//                else if(resultBuffer.toString().contains("定位") || resultBuffer.toString().contains("位置") ){
//                    Intent intent1=new Intent(yuyin.this,zhaoxun.class);
//                    startActivity(intent1);
//                }
//                else if(resultBuffer.toString().contains("天气")){
//                    Intent intent1=new Intent(yuyin.this, MainActivity1.class);
//                    startActivity(intent1);
//                }
//                else if(resultBuffer.toString().contains("运动")){
//                    Intent intent1=new Intent(yuyin.this, step.class);
//                    startActivity(intent1);
//                }
//                else if(resultBuffer.toString().contains("AI")|| resultBuffer.toString().contains("助手") ){
//                    Intent intent1=new Intent(yuyin.this, xunfei.class);
//                    startActivity(intent1);
//                }
//                else if(resultBuffer.toString().contains("手势") || resultBuffer.toString().contains("识别") ){
//                    Intent intent1=new Intent(yuyin.this, Gesture_MainActivity.class);
//                    startActivity(intent1);
//                }
//                else if(resultBuffer.toString().contains("摔倒") || resultBuffer.toString().contains("检测") ){
//                    Intent intent1=new Intent(yuyin.this, fall_detection.class);
//                    startActivity(intent1);
//                }
//            }
//            });


//
//    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {

            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 提示消息
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(yuyin.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }


    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    /**
     * 权限申请回调，可以作进一步处理
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
