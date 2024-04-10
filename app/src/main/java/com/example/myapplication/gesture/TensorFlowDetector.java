package com.example.myapplication.gesture;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import java.nio.ByteBuffer;


public class TensorFlowDetector {
    private String inputName;
    private int inputSize;
    private String[] outputNames;

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowDetector() {
    }

    public static TensorFlowDetector create(
            final AssetManager assetManager,
            final String modelFilename,
            final int inputSize,
            final String inputName,
            final String outputName) {
        TensorFlowDetector d = new TensorFlowDetector();
        d.inputName = inputName;
        d.inputSize = inputSize;
        d.outputNames = outputName.split(",");
        d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        return d;
    }

    public String decodeBitmap(final Bitmap bitmap) throws Exception{
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getRowBytes());
        bitmap.copyPixelsToBuffer(buffer);
        byte[] bytes = buffer.array();

        float[] floatValues = normalizedPixels(bytes, bitmap.getWidth(), bitmap.getHeight());

        inferenceInterface.feed("keep", new float[]{1f});
        inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 1);
        inferenceInterface.run(outputNames, false);

        TensorflowResult result = new TensorflowResult();
        inferenceInterface.fetch(outputNames[0], result.getOutput());

        Log.v("TensorFlowDetector", result.toString());
        return result.getTopInfo2();
    }


    /**
     * 正则化数据，均值为0，标准差为0.5
     * @param pixels 字节码像素
     * @param width 宽度
     * @param height 高度
     * @return 正则后数据 -0.5~0.5
     */
    private float[] normalizedPixels(byte[] pixels, int width, int height) {
        float[] floatValues = new float[width * height];
        for (int i = 0; i < pixels.length; ++i) {
            floatValues[i] = byteToGary(pixels[i]);
            floatValues[i] -= (255.0 / 2.0);
            floatValues[i] /= 255.0;
        }
        return floatValues;
    }


    /**
     * 灰度像素的转化为0-255数值
     * @param pixels 灰度图像素
     * @return 0~255
     */
    private int byteToGary(int pixels){
        int pixelsElement = pixels & 0xff;
        return pixelsElement;
    }

    protected void onDestroy() {
        inferenceInterface.close();
    }
}
