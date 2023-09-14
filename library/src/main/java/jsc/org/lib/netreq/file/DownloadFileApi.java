package jsc.org.lib.netreq.file;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;

import jsc.org.lib.netreq.utils.HttpClientUtils;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public final class DownloadFileApi {

    public static void rangeDownloadFile(String url,
                                         String md5,
                                         String filePath,
                                         @NonNull DownloadFileMonitor monitor,
                                         long timeout) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            error(null, "无效的url.", monitor);
            return;
        }
        String baseUrl = String.format(Locale.US, "%s://%s:%d/", httpUrl.scheme(), httpUrl.host(), httpUrl.port());
        List<String> segments = httpUrl.pathSegments();
        String path = String.join("/", segments);
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean mr = parent.mkdirs();
        }
        boolean needCheckMd5 = file.exists() && !TextUtils.isEmpty(md5);
        long range = file.exists() ? file.length() : 0L;
        InputStream is = null;
        Call<ResponseBody> call = createDownloadFileCall(baseUrl, path, "bytes=" + range + "-", timeout);
        monitor.onRequestCreated(call);
        try {
            Response<ResponseBody> response = call.execute();
            Log.d("DownloadFileApi", "{下载文件{RequestUrl:" + httpUrl + ", ResponseCode:" + response.code() + "}");
            if (response.code() == 416) {
                boolean dr = file.delete();
                range = 0L;
                call = createDownloadFileCall(baseUrl, path, "bytes=" + range + "-", timeout);
                response = call.execute();
            }
            ResponseBody body = response.body();
            //contentLength()指本次需要传输的字节长度(如果range等于0则contentLength等于文件总长度)
            long total = body.contentLength() + range;

            //contentRange 例如：bytes 0-86977744/86977745
            String contentRange = response.headers().get("Content-Range");
            if (range == total
                    && needCheckMd5
                    && monitor.isEquivalentFileMd5(file, md5)) {
                monitor.onDownloadEnd(filePath);
                return;
            }
            if (!file.exists()) {
                boolean cr = file.createNewFile();
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(range);
            is = body.byteStream();
            ReadableByteChannel fisChannel = Channels.newChannel(is);
            FileChannel fosChannel = randomAccessFile.getChannel();
            //通道没有办法传输数据，必须依赖缓冲区
            //分配指定大小的缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
            long progress = range;
            int count = 0;
            while (!monitor.isDownloadCancelled()) {
                int len = fisChannel.read(byteBuffer);
                if (len == -1) {
                    monitor.onDownloadProgress(progress, total);
                    break;
                }
                //切换成读数据模式
                byteBuffer.flip();
                //将缓冲区中的数据写入通道
                fosChannel.write(byteBuffer);
                //分发下载进度
                progress += len;
                count++;
                if (count >= 25) {
                    count = 0;
                    monitor.onDownloadProgress(progress, total);
                }
                //清空缓冲区
                byteBuffer.clear();
            }
            fosChannel.close();
            fisChannel.close();
            randomAccessFile.close();
            is.close();
            if (monitor.isDownloadCancelled()) {
                error(null, "已取消下载。", monitor);
                return;
            }
            if (progress != total) {
                error(file, "下载出错。", monitor);
                return;
            }
            if (!TextUtils.isEmpty(md5) && !monitor.isEquivalentFileMd5(file, md5)) {
                //md5不一致，重新下载
                if (monitor.canTryToDownloadWhenDiffFileMd5()) {
                    boolean dr = file.delete();
                    monitor.tryToDownloadWhenDiffFileMd5();
                    rangeDownloadFile(url, md5, filePath, monitor, timeout);
                } else {
                    error(file, "下载失败:MD5码不一致。", monitor);
                }
                return;
            }
            monitor.onDownloadEnd(filePath);
        } catch (IOException | NullPointerException e) {
            if (monitor.isDownloadCancelled()) {
                error(null, "已取消下载。", monitor);
            } else {
                error(file, e.getLocalizedMessage(), monitor);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadFile(String url,
                                    String md5,
                                    String filePath,
                                    @NonNull DownloadFileMonitor monitor,
                                    long timeout) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (null == httpUrl) {
            error(null, "无效的url.", monitor);
            return;
        }
        String baseUrl = String.format(Locale.US, "%s://%s:%d/", httpUrl.scheme(), httpUrl.host(), httpUrl.port());
        List<String> segments = httpUrl.pathSegments();
        String path = String.join("/", segments);
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean mr = parent.mkdirs();
        }
        InputStream is = null;
        FileOutputStream fos = null;
        Call<ResponseBody> call = createDownloadFileCall(baseUrl, path, timeout);
        monitor.onRequestCreated(call);
        try {
            Response<ResponseBody> response = call.execute();
            ResponseBody body = response.body();
            long total = body.contentLength();
            if (file.exists()) {
                boolean dr = file.delete();
            }
            boolean cr = file.createNewFile();

            is = body.byteStream();
            fos = new FileOutputStream(file);
            long progress = 0;
            int count = 0;
            byte[] buf = new byte[4096];
            while (!monitor.isDownloadCancelled()) {
                int len = is.read(buf);
                if (len == -1) {
                    monitor.onDownloadProgress(progress, total);
                    break;
                }
                fos.write(buf, 0, len);
                progress += len;
                count++;
                if (count == 25) {
                    fos.flush();
                    count = 0;
                    monitor.onDownloadProgress(progress, total);
                }
            }
            fos.flush();
            if (monitor.isDownloadCancelled()) {
                error(file, "已取消下载。", monitor);
                return;
            }
            if (progress != total) {
                error(file, "下载出错。", monitor);
                return;
            }
            if (!TextUtils.isEmpty(md5) && !monitor.isEquivalentFileMd5(file, md5)) {
                //md5不一致，重新下载
                if (monitor.canTryToDownloadWhenDiffFileMd5()) {
                    boolean dr = file.delete();
                    monitor.tryToDownloadWhenDiffFileMd5();
                    downloadFile(url, md5, filePath, monitor, timeout);
                } else {
                    error(file, "下载失败:MD5码不一致。", monitor);
                }
                return;
            }
            monitor.onDownloadEnd(filePath);
        } catch (IOException | NullPointerException e) {
            error(file, e.getLocalizedMessage(), monitor);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Call<ResponseBody> createDownloadFileCall(String baseUrl, String path, String range, long timeout) {
        return new Retrofit.Builder()
                .client(HttpClientUtils.createOkHttpClientBuilder(timeout).build())
                .baseUrl(baseUrl)
                .build()
                .create(DownloadFileService.class)
                .downloadFileByDynamicUrlAsync(range, path);
    }

    public static Call<ResponseBody> createDownloadFileCall(String baseUrl, String path, long timeout) {
        return new Retrofit.Builder()
                .client(HttpClientUtils.createOkHttpClientBuilder(timeout).build())
                .baseUrl(baseUrl)
                .build()
                .create(DownloadFileService.class)
                .downloadFileByDynamicUrlAsync(path);
    }

    private static void error(File file, String message, @NonNull DownloadFileMonitor monitor) {
        if (file != null) {
            boolean dr = file.delete();
        }
        monitor.onDownloadError(message);
        Log.d("DownloadFileApi", message);
    }
}
