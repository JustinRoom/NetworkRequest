# 网络请求

### 1、文件下载
```
    final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case 0x9000://开始下载
                    break;
                case 0x9001://下载中
                    long progress = data.getLong("progress");
                    long total = data.getLong("total");
                    break;
                case 0x9002://下载出错(或取消下载)
                    String tips = data.getString("errorMsg");
                    break;
                case 0x9003://下载完成
                    String localFilePath = data.getString("filePath");
                    break;
            }
        }
    };
```

#### 1.1、普通下载
```
    private void download() {
        String url = "http://192.168.0.230/download/.../ceshi.apk";
        File file = new File(getExternalFilesDir("download"), "ceshi.apk");
        boolean range = binding.cbRange.isChecked();
        Bundle arguments = new Bundle();
        //key:区分线程，便于做线程管理(非必要可不传)
        arguments.putString("key", "mPersonalKey");
        //url:下载地址(必传)
        arguments.putString("url", url);
        //md5:如果设定，下载文件流程中会进行md5码校验，检验文件完整性；如果不传，则不进行md5码校验。(可不传)
        arguments.putString("md5", "");
        //filePath:保存地址(必传)
        arguments.putString("filePath", file.getPath());
        //range:false普通下载
        arguments.putBoolean("range", false);
        //md5码校验不通过时自动尝试重新下载文件最大次数，避免死循环(默认为1)
        arguments.putInt("maxTryCount", 2);
        //timeOutSec:连接超时时间、读写超时时间(单位：秒)
        arguments.putLong("timeOutSec", 10);
        runnable = new DownloadFileRunnable(mHandler)
                .bindArguments(arguments)
                .submit();
    }
```

#### 1.2、断点续传
```
    private void download() {
        String url = "http://192.168.0.230/download/.../ceshi.apk";
        File file = new File(getExternalFilesDir("download"), "ceshi.apk");
        boolean range = binding.cbRange.isChecked();
        Bundle arguments = new Bundle();
        //key:区分线程，便于做线程管理(非必要可不传)
        arguments.putString("key", "mPersonalKey");
        //url:下载地址(必传)
        arguments.putString("url", url);
        //md5:如果设定，下载文件流程中会进行md5码校验，检验文件完整性；如果不传，则不进行md5码校验。(可不传)
        arguments.putString("md5", "");
        //filePath:保存地址(必传)
        arguments.putString("filePath", file.getPath());
        //range:true断点续传
        arguments.putBoolean("range", true);
        //md5码校验不通过时自动尝试重新下载文件最大次数，避免死循环(默认为1)
        arguments.putInt("maxTryCount", 1);
        //timeOutSec:连接超时时间、读写超时时间(单位：秒)
        arguments.putLong("timeOutSec", 10);
        runnable = new DownloadFileRunnable(mHandler)
                .bindArguments(arguments)
                .submit();
    }
```

### 2、网络请求

#### 2.1、初始化
```
        HttpRequester.getInstance().register(getApplicationContext());
        OkHttpClient.Builder builder = HttpClientUtils.createOkHttpClientBuilder(20L);
        HttpClientUtils.addHttpLoggingInterceptor(builder);
        //可根据自己的业务需求调整
//        HttpClientUtils.addRequestRetryInterceptor(builder, 2);
//        HttpClientUtils.addCacheValidDateInterceptor(builder, getApplicationContext());
//        HttpClientUtils.addCookieJar(builder);
//        HttpClientUtils.addCache(builder, getApplicationContext());
//        HttpClientUtils.addCustomInterceptor(builder, null);
        HttpRequester.getInstance().bindClient(builder.build());
```

#### 2.2、请求示例
```
        //响应返回主线程
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        HttpRequester.getInstance().request(request,
                new UINetCallback(dialog) {

                    @Override
                    protected boolean dealOriginalResponse(@Nullable Bundle arguments, Response response) {
                        //子线程
                        //如果此方法返回true，则最终不会回调uiCallback方法
                        return super.dealOriginalResponse(arguments, response);
                    }

                    @Override
                    public void uiCallback(@Nullable Bundle arguments, int code, String tips, String body) {
                        //主线程
                        //arguments自定义入参
                        //code网络请求响应码
                        //tips提示文案
                        //body请求响应体
                    }
                }.setArguments(arguments));
              
        //响应保留在子线程 
        HttpRequester.getInstance().request(request,
                new NetCallback() {

                    @Override
                    protected boolean dealOriginalResponse(@Nullable Bundle arguments, Response response) {
                        //子线程
                        //如果此方法返回true，则最终不会回调callback方法
                        return super.dealOriginalResponse(arguments, response);
                    }

                    @Override
                    public void callback(@Nullable Bundle arguments, int code, String tips, String body) {
                        //子线程
                        //arguments自定义入参
                        //code网络请求响应码
                        //tips提示文案
                        //body请求响应体
                    }
                }.setArguments(arguments));
```

#### 2.2.1、application/x-www-form-urlencoded POST
```
        String url = "http://192.168.3.230/identityAuth/token";
        String requestParams = new URLParamsBuilder()
                .addParams("key0", "value0")
                .addParams("key1", "value1")
                .addParams("key2", "value2")
                .addParams("key3", "value3")
                .params();
        RequestBody requestBody = RequestBody.create(requestParams, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
```

#### 2.2.2、application/json POST
```
        String url = "http://192.168.3.230/identityAuth/token";
        JSONObject obj = new JSONObject();
        try {
            obj.put("key0", "value0");
            obj.put("key1", "value1");
            obj.put("key2", "value2");
            obj.put("key3", "value3");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse(obj.toString(), "application/json; charset=utf-8"));
        Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
```

#### 2.2.3、GET
```
        Request request = new Request.Builder()
                .url(new URLParamsBuilder()
                        .baseUrl("http://192.168.3.230")
                        .actionUrl("/identityAuth/token")
                        .addParams("key0", "value0")
                        .addParams("key1", "value1")
                        .addURLEncoderParams("key2", "value2")
                        .addURLEncoderParams("key3", "value3")
                        .build())
                .get()
                .build();
```

#### 2.2.4、多文件提交multipart POST
```
        String url = "http://192.168.3.230/identityAuth/token";
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .addFormDataPart("key0", "value0")
                .addFormDataPart("ke1", "value1")
                .addFormDataPart("key2", "value2")
                .addFormDataPart("key3", "value3");
        String[] splits = TextUtils.isEmpty(photoList) ? null : photoList.split(";", -1);
        if (splits != null && splits.length > 0) {
            for (String path : splits) {
                File file = new File(path);
                if (file.exists()) {
                    builder.addFormDataPart("files", file.getName(), MultipartBody.create(file, MediaType.parse("image/*")));
                }
            }
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
```

#### 2.2.5、表单提交form POST
```
        String url = "http://192.168.3.230/identityAuth/token";
        FormBody.Builder builder = new FormBody.Builder()
                .add("key0", "value0")
                .add("key1", "value1")
                .add("key2", "value2");
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
```