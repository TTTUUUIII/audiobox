package cn.touchair.audiobox;

import android.content.Context;
import android.content.SharedPreferences;

public final class Settings {

    public static final String KEY_AUDIO_SAMPLE_RATE = "audio_sample_rate";
    public static final String KEY_AUDIO_CHANNELS = "audio_channels";

    private static SharedPreferences sp;
    public static void initialize(Context context) {
        sp = context.getSharedPreferences("sp-settings", Context.MODE_PRIVATE);
    }

    public static void put(String k, int v) {
        sp
                .edit()
                .putInt(k, v)
                .apply();
    }

    public static <T> T get(String k, T defaultValue) {
        Object v = sp.getAll()
                .get(k);
        if (v == null) return defaultValue;
        return (T) v;
    }
}
