package cn.touchair.audiobox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import cn.touchair.audiobox.annotations.BufferType;
import cn.touchair.audiobox.common.queue.OverflowCallback;
import cn.touchair.audiobox.common.queue.OverflowQueue;
import cn.touchair.audiobox.databinding.ActivityMainBinding;
import cn.touchair.audiobox.streamin.AbstractRecorder;
import cn.touchair.audiobox.streamin.AudioRecorder;
import cn.touchair.audiobox.interfaces.CaptureListener;
import cn.touchair.audiobox.streamout.RawPlayer;

public class MainActivity extends AppCompatActivity implements CaptureListener<short[]>, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private static final int REQ_CODE_PERMISSION = 1;
    private static final String[] ALL_PERMISSIONS = new String[] {
            Manifest.permission.RECORD_AUDIO,
    };

    private boolean mAllowRecordAudio = false;
//    private AudioPlayer player = new AudioPlayer();
//    private RawPlayer player = new RawPlayer();
    @SuppressLint("MissingPermission")
    private AudioRecorder<short[]> mAudioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkPermissions();
        binding.buttonRecord.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_PERMISSION) {
            checkPermissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        player.release();
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
        }
    }

    @Override
    public void onCapture(short[] data) {
        Log.d(TAG, Arrays.toString(data));
    }

    private void checkPermissions() {
        boolean needReq = false;
        ArrayList<String> requestList = new ArrayList<>();
        for (String permission : ALL_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                if (!needReq) needReq = true;
                requestList.add(permission);
            }
        }
        if (needReq) {
            requestPermissions(requestList.toArray(new String[0]), REQ_CODE_PERMISSION);
        } else {
            mAllowRecordAudio = true;
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.button_record) {
            if (mAllowRecordAudio) {
                toggleRecord();
                updateUI();
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        binding.buttonRecord.setIconResource(mAudioRecorder.isRecording() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        binding.audioWaveView.clear();
    }

    private void toggleRecord() {
        if (mAudioRecorder == null) {
            createAudioRecorder();
        }
        if (mAudioRecorder.isRecording()) {
            mAudioRecorder.pause();
        } else {
            mAudioRecorder.start();
        }
    }

    @SuppressLint("MissingPermission")
    private void createAudioRecorder() {
        mAudioRecorder = new AudioRecorder<>();
        mAudioRecorder.setCaptureListener(data -> {
            /*handle audio data*/
            binding.audioWaveView.updateData(data);
        });
    }
}