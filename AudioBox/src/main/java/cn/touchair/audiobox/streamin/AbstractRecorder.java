package cn.touchair.audiobox.streamin;

import android.media.AudioFormat;

import androidx.annotation.Nullable;

public abstract class AbstractRecorder<T, R> {
    protected final String TAG = getClass().getSimpleName();

    protected static final int DEFAULT_CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;
    protected static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int DEFAULT_SAMPLE_RATE = 48000;
    protected static final byte BUFFER_TYPE_SHORT = 1;
    protected static final byte BUFFER_TYPE_BYTE = 1 << 1;
    protected static final byte BUFFER_TYPE_FLOAT = 1 << 2;
    protected AudioFormat format;
    protected byte bufferType;
    protected CaptureListener<R> listener;
    public AbstractRecorder(AudioFormat format, Class<T> clazz) {
        this.format = format;
        String canonicalName = clazz.getCanonicalName();
        assert canonicalName != null;
        if (canonicalName.equals(Byte.class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_BYTE;
        } else if (canonicalName.equals(Short.class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_SHORT;
        } else if (canonicalName.equals(Float.class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_FLOAT;
        } else {
            throw new RuntimeException("Type " + canonicalName + " not support!");
        }
    }

    public void setCaptureListener(@Nullable CaptureListener<R> listener) {
        this.listener = listener;
    }

    public abstract void start();
    public abstract void pause();

    public abstract void reset();
    public abstract void release();
}
