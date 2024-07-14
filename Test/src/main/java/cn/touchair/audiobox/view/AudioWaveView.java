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
    private static final int LINE_WIDTH = 2;
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
    private final int LIMIT = 256;
    private final Paint mPaint = new Paint();
    private final ArrayDeque<Float> mDataDeque0 = new ArrayDeque<>();
    private final ArrayDeque<Float> mDataDeque1 = new ArrayDeque<>();
    public AudioWaveView(Context context) {
        super(context, null);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < LIMIT; ++i) {
            mDataDeque0.add(0F);
            mDataDeque1.add(0F);
        }
        mPaint.setStrokeWidth(LINE_WIDTH);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mW = getMeasuredWidth();
        mH = getMeasuredHeight();
    }

    public void updateAudioData(@Nullable AudioFrame<short[]> frame) {
        if (frame != null) {
            float amp0 = calculateAmp(frame.stream0);
            float amp1 = calculateAmp(frame.stream1);
            push(amp0, amp1);
            mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
        }
    }

    public synchronized void clear() {
        mDataDeque0.clear();
        mDataDeque1.clear();
        for (int i = 0; i < LIMIT; ++i) {
            mDataDeque0.add(0F);
            mDataDeque1.add(0F);
        }
        mHandler.sendEmptyMessage(MSG_DRAW_SNAPSHOT);
    }

    private synchronized void push(float amp0, float amp1) {
        mDataDeque0.add(amp0);
        mDataDeque0.removeFirst();
        mDataDeque1.add(amp1);
        mDataDeque1.removeFirst();
    }

    private float calculateAmp(@Nullable short[] samples) {
        if (samples == null) return 0;
        short max = 0;
        for (short sample : samples) {
            if (Math.abs(max) < Math.abs(sample)) max = sample;
        }
        return (float) max / Short.MAX_VALUE;
    }

    private static final int MARGIN_PX = 4;
    private final RectF RECTF = new RectF();
    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawArea(canvas);
        /*For stream#0*/
        mPaint.setColor(Color.BLUE);
        float axis = (mH / 2 - MARGIN_PX) / 2;
        int length = mDataDeque0.size();
        float xStep = mW / length;

        int index = 0;
        for (float it: mDataDeque0) {
            if (it > 1F) throw new RuntimeException("" + it);
            if (it == 0F) {
                canvas.drawLine(index * xStep, axis, (index + 1) * xStep, axis, mPaint);
            } else {
                RECTF.left = index * xStep;
                RECTF.top = axis - it * (mH - 2 * MARGIN_PX) / 4;
                RECTF.right = RECTF.left + xStep;
                RECTF.bottom = axis;
                if (Math.abs(RECTF.top - RECTF.bottom) < LINE_WIDTH) {
                    RECTF.top = axis - (float) LINE_WIDTH / 2;
                    RECTF.bottom = axis + (float) LINE_WIDTH / 2;
                }
                canvas.drawRect(RECTF, mPaint);
            }
            index++;
        }

        /*For stream#1*/
        mPaint.setColor(Color.RED);
        axis = (3 * mH + 2 * MARGIN_PX) / 4;
        length = mDataDeque1.size();
        xStep = mW / length;

        index = 0;
        for (float it: mDataDeque1) {
            if (it > 1F) throw new RuntimeException("" + it);
            if (it == 0) {
                canvas.drawLine(index * xStep, axis, (index + 1) * xStep, axis, mPaint);
            } else {
                RECTF.left = index * xStep;
                RECTF.top = axis - it * (mH - 2 * MARGIN_PX) / 4;
                RECTF.right = RECTF.left + xStep;
                RECTF.bottom = axis;
                if (Math.abs(RECTF.top - RECTF.bottom) < LINE_WIDTH) {
                    RECTF.top = axis - (float) LINE_WIDTH / 2;
                    RECTF.bottom = axis + (float) LINE_WIDTH / 2;
                }
                canvas.drawRect(RECTF, mPaint);
            }
            index++;
        }
    }


    private void drawArea(Canvas canvas) {
        int color = mPaint.getColor();
        mPaint.setColor(/*getContext().getColor(R.color.primary) & 0x00FFFFFF | 0x33000000*/ Color.argb(127, 0, 0, 0));
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
