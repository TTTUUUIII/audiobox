package cn.touchair.audiobox.util;

import android.util.Log;

import java.util.Locale;

public final class Logger {
    private static final String TAG = "AudioBox";
    private Logger() {}

    public static void debug(String msg) {
        Log.d(TAG, msg);
    }

    public static void info(String msg) {
        Log.i(TAG, msg);
    }

    public static void warn(String msg) {
        Log.w(TAG, msg);
    }

    public static void error(String msg) {
        Log.e(TAG, msg);
    }

    private static String format(int level, String msg) {
        return String.format(Locale.US, "[%s] %s", stringLevel(level), msg);
    }

    private static String stringLevel(int level) {
        switch (level) {
            case Log.INFO:
                return "INFO";
            case Log.ERROR:
                return "ERROR";
            case Log.WARN:
                return "WARN";
            default:
                return "DEBUG";
        }
    }
}
