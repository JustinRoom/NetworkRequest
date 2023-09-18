package com.jsc.netreq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jsc.netreq.databinding.ActivityLoggerTestBinding;

import jsc.org.lib.netreq.impl.LoggerImpl;

public class LoggerTestActivity extends BaseActivity {

    TextView textView = null;
    ActivityLoggerTestBinding binding = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoggerTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerImpl.getInstance().i("ViewClick", "View was clicked:" + System.currentTimeMillis(), true);
            }
        });
        binding.btnException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence txt = textView.getText();
            }
        });
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
