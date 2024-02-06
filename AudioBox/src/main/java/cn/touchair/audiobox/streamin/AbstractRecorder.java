package cn.touchair.audiobox.streamin;

import android.media.AudioFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.touchair.audiobox.common.PrettyTextUtils;
import cn.touchair.audiobox.interfaces.AudioComponents;
import cn.touchair.audiobox.interfaces.CaptureListener;

public abstract class AbstractRecorder<T> extends AudioComponents {
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
        showParameters();
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

    private void showParameters() {
        Object[][] rows = new Object[][] {
                {"sampleRate", format.getSampleRate()},
                {"channels", format.getChannelCount()},
                {"encoding", encodingToString(format.getEncoding())},
        };
        String table = PrettyTextUtils.table("RECORDER INFO", rows);
        System.out.println(table);
    }
}
