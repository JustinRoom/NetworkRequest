package jsc.org.lib.netreq.http;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.SoftReference;

public abstract class UINetCallback extends NetCallback {

    private SoftReference<Dialog> reference = null;
    private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x100://start
                    showLoadingDialog();
                    break;
                case 0x101://call back
                    closeLoadingDialog();
                    onFinished();
                    Bundle data = msg.getData();
                    uiCallback(
                            getArguments(),
                            data.getInt("code"),
                            data.getString("tips"),
                            data.getString("body")
                    );
                    break;
                case 0x102://canceled
                    closeLoadingDialog();
                    onFinished();
                    uiCanceled(getArguments());
                    break;
            }
            return true;
        }
    });

    public UINetCallback(@Nullable Dialog loadingDialog) {
        if (loadingDialog != null) {
            loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (cancel() && handler != null) {
                        handler.sendEmptyMessage(0x102);
                    }
                }
            });
            reference = new SoftReference<>(loadingDialog);
        }
    }

    private void showLoadingDialog() {
        Dialog dialog = reference == null ? null : reference.get();
        if (dialog != null
                && !dialog.isShowing()) {
            //这里做兼容处理
            //防止android.view.WindowManager$BadTokenException
            Context context = dialog.getContext();
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing()) {
                    dialog.show();
                }
            }
        }
    }

    private void closeLoadingDialog() {
        Dialog dialog = reference == null ? null : reference.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        reference = null;
    }

    @Override
    protected final void onStart(@Nullable Bundle arguments) {
        super.onStart(arguments);
        handler.sendEmptyMessage(0x100);
    }

    @Override
    public final void callback(@Nullable Bundle arguments, int code, String tips, String body) {
        if (handler != null) {
            Message message = Message.obtain();
            message.what = 0x101;
            Bundle args = new Bundle();
            args.putInt("code", code);
            args.putString("tips", tips);
            args.putString("body", body);
            message.setData(args);
            handler.sendMessage(message);
        }
    }

    @Override
    protected void onFinished() {
        super.onFinished();
        handler = null;
        reference = null;
    }

    public void uiCanceled(@Nullable Bundle arguments){

    }

    public abstract void uiCallback(@Nullable Bundle arguments, int code, String tips, String body);
}
