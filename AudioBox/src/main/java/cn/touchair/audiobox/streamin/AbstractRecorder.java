package cn.touchair.audiobox.streamin;

import android.media.AudioFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

    public void setCaptureListener(@NonNull CaptureListener<T> listener) {
        checkAndSetBufferType(listener);
        this.listener = listener;
    }

    private void checkAndSetBufferType(@NonNull CaptureListener<T> listener) {
        Type[] genericInterfaces = listener.getClass().getGenericInterfaces();
        if (genericInterfaces.length == 0) {
            throw new RuntimeException("Unable set buffer type.");
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericInterfaces[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        final String arrayTypeName = type.toString();
        if (arrayTypeName.equals(byte[].class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_BYTE;
        } else if (arrayTypeName.equals(short[].class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_SHORT;
        } else if (arrayTypeName.equals(float[].class.getCanonicalName())) {
            this.bufferType = BUFFER_TYPE_FLOAT;
        } else {
            throw new RuntimeException("Type " + arrayTypeName + " not support!");
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
