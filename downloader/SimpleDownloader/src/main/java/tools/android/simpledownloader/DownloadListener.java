package tools.android.simpledownloader;

import android.content.Context;

import java.io.Serializable;

public interface DownloadListener extends Serializable {
    String getReleaseCode();
    boolean checkInitialized(Context ctx, String url);
    void onDownloadStart(String url);
    void onDownloadCancel(String url);
    void onDownloadProgress(String url, int progress);
    void onDownloadSuccess(String url, String path);
    void onDownloadFailure(String url, String message);
    void onDownloadClear(boolean success, String url, String path);
}