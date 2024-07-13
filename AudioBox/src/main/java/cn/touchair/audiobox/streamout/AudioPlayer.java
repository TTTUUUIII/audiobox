package cn.touchair.audiobox.streamout;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import cn.touchair.audiobox.common.LoopThread;
import cn.touchair.audiobox.util.Prerequisites;

public class AudioPlayer extends AbstractPlayer<File>{
    private boolean mReleased = false;
    private boolean mPrepared = false;

    private int mOffset = 0;
    private PlaybackThread mThread;

    public AudioPlayer() {
        super();
    }

    public AudioPlayer(AudioAttributes attributes) {
        super(attributes);
    }

    @Override
    public void setAudioSource(@NonNull File file, @Nullable AudioFormat format) {
        super.setAudioSource(file, format);
        Prerequisites.check(file.exists(), "File " + file + " not found!");
        Prerequisites.check(file.isFile(), "File " + file + " not a file type!");
        String fileName = file.getName();
        if (fileName.endsWith("wav")) {
            mOffset = 44;
        } else if (fileName.endsWith("pcm")) {
            mOffset = 0;
        } else {
            throw new RuntimeException("This file type not support");
        }
    }

    public void setAudioSource(@NonNull String filePath) {
        Objects.requireNonNull(filePath);
        setAudioSource(new File(filePath), null);
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
        if (mThread != null) {
            mThread.exit();
            mThread = null;
        }
        mPrepared = false;
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

        private RandomAccessFile fd;

        private PlaybackThread() {
            int minBufferSize = AudioTrack.getMinBufferSize(
                    format.getSampleRate(),
                    format.getChannelMask(),
                    format.getEncoding());
            track = new AudioTrack.Builder()
                    .setBufferSizeInBytes(minBufferSize)
                    .setAudioFormat(format)
                    .setAudioAttributes(attributes)
                    .build();
        }


        public void play() {
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

        @Override
        public void onEnterLoop() {
            try {
                fd = new RandomAccessFile(source, "r");
                fd.seek(mOffset);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

        @Override
        public boolean onLoop() {
            byte[] buffer = new byte[4096];
            try {
                if (playing) {
                    int readNumInBytes = fd.read(buffer);
                    if (readNumInBytes > 0) {
                        track.write(buffer, 0, readNumInBytes);
                    } else if (readNumInBytes == -1) {
                        fd.seek(mOffset);
                        if (!loop) {
                            handler.sendEmptyMessage(MSG_WHAT_PAUSE);
                        }
                    }
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            return false;
        }

        @Override
        public void onExitLoop() {
            track.release();
        }
    }
}
