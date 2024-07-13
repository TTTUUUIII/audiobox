package cn.touchair.audiobox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.touchair.audiobox.common.AudioFrame;
import cn.touchair.audiobox.databinding.ActivityMainBinding;
import cn.touchair.audiobox.streamin.AudioRecorder;
import cn.touchair.audiobox.streamout.AbstractPlayer;
import cn.touchair.audiobox.streamout.AudioPlayer;
import cn.touchair.audiobox.util.AudioUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private static final int REQ_CODE_PERMISSION = 1;
    private static final String[] ALL_PERMISSIONS = new String[] {
            Manifest.permission.RECORD_AUDIO,
    };

    private boolean mAllowRecordAudio = false;
    private boolean mDelayedRecordAudio = false;
    private AudioPlayer mAudioPlayer;
//    private RawPlayer player = new RawPlayer();
    @SuppressLint("MissingPermission")
    private AudioRecorder<short[]> mAudioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonRecordAudio.setOnClickListener(this);
        binding.buttonPlayAudio.setOnClickListener(this);
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_PERMISSION) {
            boolean success = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    success = false;
                    break;
                }
            }
            if (success) {
                mAllowRecordAudio = true;
                if (mDelayedRecordAudio) {
                    binding.buttonRecordAudio.callOnClick();
                    mDelayedRecordAudio = false;
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        player.release();
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
    }

    private void checkPermissions() {
        boolean noNeedRequest = true;
        for (String permission : ALL_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                noNeedRequest = false;
                break;
            }
        }

        if (noNeedRequest) {
            mAllowRecordAudio = true;
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.button_record_audio) {
            toggleRecord();
        } else if (viewId == R.id.button_play_audio) {
            togglePlay();
        }
    }

    private void updateUI() {
        if (mAudioRecorder != null) {
            binding.buttonRecordAudio.setIconResource(mAudioRecorder.isRecording() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
            if (!mAudioRecorder.isRecording()) {
                binding.audioWaveView.clear();
            }
        }
        if (mAudioPlayer != null) {
            binding.buttonPlayAudio.setIconResource(mAudioPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        }
    }

    private void toggleRecord() {
        if (mAudioPlayer != null && mAudioPlayer.isPlaying()) {
            Toast.makeText(getApplicationContext(), R.string.stop_player, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAudioRecorder == null) {
            createAudioRecorder();
        }
        if (mAudioRecorder != null) {
            if (mAudioRecorder.isRecording()) {
                mAudioRecorder.pause();
                closeOutStream();
            } else {
                createOutStream();
                mAudioRecorder.start();
            }
            updateUI();
        }
    }

    private void togglePlay() {
        if (mAudioRecorder != null && mAudioRecorder.isRecording()) {
            Toast.makeText(getApplicationContext(), R.string.stop_recorder, Toast.LENGTH_SHORT).show();
            return;
        }
        File externalCacheDir = getExternalCacheDir();
        if (externalCacheDir != null) {
            File tempFile = new File(externalCacheDir.getPath() + "/temp.pcm");
            if (!tempFile.exists()) {
                Toast.makeText(getApplicationContext(), R.string.audio_file_not_exists, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mAudioPlayer == null) {
                createAudioPlayer();
            }
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
            } else {
                mAudioPlayer.reset();
                mAudioPlayer.setAudioSource(tempFile, null);
                mAudioPlayer.play();
            }
        }
    }

    private OutputStream mStream;

    private void createOutStream() {
        try {
            File externalCacheDir = getExternalCacheDir();
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
        if (mAllowRecordAudio) {
            if (mAudioRecorder != null) {
                mAudioRecorder.release();
            }
            mAudioRecorder = new AudioRecorder<>();
            mAudioRecorder.setCaptureListener(stereoData -> {
                try {
                    mStream.write(AudioUtils.asByteArray(stereoData));
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
                AudioFrame<short[]> frame = AudioUtils.separate(stereoData);
                binding.audioWaveView.updateAudioData(frame);
            });
        } else {
            mDelayedRecordAudio = true;
            requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO }, REQ_CODE_PERMISSION);
        }
    }

    private void createAudioPlayer() {
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.registerPlayerListener(new AbstractPlayer.PlayerEventListener() {
            @Override
            public void onPlay() {
                updateUI();
            }

            @Override
            public void onPause() {
                updateUI();
            }
        });
    }
}