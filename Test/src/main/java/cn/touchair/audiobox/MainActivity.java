package cn.touchair.audiobox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import cn.touchair.audiobox.streamin.AudioRecorder;
import cn.touchair.audiobox.streamin.CaptureListener;
import cn.touchair.audiobox.streamout.AudioPlayer;
import cn.touchair.audiobox.streamout.RawPlayer;

public class MainActivity extends AppCompatActivity implements CaptureListener<short[]> {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_CODE_PERMISSION = 1;
    private static final String[] ALL_PERMISSIONS = new String[] {
            Manifest.permission.RECORD_AUDIO,
    };
//    private AudioPlayer player = new AudioPlayer();
//    private RawPlayer player = new RawPlayer();
    @SuppressLint("MissingPermission")
    private AudioRecorder<Short, short[]> recorder;

    @SuppressLint("MissingPermission")
    private final Runnable onLaunchAction = () -> {
        recorder = new AudioRecorder<>(Short.class);
        recorder.setCaptureListener(this);
        recorder.start();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        launchWhenPermissionGrant();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_PERMISSION) {
            launchWhenPermissionGrant();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        player.release();
        if (recorder != null) {
            recorder.release();
        }
    }

    @Override
    public void onCapture(short[] data) {
        Log.d(TAG, Arrays.toString(data));
    }

    private void launchWhenPermissionGrant() {
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
            onLaunchAction.run();
        }
    }
}