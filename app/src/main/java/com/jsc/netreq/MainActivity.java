package com.jsc.netreq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jsc.netreq.databinding.ActivityMainBinding;
import com.jsc.netreq.utils.ViewOutlineUtils;

public class MainActivity extends BaseActivity {

    ActivityMainBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), DownloadFileActivity.class));
            }
        });
        binding.btnTestWebsocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), WebSocketActivity.class));
            }
        });
        ViewOutlineUtils.applyEllipticOutline(binding.btnDownloadFile);
        ViewOutlineUtils.applyEllipticOutline(binding.btnTestWebsocket);
    }

    @Override
    public void onLazyLoad() {

    }
}