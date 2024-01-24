package cn.touchair.audiobox.common;

import androidx.annotation.NonNull;

import java.util.Objects;

public class BoxConvert {

    public static short[] asShortArray(byte[] src, int offset, int len) {
        int dstLength = len >>> 1;
        short[] dst = new short[dstLength];
        for (int i = 0; i < dstLength; i++) {
            short x = 0;
            x += (short) (src[i * 2 + offset] & 0xff);
            x += (short) ((src[i * 2 + offset + 1] & 0xff) << 8);
            dst[i] = x;
        }
        return dst;
    }

    public static byte[] asByteArray(short[] src) {
        byte[] byteValue = new byte[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            byteValue[i * 2] = (byte) (src[i] & 0xff);
            byteValue[i * 2 + 1] = (byte) ((src[i] & 0xff00) >> 8);
        }
        return byteValue;
    }

    public static short[] asShortArray(String[] src) {
        Objects.requireNonNull(src);
        short[] arr = new short[src.length];
        for (int i = 0; i < src.length; i++) {
            try {
                int sample = Integer.parseInt(src[i].trim());
                if (sample > Short.MAX_VALUE) sample = Short.MAX_VALUE;
                if (sample < Short.MIN_VALUE) sample = Short.MIN_VALUE;
                arr[i] = (short) sample;
            } catch (NumberFormatException exception) {
                exception.printStackTrace(System.err);
            }
        }
        return arr;
    }

    public static short[] toShortArray(@NonNull String str, String split) {
        String[] src = str.split(split);
        short[] dest = new short[src.length];
        for (int i = 0; i < dest.length; ++i) {
            try {
                dest[i] = Short.parseShort(src[i], 10);
            } catch (Exception ignored) {
                dest[i] = 0;
            }
        }
        return dest;
    }
}
