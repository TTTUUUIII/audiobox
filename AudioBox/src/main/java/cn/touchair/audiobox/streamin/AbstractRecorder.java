package cn.touchair.audiobox.streamin;

import android.media.AudioFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class AbstractRecorder<T> {
    protected final String TAG = getClass().getSimpleName();

    protected static final int DEFAULT_CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;
    protected static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int DEFAULT_SAMPLE_RATE = 48000;
    protected static final byte BUFFER_TYPE_SHORT = 1;
    protected static final byte BUFFER_TYPE_BYTE = 1 << 1;
    protected static final byte BUFFER_TYPE_FLOAT = 1 << 2;
    protected AudioFormat format;
    protected byte bufferType;
    protected CaptureListener<T> listener;
    public AbstractRecorder(@NonNull AudioFormat format) {
        this.format = format;
    }

    public void setCaptureListener(@Nullable CaptureListener<T> listener, @NonNull Class<T> clazz) {
        checkAndSetBufferType(clazz);
        this.listener = listener;
    }

    private void checkAndSetBufferType(@NonNull Class<T> clazz) {
        String canonicalName = clazz.getCanonicalName();
        assert canonicalName != null;
        if (canonicalName.equals(byte[].class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_BYTE;
        } else if (canonicalName.equals(short[].class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_SHORT;
        } else if (canonicalName.equals(float[].class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_FLOAT;
        } else {
            throw new RuntimeException("Type " + canonicalName + " not support!");
        }
    }

    public abstract void start();
    public abstract void pause();

    public abstract void reset();
    public abstract void release();
}
