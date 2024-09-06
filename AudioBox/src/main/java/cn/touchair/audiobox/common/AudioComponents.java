package cn.touchair.audiobox.common;

import android.media.AudioAttributes;
import android.media.AudioFormat;

public abstract class AudioComponents {
    protected String usgaeToString(int usage) {
        switch (usage) {
            case AudioAttributes.USAGE_MEDIA:
            case AudioAttributes.USAGE_GAME:
                return "MEDIA";
            case AudioAttributes.USAGE_ALARM:
                return "ALARM";
            case AudioAttributes.USAGE_NOTIFICATION:
                return "NOTIFICATION";
            default:
                return "UNKNOWN";
        }
    }

    protected String encodingToString(int encoding) {
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_16BIT:
                return "PCM_16BIT";
            case AudioFormat.ENCODING_PCM_32BIT:
                return "PCM_32BIT";
            case AudioFormat.ENCODING_PCM_FLOAT:
                return "PCM_FLOAT";
            default:
                return "UNKNOWN";
        }
    }
}
