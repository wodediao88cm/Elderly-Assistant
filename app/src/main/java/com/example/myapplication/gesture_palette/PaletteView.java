package com.example.myapplication.gesture_palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

// PaletteView 类继承自 Android 中的 View 类，用于实现绘制和擦除功能，并生成MNIST模型适用的手写数字图像
public class PaletteView extends View {

    public static final int MNIST_SIZE = 28; // 定义手写数字图像的尺寸为28x28像素
    private Paint mPaint; // 用于绘制的画笔
    private Path mPath; // 绘制的路径

    // 上一次绘制的坐标
    private float mLastX;
    private float mLastY;

    // 用于临时存储绘制操作的位图和画布
    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;

    // 用于存储适用于MNIST模型的手写数字图像的位图和画布
    private Bitmap mBufferBitmapMnist;
    private Canvas mBufferCanvasMnist;

    // 绘制区域的矩形范围
    private RectF mContentRectF = new RectF();

    private static final int MAX_CACHE_STEP = 20;

    // 存储绘制操作和被撤销的操作的列表
    private List<DrawingInfo> mDrawingList;
    private List<DrawingInfo> mRemovedList;

    private Xfermode mClearMode; // 用于擦除的 Xfermode
    private float mDrawSize; // 绘制时的笔触大小
    private float mEraserSize; // 擦除时的笔触大小

    private boolean mCanEraser; // 标记是否能执行擦除操作

    private Callback mCallback; // 回调接口，用于在撤销/重做状态发生变化时通知

    // 绘制模式，可以是DRAW（绘制）或者ERASER（擦除）
    public enum Mode {
        DRAW,
        ERASER
    }

    private Mode mMode = Mode.DRAW; // 默认绘制模式为DRAW

    // 构造函数
    public PaletteView(Context context) {
        this(context, null);
    }

    // 构造函数
    public PaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        init();
    }

    // 回调接口，用于在撤销/重做状态发生变化时通知
    public interface Callback {
        void onUndoRedoStatusChanged();
    }

    // 设置回调接口
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    // 初始化画笔
    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFilterBitmap(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawSize = 40; // 默认绘制笔触大小
        mEraserSize = mDrawSize * 10; // 默认擦除笔触大小为绘制笔触大小的10倍
        mPaint.setStrokeWidth(mDrawSize);
        mPaint.setColor(0XFF00FF00); // 默认绘制颜色为绿色
        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 初始化擦除模式
    }

    // 初始化绘制和擦除的位图和画布
    private void initBuffer() {
        mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
        mBufferBitmapMnist = Bitmap.createBitmap(MNIST_SIZE, MNIST_SIZE, Bitmap.Config.ALPHA_8);
        mBufferCanvasMnist = new Canvas(mBufferBitmapMnist);
    }

    // 绘制操作的抽象基类
    private abstract static class DrawingInfo {
        Paint paint;
        abstract void draw(Canvas canvas);
    }

    // 绘制路径的具体操作类
    private static class PathDrawingInfo extends DrawingInfo {
        Path path;
        @Override
        void draw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }
    }

    // 获取绘制模式（绘制或擦除）
    public Mode getMode() {
        return mMode;
    }

    // 设置绘制模式
    public void setMode(Mode mode) {
        if (mode != mMode) {
            mMode = mode;
            if (mMode == Mode.DRAW) {
                mPaint.setXfermode(null);
                mPaint.setStrokeWidth(mDrawSize);
            } else {
                mPaint.setXfermode(mClearMode);
                mPaint.setStrokeWidth(mEraserSize);
            }
        }
    }

    // 设置擦除的笔触大小
    public void setEraserSize(float size) {
        mEraserSize = size;
    }

    // 设置绘制时的笔触大小
    public void setPenRawSize(float size) {
        mDrawSize = size;
    }

    // 设置绘制笔的颜色
    public void setPenColor(int color) {
        mPaint.setColor(color);
    }

    // 设置绘制笔的透明度
    public void setPenAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    // 重新绘制操作
    private void reDraw() {
        if (mDrawingList != null) {
            mBufferBitmap.eraseColor(Color.TRANSPARENT);
            for (DrawingInfo drawingInfo : mDrawingList) {
                drawingInfo.draw(mBufferCanvas);
            }
            invalidate();
        }
    }

    // 判断是否可以重做操作
    public boolean canRedo() {
        return mRemovedList != null && mRemovedList.size() > 0;
    }

    // 判断是否可以撤销操作
    public boolean canUndo() {
        return mDrawingList != null && mDrawingList.size() > 0;
    }

    // 重做操作
    public void redo() {
        int size = mRemovedList == null ? 0 : mRemovedList.size();
        if (size > 0) {
            DrawingInfo info = mRemovedList.remove(size - 1);
            mDrawingList.add(info);
            mCanEraser = true;
            reDraw();
            if (mCallback != null) {
                mCallback.onUndoRedoStatusChanged();
            }
        }
    }

    // 撤销操作
    public void undo() {
        int size = mDrawingList == null ? 0 : mDrawingList.size();
        if (size > 0) {
            DrawingInfo info = mDrawingList.remove(size - 1);
            if (mRemovedList == null) {
                mRemovedList = new ArrayList<>(MAX_CACHE_STEP);
            }
            if (size == 1) {
                mCanEraser = false;
            }
            mRemovedList.add(info);
            reDraw();
            if (mCallback != null) {
                mCallback.onUndoRedoStatusChanged();
            }
        }
    }

    // 清空绘制操作
    public void clear() {
        if (mBufferBitmap != null) {
            if (mDrawingList != null) {
                mDrawingList.clear();
            }
            if (mRemovedList != null) {
                mRemovedList.clear();
            }
            mCanEraser = false;
            mBufferBitmap.eraseColor(Color.TRANSPARENT);
            invalidate();
            if (mCallback != null) {
                mCallback.onUndoRedoStatusChanged();
            }
        }
    }

    // 获取适用于MNIST模型的手写数字位图
    public Bitmap buildBitmap() {
        mBufferBitmapMnist.eraseColor(Color.TRANSPARENT);

        // 修正绘制区域的矩形范围
        RectF fixedRect = fixWh(mContentRectF);
        Rect rect = new Rect();
        rect.left = (int) fixedRect.left;
        rect.top = (int) fixedRect.top;
        rect.right = (int) fixedRect.right;
        rect.bottom = (int) fixedRect.bottom;

        RectF dst = new RectF();
        dst.right = MNIST_SIZE;
        dst.bottom = MNIST_SIZE;

        mBufferCanvasMnist.drawBitmap(mBufferBitmap, rect, dst, mPaint);
        return Bitmap.createBitmap(mBufferBitmapMnist);
    }

    // 保存绘制的路径信息
    private void saveDrawingPath() {
        if (mDrawingList == null) {
            mDrawingList = new ArrayList<>(MAX_CACHE_STEP);
        } else if (mDrawingList.size() == MAX_CACHE_STEP) {
            mDrawingList.remove(0);
        }
        Path cachePath = new Path(mPath);
        Paint cachePaint = new Paint(mPaint);
        PathDrawingInfo info = new PathDrawingInfo();
        info.path = cachePath;
        info.paint = cachePaint;
        mDrawingList.add(info);
        mCanEraser = true;
        if (mCallback != null) {
            mCallback.onUndoRedoStatusChanged();
        }
    }

    // 更新绘制区域的矩形范围，获得绘制内容的整体矩形范围，以便能够更精确地控制绘制和处理绘制内容的位置和大小。
    private void updateContentRect() {
        mContentRectF.set(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);
        if (mDrawingList != null && mDrawingList.size() > 0) {
            for (DrawingInfo info : mDrawingList) {
                PathDrawingInfo pathInfo = (PathDrawingInfo) info;
                RectF currentRectF = new RectF();
                pathInfo.path.computeBounds(currentRectF, false);

                mContentRectF.left = currentRectF.left < mContentRectF.left ? currentRectF.left : mContentRectF.left;
                mContentRectF.top = currentRectF.top < mContentRectF.top ? currentRectF.top : mContentRectF.top;
                mContentRectF.right = currentRectF.right > mContentRectF.right ? currentRectF.right : mContentRectF.right;
                mContentRectF.bottom = currentRectF.bottom > mContentRectF.bottom ? currentRectF.bottom : mContentRectF.bottom;
            }
        }
    }

    // 绘制视图
    @Override
    protected void onDraw(Canvas canvas) {
        if (mBufferBitmap != null) {
            canvas.drawBitmap(mBufferBitmap, 0, 0, null);
        }
    }

    // 触摸事件处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x = event.getX();
        final float y = event.getY();
        switch (action) {
            //记录下当前的位置坐标，并创建一个新的绘制路径 mPath。
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                if (mPath == null) {
                    mPath = new Path();
                }
                mPath.moveTo(x, y);
                break;
                //接着当前的位置，绘制二次曲线路径
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(mLastX, mLastY, (x + mLastX) / 2, (y + mLastY) / 2);
                if (mBufferBitmap == null) {
                    initBuffer();
                }
                if (mMode == Mode.ERASER && !mCanEraser) {
                    break;
                }
                mBufferCanvas.drawPath(mPath, mPaint);
                invalidate();
                mLastX = x;
                mLastY = y;
                break;
                //如果当前是绘制模式或者可以执行擦除操作，则保存当前的绘制路径，更新绘制内容的矩形范围，并重置路径 mPath
            case MotionEvent.ACTION_UP:
                if (mMode == Mode.DRAW || mCanEraser) {
                    saveDrawingPath();
                    updateContentRect();
                }

                mPath.reset();
                postInvalidate();
                break;
        }
        return true;
    }


    // 根据指定宽高，计算转换后的目标大小百分比
    public static float[] calc(float width, float height, float targetSize) {
        float[] results = new float[]{0, 0};
        float max = Math.max(width, height);
        float times = getTimes(targetSize, max);

        results[0] = width / times;
        results[1] = height / times;
        return results;
    }

    // 获取转换后的目标大小百分比的倍数
    private static float getTimes(float targetSize, float max) {
        return Math.round((max / targetSize) * 10) / 10f;
    }

    // 修正矩形的宽高
    private static RectF fixWh(RectF rect) {
        RectF result = new RectF(rect);

        float w = rect.width();
        float h = rect.height();

        float more = Math.abs(w - h);

        if (w > h) {
            result.bottom += more / 2;
            result.top -= more / 2;
        } else {
            result.right += more / 2;
            result.left -= more / 2;
        }

        return result;
    }
}