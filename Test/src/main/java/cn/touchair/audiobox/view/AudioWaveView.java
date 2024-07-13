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

import cn.touchair.audiobox.common.AudioFrame;

public class AudioWaveView extends View {

    private static final int MSG_DRAW_SNAPSHOT = 1;
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
    private final int mLimit = 1000;
    private final Paint mPaint = new Paint();
    private final ArrayDeque<Float> mDataDeque0 = new ArrayDeque<>();
    private final ArrayDeque<Float> mDataDeque1 = new ArrayDeque<>();
    public AudioWaveView(Context context) {
        super(context, null);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < mLimit; ++i) {
            mDataDeque0.add(0F);
            mDataDeque1.add(0F);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mW = getMeasuredWidth();
        mH = getMeasuredHeight();
    }

    public void updateAudioData(AudioFrame<short[]> frame) {
        if (frame.stream0 != null) updateStream0(frame.stream0);
        if (frame.stream1 != null) updateStream1(frame.stream1);
    }

    private void updateStream0(short[] data) {
        float[] normalized = normalize(data);
        int mHandleBlockSize = 100;
        if (normalized.length < mHandleBlockSize) {
            push(normalized, 0, normalized.length, true);
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
                push(normalized, offset, length, true);
                offset += length;
                mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
            } while (--numberOfBlocks != 0);
        }
    }

    private void updateStream1(short[] data) {
        float[] normalized = normalize(data);
        int mHandleBlockSize = 100;
        if (normalized.length < mHandleBlockSize) {
            push(normalized, 0, normalized.length, false);
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
                push(normalized, offset, length, false);
                offset += length;
                mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
            } while (--numberOfBlocks != 0);
        }
    }

    public synchronized void clear() {
        mDataDeque0.clear();
        mDataDeque1.clear();
        for (int i = 0; i < mLimit; ++i) {
            mDataDeque0.add(0F);
            mDataDeque1.add(0F);
        }
        mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
    }

    private synchronized void push(float[] src, int offset, int size, boolean forStream0) {
        for (int i = offset; i < offset + size; ++i) {
            if (forStream0) {
                mDataDeque0.add(src[i]);
                while (mDataDeque0.size() > mLimit) mDataDeque0.removeFirst();
            } else  {
                mDataDeque1.add(src[i]);
                while (mDataDeque1.size() > mLimit) mDataDeque1.removeFirst();
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

    private static final int MARGIN_PX = 8;
    private final RectF RECTF = new RectF();
    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawArea(canvas);
        /*For stream#0*/
        mPaint.setColor(Color.BLUE);
        float axis = mH / 4 - (float) MARGIN_PX / 2;
        int length = mDataDeque0.size();
        float xStep = mW / length;

        int index = 0;
        float max = 0F;
        for (float it: mDataDeque0) {
            RECTF.left = index * xStep;
            RECTF.top = axis * (1 - it);
            RECTF.right = RECTF.left + xStep;
            RECTF.bottom = axis;
            canvas.drawRect(RECTF, mPaint);
            index++;
            if (max < it) max = it;
        }
        if (max == 0F) {
            mPaint.setColor(Color.BLACK);
            canvas.drawLine(0, axis, mW, axis, mPaint);
        }

        /*For stream#1*/
        mPaint.setColor(Color.RED);
        axis = mH - mH / 4 + (float) MARGIN_PX / 2;
        length = mDataDeque1.size();
        xStep = mW / length;

        index = 0;
        max = 0F;
        for (float it: mDataDeque1) {
            RECTF.left = index * xStep;
            RECTF.top = axis - it * mH / 4;
            RECTF.right = RECTF.left + xStep;
            RECTF.bottom = axis;
            canvas.drawRect(RECTF, mPaint);
            index++;
            if (max < it) max = it;
        }
        if (max == 0F) {
            mPaint.setColor(Color.BLACK);
            canvas.drawLine(0, axis, mW, axis, mPaint);
        }
    }

    private void drawArea(Canvas canvas) {
        int color = mPaint.getColor();
        mPaint.setARGB(0x33, 0x00, 0x00, 0x00);
        RECTF.top = 0;
        RECTF.left = 0;
        RECTF.right = mW;
        RECTF.bottom = mH / 2 - MARGIN_PX;
        canvas.drawRect(RECTF, mPaint);

        RECTF.top = mH / 2 + MARGIN_PX;
        RECTF.bottom = mH;
        canvas.drawRect(RECTF, mPaint);
        mPaint.setColor(color);
    }
}
