package com.jsc.netreq;

import android.app.Application;

import androidx.annotation.NonNull;

import jsc.org.lib.netreq.impl.LoggerImpl;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerImpl.getInstance().init(this, getExternalFilesDir("logs"), false);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                LoggerImpl.getInstance().t(null, e, true);
            }
        });
    }
}
