package cn.touchair.audiobox.common;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ChannelFilter {
    public static final int FILTER_CHANNEL_IN_LEFT = 0;
    public static final int FILTER_CHANNEL_IN_RIGHT = 1;

    /**
     * 过滤模式
     * FILL 填充模式: 将原始数据的指定通道数据用0填充
     * CLIP 裁剪模式: 创建新的数组，将需要保留的声道数据复制并返回
     */
    public static final int FILTER_MODEL_FILL = 0;
    public static final int FILTER_MODEL_CLIP = 1;
    private final int filterChannelId;
    private final int filterModel;

    public ChannelFilter(@FilterChannel int channelFilter, @FilterMode int filterModel) {
        if (channelFilter != FILTER_CHANNEL_IN_LEFT && channelFilter != FILTER_CHANNEL_IN_RIGHT) channelFilter = FILTER_CHANNEL_IN_LEFT;
        if (filterModel != FILTER_MODEL_FILL && filterModel != FILTER_MODEL_CLIP) filterModel = FILTER_MODEL_FILL;
        this.filterChannelId = channelFilter;
        this.filterModel = filterModel;
    }

    public short[] filter(short[] rawData, int offset, int size) {
        if (filterModel == FILTER_MODEL_FILL) {
            fill(rawData, offset, size);
            return rawData;
        } else return clip(rawData, offset, size);
    }

    private void fill(short[] rawData, int offset, int size) {
        for (int i = offset + filterChannelId; i < rawData.length; i += 2) {
            rawData[i] = 0;
        }
    }

    private short[] clip(short[] rawData, int offset, int size) {
        short[] result = new short[(int)(size / 2)];
        int channelId = filterChannelId == FILTER_CHANNEL_IN_LEFT ? FILTER_CHANNEL_IN_RIGHT : FILTER_CHANNEL_IN_LEFT;
        for (int i = 0, j = offset + channelId; i < result.length; i++, j += 2) {
            result[i] = rawData[j];
        }
        return result;
    }

    @IntDef({
            FILTER_CHANNEL_IN_LEFT,
            FILTER_CHANNEL_IN_RIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FilterChannel{}

    @IntDef({
            FILTER_MODEL_CLIP,
            FILTER_MODEL_FILL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FilterMode{}
}
