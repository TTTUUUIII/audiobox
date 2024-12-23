package cn.touchair.audiobox.streamout;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.touchair.audiobox.common.LoopThread;
import cn.touchair.audiobox.util.Prerequisites;
import cn.touchair.audiobox.common.AudioPacket;

public class DataPlayer extends AbstractPlayer<AudioPacket> {

    private boolean mReleased = false;
    private boolean mPrepared = false;
    private int mMinBufferSize;
    private PlaybackThread mThread;

    public DataPlayer() {
        super();
    }

    public DataPlayer(@NonNull AudioAttributes attributes) {
        super(attributes);
    }

    @Override
    public void setAudioSource(@NonNull AudioPacket packet, @Nullable AudioFormat format) {
        Prerequisites.check(!mReleased, "Player already released!");
        super.setAudioSource(packet, format);
        mMinBufferSize = AudioTrack.getMinBufferSize(
                this.format.getSampleRate(),
                this.format.getChannelMask(),
                this.format.getEncoding());
        source.fillZero(mMinBufferSize);
    }

    @Override
    public void play() {
        Prerequisites.check(!mReleased, "Player already released!");
        if (!mPrepared) {
            mThread = new PlaybackThread();
            mThread.start();
            mPrepared = true;
        }
        mThread.play();
    }

    @Override
    public void pause() {
        Prerequisites.check(!mReleased, "Player already released!");
        if (mPrepared) {
            mThread.pause();
        }
    }

    @Override
    public void reset() {
        Prerequisites.check(!mReleased, "Player already released!");
        if (mPrepared) {
            mThread.exit();
            mThread = null;
            mPrepared = false;
        }
    }

    @Override
    public void release() {
        reset();
        mReleased = true;
        source = null;
    }

    @Override
    public boolean isPlaying() {
        return mPrepared && mThread.playing;
    }

    private class PlaybackThread extends LoopThread {
        private final AudioTrack track;
        private volatile boolean playing = false;

        private PlaybackThread() {
            track = new AudioTrack.Builder()
                    .setBufferSizeInBytes(mMinBufferSize)
                    .setAudioFormat(format)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setAudioAttributes(attributes)
                    .build();
            track.setPreferredDevice(preferenceOutputDevice);
        }

        public boolean isPlaying() {
            return playing;
        }

        public void play() {
            Prerequisites.check(isActive(), "PlaybackThread already exited!");
            if (!playing) {
                track.play();
                playing = true;
                if (listener != null) {
                    handler.post(listener::onPlay);
                }
            }
        }

        public void pause() {
            if (playing) {
                playing = false;
                track.pause();
                if (listener != null) {
                    handler.postAtTime(listener::onPause, SystemClock.uptimeMillis() + 100);
                }
            }
        }

        private boolean flag = true;
        @Override
        public boolean onLoop() {
            if (playing) {
                if (flag) {
                    track.write(source.header, 0, source.header.length, AudioTrack.WRITE_BLOCKING);
                    flag = false;
                }
                track.write(source.body, 0, source.body.length, AudioTrack.WRITE_BLOCKING);
                if (!loop) {
                    track.write(source.tail, 0, source.tail.length, AudioTrack.WRITE_BLOCKING);
                    handler.sendEmptyMessage(MSG_WHAT_PAUSE);
                }
                return true;
            } else {
                flag = true;
            }
            return false;
        }

        @Override
        public void onExitLoop() {
            super.onExitLoop();
            if (playing) {
                track.write(source.tail, 0, source.tail.length, AudioTrack.WRITE_BLOCKING);
            }
            track.release();
        }
    }
}
