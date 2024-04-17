package cn.touchair.audiobox.common.queue;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class OverflowQueue<T> {

    private static final byte TYPE_BYTE = 1;
    private static final byte TYPE_SHORT = 1 << 1;
    private static final byte TYPE_FLOAT = 1 << 2;
    private static final byte TYPE_INTEGER = 1 << 3;

    private int mPointer = 0;
    private byte mQueueType;
    private byte[] mQueue0;
    private short[] mQueue1;
    private float[] mQueue2;
    private int[] mQueue3;
    private final OverflowCallback<T> mCallback;
    public OverflowQueue(@IntRange(from = 1) int size, byte queueType, @NonNull OverflowCallback<T> callback){
        doInitialize(size, queueType, callback);
        mCallback = callback;
    }

    public void enqueue(T src, int offset, int size) {
        switch (mQueueType) {
            case TYPE_BYTE:
                enqueue0((byte[]) src, offset, size);
                break;
            case TYPE_SHORT:
                enqueue1((short[]) src, offset, size);
                break;
            case TYPE_FLOAT:
                enqueue2((float[]) src, offset, size);
                break;
            case TYPE_INTEGER:
                enqueue3((int[]) src, offset, size);
                break;
            default:
        }
    }

    private void enqueue0(byte[] src, int offset, int size) {
        for (int i = offset; i < size; i++) {
            if (mPointer == mQueue0.length) {
                mCallback.flow((T) mQueue0);
                overflow();
            }
            mQueue0[mPointer++] = src[i];
        }
    }

    private void enqueue1(short[] src, int offset, int size) {
        for (int i = offset; i < size; i++) {
            if (mPointer == mQueue1.length) {
                mCallback.flow((T) mQueue1);
                overflow();
            }
            mQueue1[mPointer++] = src[i];
        }
    }
    private void enqueue2(float[] src, int offset, int size) {
        for (int i = offset; i < size; i++) {
            if (mPointer == mQueue2.length) {
                mCallback.flow((T) mQueue2);
                overflow();
            }
            mQueue2[mPointer++] = src[i];
        }
    }
    private void enqueue3(int[] src, int offset, int size) {
        for (int i = offset; i < size; i++) {
            if (mPointer == mQueue3.length) {
                mCallback.flow((T) mQueue3);
                overflow();
            }
            mQueue3[mPointer++] = src[i];
        }
    }


    private void overflow() {
        mPointer = 0;
    }

    private void doInitialize(int size, byte queueType, OverflowCallback<T> callback) {
        switch (queueType) {
            case TYPE_BYTE:
                mQueue0 = new byte[size];
                break;
            case TYPE_SHORT:
                mQueue1 = new short[size];
                break;
            case TYPE_FLOAT:
                mQueue2 = new float[size];
                break;
            case TYPE_INTEGER:
                mQueue3 = new int[size];
                break;
            default:
                throw new RuntimeException("Unsupported type " + queueType + " queue.");
        }
        mQueueType = queueType;
    }
}