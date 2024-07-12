package cn.touchair.audiobox.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;

public class AudioWaveView extends View {

    private static final int MSG_DRAW_SNAPSHOT = 1;
    private final byte[] mLock = new byte[0];
    private float mW;
    private float mH;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DRAW_SNAPSHOT) {
                invalidate();
            }
        }
    };
    private int mLimit = 1000;
    private final int mHandleBlockSize = 100;
    private final Paint mPaint = new Paint();
    private final ArrayDeque<Float> mDataDeque = new ArrayDeque<>();
    public AudioWaveView(Context context) {
        super(context, null);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < mLimit; ++i) {
            mDataDeque.add(0F);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mW = getMeasuredWidth();
        mH = getMeasuredHeight();
    }

    public void updateData(short[] data) {
        float[] normalized = normalize(data);
        if (normalized.length < mHandleBlockSize) {
            push(normalized, 0, normalized.length);
            mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
        } else {
            int offset = 0;
            int length;
            int numberOfBlocks = (int) Math.ceil((double) normalized.length / mHandleBlockSize);
            do {
                if (offset + mHandleBlockSize < normalized.length) {
                    length = mHandleBlockSize;
                } else {
                    length = normalized.length - offset;
                }
                push(normalized, offset, length);
                offset += length;
                mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
            } while (--numberOfBlocks != 0);
        }
    }

    public void clear() {
        synchronized (mLock) {
            mDataDeque.clear();
            for (int i = 0; i < mLimit; ++i) {
                mDataDeque.add(0F);
            }
            mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
        }
    }

    private void push(float[] src, int offset, int size) {
        synchronized (mLock) {
            for (int i = offset; i < offset + size; ++i) {
                mDataDeque.add(src[i]);
                while (mDataDeque.size() > mLimit) mDataDeque.removeFirst();
            }
        }
    }

    private float[] normalize(short[] data) {
        float[] normalized = new float[data.length];
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] = (float) data[i] / Short.MAX_VALUE;
        }
        return normalized;
    }

    private final RectF mRectF = new RectF();
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        synchronized (mLock) {
            mPaint.setColor(Color.BLUE);
            float axis = mH / 2;
            int length = mDataDeque.size();
            float xStep = mW / length;

            int index = 0;
            float max = 0F;
            for (float it: mDataDeque) {
                mRectF.left = index * xStep;
                mRectF.top = axis - it * mH / 2;
                mRectF.right = mRectF.left + xStep;
                mRectF.bottom = axis;
                canvas.drawRect(mRectF, mPaint);
                index++;
                if (max < it) max = it;
            }
            if (max == 0F) {
                mPaint.setColor(Color.BLACK);
                canvas.drawLine(0, axis, mW, axis, mPaint);
            }
        }
    }
}
