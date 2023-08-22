package jsc.org.lib.netreq.file;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface DownloadFileService {
    /**
     * 下载文件。Streaming注解避免把整个文件加载到内存中导致内存溢出
     * @param fileUrl
     * @return
     */
    @Streaming
    @GET
    Call<ResponseBody> downloadFileByDynamicUrlAsync(@Url String fileUrl);

    /**
     * 下载文件。Streaming注解避免把整个文件加载到内存中导致内存溢出
     * @param fileUrl
     * @return
     */
    @Streaming
    @GET
    Call<ResponseBody> downloadFileByDynamicUrlAsync(@Header ("Range") String range, @Url String fileUrl);
}
