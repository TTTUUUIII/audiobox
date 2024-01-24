package cn.touchair.audiobox.streamout;

import android.media.AudioTrack;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import cn.touchair.audiobox.common.Prerequisites;

public class AudioPlayer extends AbstractPlayer{

    private File mFile;
    private boolean mReleased = false;
    private boolean mPrepared = false;

    private int mOffset = 0;
    private PlaybackThread mThread;

    public void setAudioSource(@NonNull File file) {
        Objects.requireNonNull(file);
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
        mFile = file;
    }

    public void setAudioSource(@NonNull String filePath) {
        Objects.requireNonNull(filePath);
        setAudioSource(new File(filePath));
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
        mFile = null;
    }

    private class PlaybackThread extends Thread {

        private final String tag = getClass().getSimpleName();
        private final AudioTrack track;
        private volatile boolean playing = false;
        private volatile boolean exit = false;

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

        @Override
        public void run() {
            try (RandomAccessFile fd = new RandomAccessFile(mFile, "r")){
                fd.seek(mOffset);
                byte[] buffer = new byte[4096];
                while (!exit) {
                    if (playing) {
                        int readNumInBytes = fd.read(buffer);
                        if (readNumInBytes > 0) {
                            track.write(buffer, 0, readNumInBytes);
                        } else if (readNumInBytes == -1) {
                            if (loop) {
                                fd.seek(mOffset);
                            } else {
                                handler.sendEmptyMessage(MSG_WHAT_PAUSE);
                            }
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace(System.err);
            }
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
