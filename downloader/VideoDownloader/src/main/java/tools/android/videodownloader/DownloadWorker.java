package tools.android.videodownloader;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DownloadWorker extends Runnable, Serializable {
    String getKey();

    String getFileName();

    long getDownloadFileSize(String inputUrl);

    String isFileExist(String dir, String fileName, long targetSize);

    long getlocalCacheFileSize(String dir, String fileName);

    void setFuture(Future future);

    Future getFuture();

    void setInterrupt(InterruptReason reason);

    boolean running();

    long startTimeStamp();
}
