package cn.touchair.audiobox.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AudioUtils {
    public static final int CHANNEL_LEFT = 0;
    public static final int CHANNEL_RIGHT = 1;

    public static short[] monoToStereo(short[] src) {
        short[] dst = new short[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            dst[i * 2] = src[i];
            dst[i * 2 + 1] = src[i];
        }
        return dst;
    }

    public static void mute(int channel, short[] src) {
        for (int i = 0; i < src.length; i++) {
            if (i % 2 == channel) src[i] = 0;
        }
    }

    public static void equalizer(short[] src, float a) {
        for (int i = 0; i < src.length; i++) {
            src[i] = (short) (src[i] * a);
        }
    }
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
}
