package tools.android.simpledownloader;

import java.io.Serializable;

public interface DownloadWorker extends Runnable, Serializable {
    String getFileName();

    long getDownloadFileSize(String inputUrl);

    String isFileExist(String dir, String fileName, long targetSize);

    long getlocalCacheFileSize(String dir, String fileName);
}