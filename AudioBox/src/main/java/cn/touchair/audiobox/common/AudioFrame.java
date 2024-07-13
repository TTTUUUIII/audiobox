package cn.touchair.audiobox.common;

import cn.touchair.audiobox.util.Prerequisites;

public class AudioFrame<T> {

    private boolean stereo = true;
    public final T stream0;
    public final T stream1;

    public AudioFrame(T stream0, T stream1) {
        Prerequisites.check(stream0 != null || stream1 != null, "Invalid stream data!");
        this.stream0 = stream0;
        this.stream1 = stream1;
        if (stream0 == null || stream1 == null) stereo = false;
    }

    public boolean isStereo() {
        return stereo;
    }
}
