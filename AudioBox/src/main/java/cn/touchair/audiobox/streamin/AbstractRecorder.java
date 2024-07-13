package cn.touchair.audiobox.streamin;

import android.media.AudioFormat;

import androidx.annotation.NonNull;

import cn.touchair.audiobox.annotations.BufferType;
import cn.touchair.audiobox.util.PrettyTextUtils;
import cn.touchair.audiobox.interfaces.AudioComponents;

public abstract class AbstractRecorder<T> extends AudioComponents {
    protected final String TAG = getClass().getSimpleName();
    protected static final int DEFAULT_CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;
    protected static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int DEFAULT_SAMPLE_RATE = 48000;
    protected AudioFormat format;
    protected @BufferType int bufferType;
    protected Callback<T> listener;
    public AbstractRecorder(@NonNull AudioFormat format) {
        this.format = format;
        showParameters();
    }

    public void setCaptureListener(@NonNull Callback<T> listener, @BufferType int captureType) {
        bufferType = checkBufferType(captureType);
        this.listener = listener;
    }

    public void setCaptureListener(@NonNull Callback<T> listener) {
        setCaptureListener(listener, BufferType.SHORT);
    }

    private int checkBufferType(@BufferType int type) {
        switch (type) {
            case BufferType.BYTE:
            case BufferType.FLOAT:
            case BufferType.SHORT:
            case BufferType.INTEGER:
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

    public interface Callback<T> {
        void onAudioBuffer(T data);
    }
}
