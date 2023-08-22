package jsc.org.lib.netreq.file;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;

public interface DownloadFileMonitor {

    void onRequestCreated(Call<ResponseBody> call);

    boolean isEquivalentFileMd5(File localFile, String md5);

    void tryToDownloadWhenDiffFileMd5();

    boolean canTryToDownloadWhenDiffFileMd5();

    boolean isDownloadCancelled();

    void onDownloadProgress(long progress, long total);

    void onDownloadError(String message);

    void onDownloadEnd(String filePath);
}
