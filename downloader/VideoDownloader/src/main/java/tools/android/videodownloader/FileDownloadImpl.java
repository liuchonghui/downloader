package tools.android.videodownloader;

import java.io.Serializable;

public interface FileDownloadImpl extends Serializable {
    void notifyDownloadWaiting(final String key, final String url);
    void notifyDownloadStart(final String key, final String url);
    void notifyDownloadCancel(final String key, final String url);
    void notifyDownloadProgress(final String key, final String url, final int progress, final long completeSize, final long totalSize);
    void notifyDownloadSuccess(final String key, String url, String path);
    void notifyDownloadClear(final String key, boolean success, String url, String path, DownloadError error);
    void notifyDownloadFailure(final String key, String url, DownloadError error, String message);
}
