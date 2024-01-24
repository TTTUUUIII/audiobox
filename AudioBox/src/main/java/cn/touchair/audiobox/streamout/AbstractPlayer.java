package cn.touchair.audiobox.streamout;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public abstract class AbstractPlayer {
    protected final String TAG = getClass().getSimpleName();

    protected static final int MSG_WHAT_PAUSE = 1;
    protected static final int MSG_WHAT_PLAY = 1 << 1;
    protected static final int DEFAULT_SAMPLE_RATE = 48000;
    protected static final int DEFAULT_USAGE = AudioAttributes.USAGE_MEDIA;
    protected static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int DEFAULT_CHANNEL_MASK = AudioFormat.CHANNEL_OUT_STEREO;
    protected AudioAttributes attributes;
    protected AudioFormat format;
    protected boolean loop = false;

    protected final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_WHAT_PAUSE:
                    pause();
                    break;
                case MSG_WHAT_PLAY:
                    play();
                    break;
                default:
            }
        }
    };

    public AbstractPlayer() {
        this(new AudioAttributes.Builder()
                .setUsage(DEFAULT_USAGE)
                .build(),
                new AudioFormat.Builder()
                        .setEncoding(DEFAULT_ENCODING)
                        .setSampleRate(DEFAULT_SAMPLE_RATE)
                        .setChannelMask(DEFAULT_CHANNEL_MASK)
                        .build());
    }

    public AbstractPlayer(AudioAttributes attributes, AudioFormat format) {
        this.attributes = attributes;
        this.format = format;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            release();
        } catch (Exception ignored) {}
    }

    public abstract void play();
    public abstract void pause();

    public abstract void reset();
    public abstract void release();
}
