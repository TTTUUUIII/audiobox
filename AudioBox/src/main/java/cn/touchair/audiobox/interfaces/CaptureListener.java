package cn.touchair.audiobox.interfaces;

public interface CaptureListener<T> {
    void onCapture(T data);
}
