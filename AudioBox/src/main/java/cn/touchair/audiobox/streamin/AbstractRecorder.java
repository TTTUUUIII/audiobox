package cn.touchair.audiobox.streamin;

import android.media.AudioFormat;

import androidx.annotation.NonNull;

import cn.touchair.audiobox.common.PrettyTextUtils;
import cn.touchair.audiobox.interfaces.AudioComponents;
import cn.touchair.audiobox.interfaces.CaptureListener;

public abstract class AbstractRecorder<T> extends AudioComponents {
    protected final String TAG = getClass().getSimpleName();
    protected static final int DEFAULT_CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;
    protected static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int DEFAULT_SAMPLE_RATE = 48000;
    public static final byte BUFFER_TYPE_SHORT = 1;
    public static final byte BUFFER_TYPE_BYTE = 1 << 1;
    public static final byte BUFFER_TYPE_FLOAT = 1 << 2;
    protected AudioFormat format;
    protected byte bufferType;
    protected CaptureListener<T> listener;
    public AbstractRecorder(@NonNull AudioFormat format) {
        this.format = format;
        showParameters();
    }

    public void setCaptureListener(@NonNull CaptureListener<T> listener, byte captureType) {
        bufferType = checkBufferType(captureType);
        this.listener = listener;
    }

    private byte checkBufferType(byte type) {
        switch (type) {
            case BUFFER_TYPE_BYTE:
            case BUFFER_TYPE_FLOAT:
            case BUFFER_TYPE_SHORT:
                return type;
            default:
                throw new RuntimeException("Type " + type + " not support!");
        }
    }

    public abstract void start();
    public abstract void pause();

    public abstract void reset();
    public abstract void release();

    public abstract boolean isRecording();

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
