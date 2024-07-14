package cn.touchair.audiobox.common;

import android.graphics.Color;
import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;

import cn.touchair.audiobox.R;
import cn.touchair.audiobox.common.AudioFrame;
import cn.touchair.audiobox.view.AudioSpectrumView;
import cn.touchair.audiobox.view.AudioWaveView;

public final class ViewBindAdapters {
    private ViewBindAdapters() {}

    @BindingAdapter("onUpdateData")
    public static void  updateData(AudioWaveView view, AudioFrame<short[]> data) {
        view.updateAudioData(data);
    }

    @BindingAdapter("onUpdateData")
    public static void  updateData(AudioSpectrumView view, AudioFrame<short[]> data) {
        view.updateAudioData(data);
    }

    @BindingAdapter("recording")
    public static void isRecording(MaterialButton button, boolean recording) {
        button.setBackgroundColor(recording ? Color.RED : button.getContext().getColor(R.color.primary));
        button.setIconResource(recording ? R.drawable.ic_mic_off : R.drawable.ic_mic);
    }

    @BindingAdapter("recording")
    public static void isRecording(AudioWaveView view, boolean recording) {
        if (!recording) view.clear();
    }

    @BindingAdapter("recording")
    public static void isRecording(AudioSpectrumView view, boolean recording) {
        if (!recording) view.clear();
    }

    @BindingAdapter("playing")
    public static void isPlaying(MaterialButton button, boolean playing) {
        button.setBackgroundColor(playing ? Color.RED : button.getContext().getColor(R.color.primary));
        button.setIconResource(playing ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
    }
}
