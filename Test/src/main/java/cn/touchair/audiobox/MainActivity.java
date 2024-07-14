package cn.touchair.audiobox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Toast;

import java.io.File;

import cn.touchair.audiobox.databinding.ActivityMainBinding;
import cn.touchair.audiobox.view.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private ActivityMainBinding binding;
    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        setContentView(binding.getRoot());
        binding.setViewModel(mViewModel);
        binding.setLifecycleOwner(this);
        mViewModel.request.observe(this, request -> {
            if (request == MainActivityViewModel.REQUEST_AUDIO_RECORD_PERMISSION) {
                requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        });
        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean success = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    success = false;
                    break;
                }
            }
            mViewModel.setSystemAllowRecordAudio(success);
            if (!success) {
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView() {
        String[] channels = getResources().getStringArray(R.array.channels);
        String[] sampleRates = getResources().getStringArray(R.array.sample_rates);
        ArrayAdapter<String> sliderChannelsAdapter = new ArrayAdapter<String>(this, R.layout.layout_simple_text, channels) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new InvalidFilter();
            }
        };
        ArrayAdapter<String> sliderSampleRatesAdapter = new ArrayAdapter<String>(this, R.layout.layout_simple_text, sampleRates) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new InvalidFilter();
            }
        };
        binding.sliderChannels.setAdapter(sliderChannelsAdapter);
        binding.sliderSampleRate.setAdapter(sliderSampleRatesAdapter);
    }

    private static class InvalidFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

        }
    }
}