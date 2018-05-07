package tools.android.videodownloader

import android.content.Context

abstract class DownloadAdapter : DownloadListener {
    companion object {
        var debug: Boolean = false
    }

    override fun getReleaseCode(): String {
        return hashCode().toString()
    }

    override fun checkInitialized(ctx: Context, url: String?): Boolean {
        return false
    }

    override fun onDownloadWaiting(url: String) {}

    override fun onDownloadStart(url: String) {}

    override fun onDownloadCancel(url: String) {}

    override fun onDownloadProgress(url: String, progress: Int, completeSize: Long, totalSize: Long) {}

    override fun onDownloadSuccess(url: String, path: String) {}

    override fun onDownloadFailure(url: String, error: DownloadError?, message: String?) {}

    override fun onDownloadClear(success: Boolean, url: String?, path: String?, error: DownloadError?) {}
}