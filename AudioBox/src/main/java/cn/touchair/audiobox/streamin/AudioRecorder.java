package cn.touchair.audiobox.streamin;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import cn.touchair.audiobox.annotations.BufferType;
import cn.touchair.audiobox.common.LoopThread;
import cn.touchair.audiobox.util.Prerequisites;

public class AudioRecorder<T> extends AbstractRecorder<T> {

    private boolean mPrepared = false;
    private boolean mReleased = false;
    private RecordThread mThread;

    @RequiresPermission("android.permission.RECORD_AUDIO")
    public AudioRecorder() {
        this(new AudioFormat.Builder()
                .setChannelMask(DEFAULT_CHANNEL_MASK)
                .setEncoding(DEFAULT_ENCODING)
                .setSampleRate(DEFAULT_SAMPLE_RATE)
                .build());
    }

    @RequiresPermission("android.permission.RECORD_AUDIO")
    public AudioRecorder(@NonNull AudioFormat format) {
        super(format);
    }

    @Override
    public void start() {
        Prerequisites.check(!mReleased, "Recorder already released!");
        if (!mPrepared) {
            mThread = new RecordThread();
            mThread.start();
            mPrepared = true;
        }
        if (mThread.recording) return;
        mThread.startRecorder();
    }

    @Override
    public void pause() {
        Prerequisites.check(!mReleased, "Recorder already released!");
        if (mPrepared) {
            mThread.stopRecorder();
        }
    }

    @Override
    public void reset() {
        Prerequisites.check(!mReleased, "Recorder already released!");
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
    }

    @Override
    public boolean isRecording() {
        return mPrepared && mThread.recording;
    }

    private class RecordThread extends LoopThread {
        private final AudioRecord record;
        private boolean recording = false;
        private short[] buffer1;
        private byte[] buffer2;

        private float[] buffer3;
        @SuppressLint("MissingPermission")
        private RecordThread() {
            int minBufferSizeInBytes = AudioRecord.getMinBufferSize(format.getSampleRate(), format.getChannelMask(), format.getEncoding());
            record = new AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(minBufferSizeInBytes)
                    .build();
            switch (bufferType) {
                case BufferType.SHORT:
                case BufferType.INTEGER:
                    buffer1 = new short[minBufferSizeInBytes / Short.BYTES];
                    break;
                case BufferType.BYTE:
                    buffer2 = new byte[minBufferSizeInBytes];
                    break;
                case BufferType.FLOAT:
                    buffer3 = new float[minBufferSizeInBytes / Float.BYTES];
                    break;
                default:
                    throw new RuntimeException("Invalid buffer type " + bufferType);
            }
        }

        private void capture1() {
            int readNum = record.read(buffer1, 0, buffer1.length);
            if (readNum > 0) {
                final short[] dest = new short[readNum];
                System.arraycopy(buffer1, 0, dest, 0, dest.length);
                onNewAudioBuffer((T) dest);
            }
        }

        private void capture2() {
            int readNum = record.read(buffer2, 0, buffer2.length);
            if (readNum > 0) {
                final byte[] dest = new byte[readNum];
                System.arraycopy(buffer2, 0, dest, 0, dest.length);
                onNewAudioBuffer((T) dest);
            }
        }

        private void capture3() {
            int readNum = record.read(buffer3, 0, buffer3.length, AudioRecord.READ_NON_BLOCKING);
            if (readNum > 0) {
                final float[] dest = new float[readNum];
                System.arraycopy(buffer3, 0, dest, 0, dest.length);
                onNewAudioBuffer((T) dest);
            }
        }

        void startRecorder() {
            Prerequisites.check(isActive(), "RecordThread already exited!");
            if (recording) return;
            record.startRecording();
            recording = true;
            if (listener != null) {
                handler.post(listener::onStart);
            }
        }

        void stopRecorder() {
            Prerequisites.check(isActive(), "RecordThread already exited!");
            if (recording) {
                recording = false;
                record.stop();
                if (listener != null) {
                    handler.postAtTime(listener::onPause, SystemClock.uptimeMillis() + 100);
                }
            }
        }

        @Override
        public boolean onLoop() {
            if (recording) {
                if (bufferType == BufferType.SHORT || bufferType == BufferType.INTEGER) {
                    capture1();
                } else if (bufferType == BufferType.BYTE) {
                    capture2();
                } else if (bufferType == BufferType.FLOAT){
                    capture3();
                } else {
                    throw new RuntimeException("Invalid buffer type " + bufferType);
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onExitLoop() {
            super.onExitLoop();
            record.release();
            recording = false;
        }
    }
}
