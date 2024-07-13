package cn.touchair.audiobox.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import cn.touchair.audiobox.common.AudioFrame;
import cn.touchair.audiobox.fft.RealDoubleFFT;

public class AudioSpectrumView extends View {

    private static final int FFT_SIZE = 2048;
    private final RealDoubleFFT mRealFft = new RealDoubleFFT(FFT_SIZE);
    private int mPosition = 0;
    private final double[] mFft = new double[FFT_SIZE];

    private final Paint mPaint = new Paint();
    private float mH;
    private float mW;

    public AudioSpectrumView(Context context) {
        super(context, null);
    }

    public AudioSpectrumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void updateAudioData(AudioFrame<short[]> data) {
        if (data == null) return;
        final short[] samples = data.stream0 != null ? data.stream0 : data.stream1;
        enqueue(samples);
    }

    public void clear() {
        Arrays.fill(mMagnitudes, 0);
        invalidate();
    }

    private void enqueue(short[] samples) {
        for (short sample : samples) {
            mFft[mPosition++] = sample;
            if (mPosition == FFT_SIZE) {
                dequeue();
            }
        }
    }

    private final float[] mMagnitudes = new float[FFT_SIZE / 2 + 1];

    private void dequeue() {
        mRealFft.ft(mFft);
        fftToAmp();
        invalidate();
        mPosition = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mH = getMeasuredHeight();
        mW = getMeasuredWidth();
    }

    private final RectF RECTF = new RectF();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawArea(canvas);
        float xStep = mW / mMagnitudes.length;
        for (int i = 0; i < mMagnitudes.length; i++) {
            RECTF.left = i * xStep;
            RECTF.top = mH - mMagnitudes[i];
            RECTF.bottom = mH;
            RECTF.right = RECTF.left + xStep;
            canvas.drawRect(RECTF, mPaint);
        }
    }

    private void drawArea(Canvas canvas) {
        int color = mPaint.getColor();
        mPaint.setARGB(0x33, 0x00, 0x00, 0x00);
        RECTF.top = 0;
        RECTF.left = 0;
        RECTF.right = mW;
        RECTF.bottom = mH;
        canvas.drawRect(RECTF, mPaint);
        mPaint.setColor(color);
    }

    private void fftToAmp() {
        double scaler = 2.0 * 2.0 / (mFft.length * mFft.length);  // *2 since there are positive and negative frequency part
        mMagnitudes[0] = (float) (mFft[0] * mFft[0] * scaler / 4.0);
        int j = 1;
        for (int i = 1; i < mFft.length - 1; i += 2, j++) {
            mMagnitudes[j] = (float) ((Math.pow(mFft[i], 2) + Math.pow(mFft[i + 1], 2)) * scaler);
        }
        mMagnitudes[j] = (float) (Math.pow(mFft[mFft.length - 1], 2) * scaler / 4.0);
    }
}
