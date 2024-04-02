package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.iflytek.sparkchain.core.LLM;
import com.iflytek.sparkchain.core.LLMCallbacks;
import com.iflytek.sparkchain.core.LLMConfig;
import com.iflytek.sparkchain.core.LLMError;
import com.iflytek.sparkchain.core.LLMEvent;
import com.iflytek.sparkchain.core.LLMResult;
import com.iflytek.sparkchain.core.Memory;
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
public class xunfei extends AppCompatActivity {
    private static final String TAG = "AEE";
    private Button startChatBtn;
    private TextView chatText;
    private EditText inputText;
    // 设定flag，在输出未完成时无法进行发送
    private boolean sessionFinished = true;

    private LLM llm;
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
    private TextView tvResult1;
    LLMCallbacks llmCallbacks = new LLMCallbacks() {
        @Override
        public void onLLMResult(LLMResult llmResult, Object usrContext) {
            Log.d(TAG,"onLLMResult\n");
            String content = llmResult.getContent();
            Log.e(TAG,"onLLMResult:" + content);
            int status = llmResult.getStatus();
            if(content != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatText.append(content);
                        toend();
                    }
                });
            }
            if(usrContext != null) {
                String context = (String)usrContext;
                Log.d(TAG,"context:" + context);
            }
            if(status == 2){
                int completionTokens = llmResult.getCompletionTokens();
                int promptTokens = llmResult.getPromptTokens();//
                int totalTokens = llmResult.getTotalTokens();
                Log.e(TAG,"completionTokens:" + completionTokens + "promptTokens:" + promptTokens + "totalTokens:" + totalTokens);
                sessionFinished = true;
            }
        }

        @Override
        public void onLLMEvent(LLMEvent event, Object usrContext) {
            Log.d(TAG,"onLLMEvent\n");
            Log.w(TAG,"onLLMEvent:" + " " + event.getEventID() + " " + event.getEventMsg());
        }

        @Override
        public void onLLMError(LLMError error, Object usrContext) {
            Log.d(TAG,"onLLMError\n");
            Log.e(TAG,"errCode:" + error.getErrCode() + "errDesc:" + error.getErrMsg());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatText.append("错误:" + " err:" + error.getErrCode() + " errDesc:" + error.getErrMsg() + "\n");
                }
            });
            if(usrContext != null) {
                String context = (String)usrContext;
                Log.d(TAG,"context:" + context);
            }
            sessionFinished = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bigmoudle_xunfei);
        tvResult = findViewById(R.id.chat_input_text);
        btnStart = findViewById(R.id.chat_speak_btn);
        initView();
        initButtonClickListener();
        initSDK();
        btnStart.setOnClickListener(this::onClick);
        // tvResult1=findViewById(R.id.tv_result1);
        initPermission();//权限请求
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(xunfei.this, mInitListener);
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(xunfei.this, mInitListener);
        mSharedPreferences = getSharedPreferences("ASR",
                Activity.MODE_PRIVATE);

    }

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
            printButton(results);

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

    }
    private void printButton(RecognizerResult results) {
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

    }

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
        Toast.makeText(xunfei.this, msg, Toast.LENGTH_SHORT).show();
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unInitSDK();

        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    private void setLLMConfig(){
        Log.d(TAG,"setLLMConfig");
        LLMConfig llmConfig = LLMConfig.builder();
        llmConfig.domain("generalv3.5");
        llmConfig.url("ws(s)://spark-api.xf-yun.com/v3.5/chat");//必填
        //memory有两种，windows_memory和tokens_memory，二选一即可
        Memory window_memory = Memory.windowMemory(5);
        llm = new LLM(llmConfig,window_memory);

//        Memory tokens_memory = Memory.tokenMemory(8192);
//        llm = new LLM(llmConfig,tokens_memory);

        llm.registerLLMCallbacks(llmCallbacks);
    }

    private void initSDK() {
        // 初始化SDK，Appid等信息在清单中配置
        SparkChainConfig sparkChainConfig = SparkChainConfig.builder();
        sparkChainConfig.appID("85f780f1")
                .apiKey("adbfcb135a4c703b2922d2a328c741f0")
                .apiSecret("ODNjYjdjMzM5YjYzZjVkZWZhNjRkMzhk")//应用申请的appid三元组
                .logLevel(0);

        int ret = SparkChain.getInst().init(getApplicationContext(),sparkChainConfig);
        if(ret == 0){
            Log.d(TAG,"SDK初始化成功：" + ret);
            showToast(xunfei.this, "SDK初始化成功：" + ret);
            setLLMConfig();
        }else{
            Log.d(TAG,"SDK初始化失败：其他错误:" + ret);
            showToast(xunfei.this, "SDK初始化失败-其他错误：" + ret);
        }
    }



    private void startChat() {
        if(llm == null){
            Log.e(TAG,"startChat failed,please setLLMConfig before!");
            return;
        }
        String usrInputText = inputText.getText().toString();
        Log.d(TAG,"用户输入：" + usrInputText);
        if(usrInputText.length() >= 1)
            chatText.append("\n输入:\n    " + usrInputText  + "\n");

        String myContext = "myContext";

        int ret = llm.arun(usrInputText,myContext);
        if(ret != 0){
            Log.e(TAG,"SparkChain failed:\n" + ret);
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputText.setText("");
                chatText.append("输出:\n    ");
            }
        });

        sessionFinished = false;
        return;
    }

    private void unInitSDK() {
        SparkChain.getInst().unInit();
    }

    private void initButtonClickListener() {
        startChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sessionFinished){
                    startChat();
                    toend();
                } else {
                    Toast.makeText(xunfei.this, "Busying! Please Wait", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 监听文本框点击时间,跳转到底部
        inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toend();
            }
        });
    }

    private void initView() {
        startChatBtn = findViewById(R.id.chat_start_btn);
        chatText = findViewById(R.id.chat_output_text);
        inputText = findViewById(R.id.chat_input_text);

        chatText.setMovementMethod(new ScrollingMovementMethod());

        GradientDrawable drawable = new GradientDrawable();
        // 设置圆角弧度为5dp
        drawable.setCornerRadius(dp2px(this, 5f));
        // 设置边框线的粗细为1dp，颜色为黑色【#000000】
        drawable.setStroke((int) dp2px(this, 1f), Color.parseColor("#000000"));
        inputText.setBackground(drawable);
    }

    private float dp2px(Context context, float dipValue) {
        if (context == null) {
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (float) (dipValue * scale + 0.5f);
    }

    public static void showToast(final Activity context, final String content){

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int random = (int) (Math.random()*(1-0)+0);
                Toast.makeText(context,content,random).show();
            }
        });

    }

    public void toend(){
        int scrollAmount = chatText.getLayout().getLineTop(chatText.getLineCount()) - chatText.getHeight();
        if (scrollAmount > 0) {
            chatText.scrollTo(0, scrollAmount+10);
        }
    }

}
