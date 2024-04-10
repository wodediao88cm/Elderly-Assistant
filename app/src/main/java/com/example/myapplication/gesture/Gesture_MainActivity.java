package com.example.myapplication.gesture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.gesture_palette.PaletteView;


public class Gesture_MainActivity extends Activity implements View.OnClickListener {
    private static final String NOTMNIST_MODEL_FILE = "file:///android_asset/not-mnist-a-j-tf1.2_9718.pb";

    private TensorFlowDetector mDetector;
    private TextView mTextView;
    private PaletteView mPaletteView;
    private Bitmap mCurrentBitmap28x28;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture);
        mTextView = (TextView) findViewById(R.id.text);
        mPaletteView = (PaletteView) findViewById(R.id.palette);
        findViewById(R.id.undo).setOnClickListener(this);
        findViewById(R.id.redo).setOnClickListener(this);
        findViewById(R.id.pen).setOnClickListener(this);
        findViewById(R.id.eraser).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        View fab = findViewById(R.id.feed);
        mTextView.setText("A:打开支付宝;B:打开抖音;C:打开微信;D:打开电话;E:打开浏览器 ");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mCurrentBitmap28x28 = mPaletteView.buildBitmap();
                    String charString = mDetector.decodeBitmap(mCurrentBitmap28x28);
                    if(charString.equals("A")){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("alipayqr://platformapi/startapp"));
                        startActivity(intent);
                    } else if (charString.equals("B")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("snssdk1128://feed?refer=web&gd_label={{gd_label}}"));
                        startActivity(intent);
                    }else if (charString.equals("C")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("weixin://"));
                        startActivity(intent);
                    }
                    else if (charString.equals("D")) {
                        Intent intent = new Intent(Intent.ACTION_CALL_BUTTON);
                        startActivity(intent);
                    }
                    else if (charString.equals("E")) {
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mTextView.setText(e.getMessage());
                }
            }
        });

        mDetector = TensorFlowDetector.create(
                getAssets(),
                NOTMNIST_MODEL_FILE,
                28,
                "input",
                "out_softmax");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undo:
                mPaletteView.undo();
                break;
            case R.id.redo:
                mPaletteView.redo();
                break;
            case R.id.pen:
                v.setSelected(true);
                mPaletteView.setMode(PaletteView.Mode.DRAW);
                break;
            case R.id.eraser:
                v.setSelected(true);
                mPaletteView.setMode(PaletteView.Mode.ERASER);
                break;
            case R.id.clear:
                mPaletteView.clear();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDetector.onDestroy();
    }
}
