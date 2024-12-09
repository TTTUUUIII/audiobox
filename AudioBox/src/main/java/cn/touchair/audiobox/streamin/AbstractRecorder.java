package cn.touchair.audiobox.streamin;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import cn.touchair.audiobox.annotations.BufferType;
import cn.touchair.audiobox.util.Logger;
import cn.touchair.audiobox.util.PrettyTextUtils;
import cn.touchair.audiobox.common.AudioComponents;

public abstract class AbstractRecorder<T> extends AudioComponents {
    protected static final int MSG_NEW_AUDIO_BUFFER = 1;
    protected static final int DEFAULT_CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;
    protected static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int DEFAULT_SAMPLE_RATE = 48000;
    protected final AudioFormat format;
    protected @BufferType int bufferType;
    private Callback<T> callback;
    protected RecorderEventListener listener;
    protected Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_NEW_AUDIO_BUFFER) {
                if (callback != null) {
                    try {
                        callback.onAudioBuffer((T) msg.obj);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    };
    public AbstractRecorder(@NonNull AudioFormat format) {
        this.format = format;
        showParameters();
    }

    public void setCaptureListener(@NonNull Callback<T> listener, @BufferType int captureType) {
        bufferType = checkBufferType(captureType);
        this.callback = listener;
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
    public abstract void setPreferredDevice(AudioDeviceInfo device);
    public abstract void reset();
    public abstract void release();

    protected void onNewAudioBuffer(T data) {
        Message message = new Message();
        message.what = MSG_NEW_AUDIO_BUFFER;
        message.obj = data;
        handler.sendMessage(message);
    }

    public abstract boolean isRecording();

    public void registerStateListener(RecorderEventListener listener) {
        this.listener = listener;
    }

    private void showParameters() {
        Object[][] rows = new Object[][] {
                {"sampleRate", format.getSampleRate()},
                {"channels", format.getChannelCount()},
                {"encoding", encodingToString(format.getEncoding())},
        };
        String metadata = PrettyTextUtils.table("RECORDER INFO", rows);
        Logger.info(metadata);
    }

    public interface Callback<T> {
        void onAudioBuffer(T data);
    }

    public interface RecorderEventListener {
        void onStart();
        void onPause();
    }
}
