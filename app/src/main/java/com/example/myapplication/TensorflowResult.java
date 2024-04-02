package com.example.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    // 获取输出结果对应的标签名称
    public String getLabel() {
        return labels[argmax(output)]; // 返回对应输出结果的标签
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
        float accuracy = output[topIndex];
        return label;
        //return label + ", " + (String.valueOf(accuracy));
    }

    // 返回具有最高和次高输出结果的标签和对应的准确度
    public String getTopInfo() {
        List<SortData> data1 = sort(); // 对输出结果进行排序

        SortData No1 = data1.get(0);
        SortData No2 = data1.get(1);

        StringBuffer sb = new StringBuffer();
        sb.append(labels[No1.index]);
        sb.append("(" + No1.accuracy + ")");

        sb.append("\n");
        sb.append(labels[No2.index]);
        sb.append("(" + No2.accuracy + ")");

        return sb.toString(); // 返回排序后的结果信息
    }

    // 重写toString方法，用于打印结果
    @Override
    public String toString() {
        return "TensorflowResult{" +
                "output=" + Arrays.toString(output) +
                '}';
    }

    // 对输出结果进行排序
    private List<SortData> sort() {
        List<SortData> list = new ArrayList<>();

        for (int i = 0; i < output.length; i++) {
            SortData data = new SortData();
            data.index = i;
            data.accuracy = output[i];
            list.add(data);
        }

        Collections.sort(list); // 使用准确度进行排序
        return list;
    }

    // 定义用于存储排序后的输出结果的数据结构
    class SortData implements Comparable<SortData> {
        int index;
        float accuracy;

        // 实现Comparable接口的compareTo方法，用于自定义排序
        @Override
        public int compareTo(SortData sortData) {
            if (accuracy == sortData.accuracy) {
                return 0;
            }
            if (accuracy < sortData.accuracy) {
                return 1; // 降序排序
            } else {
                return -1;
            }
        }
    }
}
