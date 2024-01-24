package cn.touchair.audiobox.streamin;

public interface CaptureListener<T> {
    void onCapture(T data);
}
