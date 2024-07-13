package cn.touchair.audiobox.util;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import cn.touchair.audiobox.common.AudioFrame;
import cn.touchair.audiobox.common.WaveHeader;

public abstract class AudioUtils {
    public static final int CHANNEL_LEFT = 0;
    public static final int CHANNEL_RIGHT = 1;

    public static short[] stereo(short[] mono) {
        short[] dst = new short[mono.length * 2];
        for (int i = 0; i < mono.length; i++) {
            dst[i * 2] = mono[i];
            dst[i * 2 + 1] = mono[i];
        }
        return dst;
    }

    public static void mute(int channel, short[] stereo) {
        for (int i = 0; i < stereo.length; i++) {
            if (i % 2 == channel) stereo[i] = 0;
        }
    }

    public static AudioFrame<short[]> separate(short[] stereo) {
        int length = stereo.length / 2;
        AudioFrame<short[]> dest = new AudioFrame<short[]>(new short[length], new short[length]);
        for (int i = 0, position = 0; i < length; i++, position += 2) {
            dest.stream0[i] = stereo[position];
            dest.stream1[i] = stereo[position + 1];
        }
        return dest;
    }

    public static void equalizer(short[] src, float a) {
        for (int i = 0; i < src.length; i++) {
            src[i] = (short) (src[i] * a);
        }
    }

    /**
     * Generate wav file header
     * @param fileLen
     * @param sampleRate
     * @param channels
     * @param bps
     * @return
     */
    public static byte[] generateWavHeader(long fileLen, long sampleRate, int channels, int bps) {
        WaveHeader waveHeader = new WaveHeader(WaveHeader.FORMAT_PCM, (short) channels, (short) sampleRate, (short) bps, (int) fileLen);
        byte[] res = new byte[0];
        try (ByteArrayOutputStream fd = new ByteArrayOutputStream()){
            waveHeader.write(fd);
            fd.flush();
            res = fd.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }


    /*For data convert*/
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
