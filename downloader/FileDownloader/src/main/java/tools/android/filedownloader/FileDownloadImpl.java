package tools.android.filedownloader;

import java.io.Serializable;

public interface FileDownloadImpl extends Serializable {
    void notifyDownloadStart(final String url);
    void notifyDownloadProgress(final String url, final int progress);
    void notifyDownloadSuccess(String url, String path);
    void notifyDownloadClear(boolean success, String url, String path);
    void notifyDownloadFailure(String url, String message);
}
