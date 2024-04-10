package com.example.myapplication.gesture;

/**
 * 这是一个用于处理TensorFlow模型输出的类
 */
public class TensorflowResult {
    // 模型标签，用于将输出索引映射到标签名称
    private static String[] labels = new String[]{
            "A", "B", "C",
            "D", "E", "F",
            "G", "H", "I",
            "J"
    };

    // 模型输出结果存储在这个数组中
    private float[] output = new float[labels.length];

    // 获取模型输出结果数组
    public float[] getOutput() {
        return output;
    }

    // 查找数组中值最大的元素的索引
    private int argmax(float[] array) {
        int index = 0;
        float largest = Integer.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > largest) {
                largest = array[i];
                index = i;
            }
        }
        return index;
    }

    // 返回具有最高输出结果的标签和对应的准确度
    public String getTopInfo2() {
        int topIndex = argmax(output);
        String label = labels[topIndex];
        return label;
        //float accuracy = output[topIndex];
        //return label + ", " + (String.valueOf(accuracy));
    }
}
