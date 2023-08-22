package com.jsc.netreq;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jsc.netreq.databinding.ActivityDownloadFileBinding;
import com.jsc.netreq.utils.ViewOutlineUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;

import jsc.org.lib.netreq.file.DownloadFileRunnable;
import jsc.org.lib.netreq.utils.Md5Utils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DownloadFileActivity extends BaseActivity {

    ActivityDownloadFileBinding binding = null;
    DecimalFormat format = new DecimalFormat();
    final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case 0x9000:
                    binding.tvProgress.setText("初始化下载");
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    break;
                case 0x9001:
                    long progress = data.getLong("progress");
                    long total = data.getLong("total");
                    float value = progress * 1.0f / total * 100;
                    binding.tvProgress.setText(String.format(Locale.US, "正在下载:%s", format.format(value) + "%"));
                    break;
                case 0x9002:
                    binding.btnCancel.setVisibility(View.INVISIBLE);
                    binding.tvProgress.setText(data.getString("errorMsg"));
                    cancel();
                    break;
                case 0x9003:
                    String filePath = data.getString("filePath");
                    binding.btnCancel.setVisibility(View.INVISIBLE);
                    binding.tvProgress.setText("下载成功:" + filePath);
                    cancel();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        ViewOutlineUtils.applyEllipticOutline(binding.btnStart);
        ViewOutlineUtils.applyEllipticOutline(binding.btnCancel);
        format.setMaximumFractionDigits(2);
    }

    @Override
    public void onLazyLoad() {

    }

    private DownloadFileRunnable runnable = null;

    private void download() {
        String url = "http://192.168.0.230/download/appak/1689923434238/AuthMegRelease(SXL2)_3.2.1.137 (2).apk";
        File file = new File(getExternalFilesDir("download"), "AuthMegRelease(SXL2)_3.2.1.137.apk");
        boolean range = binding.cbRange.isChecked();
        Bundle arguments = new Bundle();
        arguments.putString("key", "mPersonalKey");
        arguments.putString("url", url);
        arguments.putString("md5", Md5Utils.calcFileMd5(file));
        arguments.putString("filePath", file.getPath());
        arguments.putBoolean("range", range);
        arguments.putInt("maxTryCount", 2);
        arguments.putLong("timeOutSec", 10);
        runnable = new DownloadFileRunnable(mHandler)
                .bindArguments(arguments)
                .submit();
    }

    private void cancel() {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
    }

    void sadf(){

    }
}
