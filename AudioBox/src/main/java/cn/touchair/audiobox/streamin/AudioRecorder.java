package cn.touchair.audiobox.streamin;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import cn.touchair.audiobox.common.Prerequisites;

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
    }

    private class RecordThread extends Thread {
        private final String tag = getClass().getSimpleName();
        private final AudioRecord record;
        private boolean exit = false;
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
                case BUFFER_TYPE_SHORT:
                    buffer1 = new short[minBufferSizeInBytes / Short.BYTES];
                    break;
                case BUFFER_TYPE_BYTE:
                    buffer2 = new byte[minBufferSizeInBytes];
                    break;
                case BUFFER_TYPE_FLOAT:
                    buffer3 = new float[minBufferSizeInBytes / Float.BYTES];
                    break;
                default:
                    throw new RuntimeException("Invalid buffer type " + bufferType);
            }
        }

        @Override
        public void run() {
            super.run();
            Log.i(tag, tag + " running");
            while (!exit) {
                if (recording) {
                    if (bufferType == BUFFER_TYPE_SHORT) {
                        capture1();
                    } else if (bufferType == BUFFER_TYPE_BYTE) {
                        capture2();
                    } else if (bufferType == BUFFER_TYPE_FLOAT){
                        capture3();
                    } else {
                        Log.w(TAG, "Invalid buffer type " + bufferType);
                    }
                }
            }
            if (recording) {
                recording = false;
                record.release();
            }
            record.release();
            Log.i(tag, tag + " exit");
        }

        private void capture1() {
            int readNum = record.read(buffer1, 0, buffer1.length);
            if (readNum > 0 && listener != null) {
                final short[] dest = new short[readNum];
                System.arraycopy(buffer1, 0, dest, 0, dest.length);
                listener.onCapture((T)dest);
            }
        }

        private void capture2() {
            int readNum = record.read(buffer2, 0, buffer2.length);
            if (readNum > 0 && listener != null) {
                final byte[] dest = new byte[readNum];
                System.arraycopy(buffer2, 0, dest, 0, dest.length);
                listener.onCapture((T) dest);
            }
        }

        private void capture3() {
            int readNum = record.read(buffer3, 0, buffer3.length, AudioRecord.READ_NON_BLOCKING);
            if (readNum > 0 && listener != null) {
                final float[] dest = new float[readNum];
                System.arraycopy(buffer3, 0, dest, 0, dest.length);
                listener.onCapture((T) dest);
            }
        }

        void startRecorder() {
            Prerequisites.check(!exit, "RecordThread already exited!");
            if (recording) return;
            record.startRecording();
            recording = true;
        }

        void stopRecorder() {
            Prerequisites.check(!exit, "RecordThread already exited!");
            if (recording) {
                recording = false;
                record.stop();
            }
        }

        void exit() {
            exit = true;
        }
    }
}
