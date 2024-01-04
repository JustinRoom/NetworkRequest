package com.jsc.netreq;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jsc.netreq.databinding.ActivityRequestDataFromServerBinding;
import com.jsc.netreq.entity.ServerData;
import com.jsc.netreq.utils.ServerDataParser;
import com.jsc.netreq.utils.ViewOutlineUtils;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import jsc.org.lib.netreq.file.DownloadFileRunnable;
import jsc.org.lib.netreq.http.HttpRequester;
import jsc.org.lib.netreq.http.UINetCallback;
import jsc.org.lib.netreq.utils.Md5Utils;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class RequestDataFromServerActivity extends BaseActivity {

    ActivityRequestDataFromServerBinding binding = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestDataFromServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        ViewOutlineUtils.applyEllipticOutline(binding.btnRegister);
    }

    @Override
    public void onLazyLoad() {

    }

    private void register() {
        String url = "http://192.168.0.230/cims/mobileDevice/app_addOne";
        //响应返回主线程
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        FormBody body = new FormBody.Builder()
                .add("sbId", "test123456789")//设备ID或设备序列号
                .add("sblx", "android")
                .add("sbcj", "yunji")
                .add("fwqIp", "192.168.3.230")//
                .add("sbIp", getDeviceIpAddress())//
                .add("appbbh", getAppVersionName(this))//
                .add("jsbh", "test01")//
                .add("xxbsm", "1234567890")//
                .build();
        HttpRequester.getInstance().request(new Request.Builder()
                        .url(url)
                        .post(body)
                        .build(),
                new UINetCallback(dialog) {

                    @Override
                    public void uiCallback(@Nullable Bundle arguments, int code, String tips, String body) {
                        //主线程
                        Log.i("RequestDataFrom", body);
                        //{"ret":-1,"msg":"无对应学校标识码","body":{}}
                        ServerData data = ServerDataParser.parse(body);
                        if (data != null) {
                            Toast.makeText(getApplicationContext(), data.msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * 获取设备ip地址
     */
    public static String getDeviceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI
                    .hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = netI.getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 返回版本名字
     * 对应build.gradle中的versionName
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        if (null != context) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                versionName = packInfo.versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return versionName;
    }

}
