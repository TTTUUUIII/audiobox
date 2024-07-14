package cn.touchair.audiobox.util;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class TimeUtils {
    private TimeUtils() {}

    public static String format(@NonNull String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        return dateFormat.format(System.currentTimeMillis());
    }

    public static String formattedDate() {
        return format("MMddHHmm");
    }
}
