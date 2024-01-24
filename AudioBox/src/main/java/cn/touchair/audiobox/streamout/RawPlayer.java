package cn.touchair.audiobox.streamout;

import android.media.AudioFormat;
import android.media.AudioTrack;

import cn.touchair.audiobox.common.Prerequisites;
import cn.touchair.audiobox.common.RawPacket;

public class RawPlayer extends AbstractPlayer {
    private RawPacket mPacket;

    private boolean mReleased = false;
    private boolean mPrepared = false;
    private int mMinBufferSize;
    private PlaybackThread mThread;

    public void setAudioSource(RawPacket packet) {
        setAudioSource(packet, new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(DEFAULT_SAMPLE_RATE)
                .setChannelMask(DEFAULT_CHANNEL_MASK)
                .build());
    }

    public void setAudioSource(RawPacket packet, AudioFormat format) {
        Prerequisites.check(!mReleased, "Player already released!");
        this.format = format;
        mMinBufferSize = AudioTrack.getMinBufferSize(
                format.getSampleRate(),
                format.getChannelMask(),
                format.getEncoding());
        mPacket = packet;
        mPacket.fillZero(mMinBufferSize);
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
        mPacket = null;
    }

    private class PlaybackThread extends Thread {

        private final String tag = PlaybackThread.class.getSimpleName();
        private final AudioTrack track;
        private volatile boolean playing = false;
        private volatile boolean exit = false;

        private PlaybackThread() {
            track = new AudioTrack.Builder()
                    .setBufferSizeInBytes(mMinBufferSize)
                    .setAudioFormat(format)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setAudioAttributes(attributes)
                    .build();
        }

        @Override
        public void run() {
            boolean flag = true;
            while (!exit) {
                if (playing) {
                    if (flag) {
                        track.write(mPacket.header, 0, mPacket.header.length, AudioTrack.WRITE_BLOCKING);
                        flag = false;
                    }
                    track.write(mPacket.body, 0, mPacket.body.length, AudioTrack.WRITE_BLOCKING);
                    if (!loop) {
                        track.write(mPacket.tail, 0, mPacket.tail.length, AudioTrack.WRITE_BLOCKING);
                        handler.sendEmptyMessage(MSG_WHAT_PAUSE);
                    }
                }
            }
            track.write(mPacket.tail, 0, mPacket.tail.length, AudioTrack.WRITE_BLOCKING);
            track.stop();
            track.release();
        }

        public boolean isPlaying() {
            return playing;
        }

        public void play() {
            Prerequisites.check(!exit, "PlaybackThread already exited!");
            if (!playing) {
                track.play();
                playing = true;
            }
        }

        public void pause() {
            if (playing) {
                playing = false;
                track.pause();
            }
        }

        public void exit() {
            exit = true;
        }
    }
}
