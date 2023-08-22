package jsc.org.lib.netreq.file;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.concurrent.Executor;

import jsc.org.lib.netreq.utils.Md5Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DownloadFileRunnable implements Runnable {

    private Handler mHandler;
    private Bundle arguments;
    private int tryCount = 0;
    private String key = "";
    private boolean cancelled = false;
    private Call<ResponseBody> mCall = null;

    public DownloadFileRunnable(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public final DownloadFileRunnable bindArguments(Bundle arguments) {
        this.arguments = arguments;
        return this;
    }

    public final void cancel() {
        cancelled = true;
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    public boolean isSameFileMd5(File localFile, String md5) {
        return Md5Utils.calcFileMd5(localFile).equals(md5);
    }

    @Override
    public void run() {
        String url = arguments == null ? null : arguments.getString("url");
        String md5 = arguments == null ? null : arguments.getString("md5");
        String filePath = arguments == null ? null : arguments.getString("filePath");
        long timeOut = arguments == null ? 20 : Math.max(0, arguments.getLong("timeOutSec"));
        key = arguments == null ? null : arguments.getString("key");
        if (TextUtils.isEmpty(key)) {
            key = "defaultKey";
        }
        boolean range = arguments != null && arguments.getBoolean("range");
        final int maxTryCount = arguments == null ? 1 : Math.max(0, arguments.getInt("maxTryCount"));
        assert !TextUtils.isEmpty(url) : "No 'url'.";
        assert !TextUtils.isEmpty(filePath) : "No 'filePath'.";
        begin(url, filePath);
        DownloadFileMonitor monitor = new DownloadFileMonitor() {
            @Override
            public void onRequestCreated(Call<ResponseBody> call) {
                mCall = call;
            }

            @Override
            public boolean isEquivalentFileMd5(File localFile, String md5) {
                return isSameFileMd5(localFile, md5);
            }

            @Override
            public void tryToDownloadWhenDiffFileMd5() {
                tryCount++;
            }

            @Override
            public boolean canTryToDownloadWhenDiffFileMd5() {
                return tryCount <= maxTryCount;
            }

            @Override
            public boolean isDownloadCancelled() {
                return isCancelled();
            }

            @Override
            public void onDownloadProgress(long progress, long total) {
                progress(progress, total);
            }

            @Override
            public void onDownloadError(String message) {
                error(message);
                releaseResource();
            }

            @Override
            public void onDownloadEnd(String filePath) {
                end(filePath);
                releaseResource();
            }
        };
        if (range) {
            DownloadFileApi.rangeDownloadFile(url, md5, filePath, monitor, timeOut);
        } else {
            DownloadFileApi.downloadFile(url, md5, filePath, monitor, timeOut);
        }
    }

    private void releaseResource() {
        mHandler = null;
        mCall = null;
    }

    public DownloadFileRunnable submit() {
        new Thread(this).start();
        return this;
    }

    public DownloadFileRunnable submit(Executor executor) {
        executor.execute(this);
        return this;
    }

    private void begin(String url, String filePath) {
        if (mHandler != null) {
            Bundle data = new Bundle();
            data.putString("key", key);
            data.putString("url", url);
            data.putString("filePath", filePath);
            Message message = Message.obtain();
            message.what = 0x9000;
            message.setData(data);
            mHandler.sendMessage(message);
        }
    }

    private void progress(long progress, long total) {
        if (mHandler != null) {
            Bundle data = new Bundle();
            data.putString("key", key);
            data.putLong("progress", progress);
            data.putLong("total", total);
            Message message = Message.obtain();
            message.what = 0x9001;
            message.setData(data);
            mHandler.sendMessage(message);
        }
    }

    private void error(String errorMsg) {
        if (mHandler != null) {
            Bundle data = new Bundle();
            data.putString("key", key);
            data.putString("errorMsg", errorMsg);
            Message message = Message.obtain();
            message.what = 0x9002;
            message.setData(data);
            mHandler.sendMessage(message);
        }
    }

    private void end(String filePath) {
        if (mHandler != null) {
            Bundle data = new Bundle();
            data.putString("key", key);
            data.putString("filePath", filePath);
            Message message = Message.obtain();
            message.what = 0x9003;
            message.setData(data);
            mHandler.sendMessage(message);
        }
    }
}
