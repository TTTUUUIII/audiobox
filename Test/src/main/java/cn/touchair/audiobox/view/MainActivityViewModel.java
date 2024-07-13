package cn.touchair.audiobox.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.touchair.audiobox.App;
import cn.touchair.audiobox.R;
import cn.touchair.audiobox.Settings;
import cn.touchair.audiobox.common.AudioFrame;
import cn.touchair.audiobox.streamin.AbstractRecorder;
import cn.touchair.audiobox.streamin.AudioRecorder;
import cn.touchair.audiobox.streamout.AbstractPlayer;
import cn.touchair.audiobox.streamout.AudioPlayer;
import cn.touchair.audiobox.util.AudioUtils;

public class MainActivityViewModel extends ViewModel {

    public static final int REQUEST_AUDIO_RECORD_PERMISSION = 1;

    private AudioPlayer mAudioPlayer;
    @SuppressLint("MissingPermission")
    private AudioRecorder<short[]> mAudioRecorder;
    public final MutableLiveData<Integer> request = new MutableLiveData<>();
    public final MutableLiveData<AudioFrame<short[]>> audioFrame = new MutableLiveData<>();
    public final MutableLiveData<Boolean> playing = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> recording = new MutableLiveData<>(false);

    private int mSampleRate = Settings.get(Settings.KEY_AUDIO_SAMPLE_RATE, 44100);
    private int mChannels = Settings.get(Settings.KEY_AUDIO_CHANNELS, 2);
    private AudioFormat mAudioInFormat;
    private AudioFormat mAudioOutFormat;


    public void setSampleRate(String sampleRateInText) {
        try {
            mSampleRate = Integer.parseInt(sampleRateInText);
            Settings.put(Settings.KEY_AUDIO_SAMPLE_RATE, mSampleRate);
        } catch (NumberFormatException ignored) {}
    }

    public String getSampleRate() {
        return String.valueOf(mSampleRate);
    }

    public void setChannels(String channelsInText) {
        try {
            mChannels = Integer.parseInt(channelsInText);
            Settings.put(Settings.KEY_AUDIO_CHANNELS, mChannels);
        } catch (NumberFormatException ignored) {}
    }

    public String getChannels() {
        return String.valueOf(mChannels);
    }

    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.button_record_audio) {
            toggleRecord();
        } else if (viewId == R.id.button_play_audio) {
            togglePlay();
        }
    }

    public void setSystemAllowRecordAudio(boolean allow) {
        if (allow && waitingRecordAudio) {
            toggleRecord();
            waitingRecordAudio = false;
        }
    }

    private boolean waitingRecordAudio = false;
    private void toggleRecord() {
        if (mAudioPlayer != null && mAudioPlayer.isPlaying()) {
            Context applicationContext = App.requireApplicationContext();
            Toast.makeText(applicationContext, R.string.stop_player, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAudioRecorder == null) {
            Context applicationContext = App.requireApplicationContext();
            if (applicationContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                createAudioRecorder();
            } else {
                waitingRecordAudio = true;
                request.postValue(REQUEST_AUDIO_RECORD_PERMISSION);
                return;
            }
        }
        if (mAudioRecorder != null) {
            if (mAudioRecorder.isRecording()) {
                mAudioRecorder.pause();
                closeOutStream();
            } else {
                int sampleRate = mAudioInFormat.getSampleRate();
                int channels = mAudioInFormat.getChannelMask() == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
                if (sampleRate != mSampleRate || channels != mChannels) {
                    createAudioRecorder();
                }
                createOutStream();
                mAudioRecorder.start();
            }
        }
    }

    private void togglePlay() {
        final Context applicationContext = App.requireApplicationContext();
        if (mAudioRecorder != null && mAudioRecorder.isRecording()) {
            Toast.makeText(applicationContext, R.string.stop_recorder, Toast.LENGTH_SHORT).show();
            return;
        }
        File externalCacheDir = applicationContext.getExternalCacheDir();
        if (externalCacheDir != null) {
            File tempFile = new File(externalCacheDir.getPath() + "/temp.pcm");
            if (!tempFile.exists()) {
                Toast.makeText(applicationContext, R.string.audio_file_not_exists, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mAudioPlayer == null) {
                createAudioPlayer();
            }
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
            } else {
                int sampleRate = mAudioOutFormat.getSampleRate();
                int channels = mAudioOutFormat.getChannelMask() == AudioFormat.CHANNEL_OUT_MONO ? 1 : 2;
                if (sampleRate != mSampleRate || channels != mChannels) {
                    createAudioPlayer();
                }
                mAudioPlayer.reset();
                mAudioPlayer.setAudioSource(tempFile, mAudioOutFormat);
                mAudioPlayer.play();
            }
        }
    }

    private OutputStream mStream;

    private void createOutStream() {
        Context applicationContext = App.requireApplicationContext();
        try {
            File externalCacheDir = applicationContext.getExternalCacheDir();
            if (externalCacheDir != null) {
                mStream = new FileOutputStream(externalCacheDir.getPath() + "/temp.pcm");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
        }
    }

    private void closeOutStream() {
        if (mStream != null) {
            try {
                mStream.flush();
                mStream.close();
                mStream = null;
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void createAudioRecorder() {
        if (mAudioRecorder != null) mAudioRecorder.release();
        mAudioInFormat = new AudioFormat.Builder()
                .setSampleRate(mSampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO)
                .build();
        mAudioRecorder = new AudioRecorder<>(mAudioInFormat);
        mAudioRecorder.setCaptureListener(data -> {
            try {
                mStream.write(AudioUtils.asByteArray(data));
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
            audioFrame.postValue(mChannels == 1 ? new AudioFrame<>(data, null) : AudioUtils.separate(data));
        });
        mAudioRecorder.registerStateListener(new AbstractRecorder.RecorderEventListener() {
            @Override
            public void onStart() {
                recording.postValue(true);
            }

            @Override
            public void onPause() {
                recording.postValue(false);
            }
        });
    }

    private void createAudioPlayer() {
        if (mAudioPlayer != null) mAudioPlayer.release();
        mAudioOutFormat = new AudioFormat.Builder()
                .setSampleRate(mSampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(mChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                .build();
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.registerStateListener(new AbstractPlayer.PlayerEventListener() {
            @Override
            public void onPlay() {
                playing.postValue(true);
            }

            @Override
            public void onPause() {
                playing.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
    }
}
