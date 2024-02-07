package cn.touchair.audiobox.common.queue;

public interface OverflowCallback<T> {
    void flow(T data);
}
