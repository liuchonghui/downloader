package tools.android.simpledownloader

import android.compact.utils.FileCompactUtil
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SimpleDownloadManager : FileDownloadImpl {
    companion object {
        private var instance: SimpleDownloadManager? = null
        fun get(): SimpleDownloadManager {
            if (instance == null) {
                synchronized(SimpleDownloadManager::class.java) {
                    if (instance == null) {
                        instance = SimpleDownloadManager()
                    }
                }
            }
            return instance!!
        }
    }

    constructor() {
        initHandler("init thread handler at construct")
    }

    private fun initHandler(desc: String?) {
        if (mHandler == null) {
            var postThread = HandlerThread("simple-download-post-state-thread")
            postThread.start()
            mHandler = Handler(postThread.looper)
            if (!TextUtils.isEmpty(desc)) {
                Log.d("SDM", desc)
            }
        }
    }

    protected var downloadExecutor: ExecutorService? = null
    protected var mHandler: Handler? = null
    protected var pair = ConcurrentHashMap<String, List<DownloadListener>>()
    protected var cache: ConcurrentHashMap<String, String>? = ConcurrentHashMap()
    internal var cachePath: String? = null

    fun setDownloadCacheDir(path: String?) {
        cachePath = path
    }

    fun getDownloadCacheDir(context: Context): String? {
        if (TextUtils.isEmpty(cachePath)) {
            cachePath = FileCompactUtil.getTempDirPath(context)
        }
        return cachePath
    }

    private fun writeCache(id: String?, value: String?) {
        if (id == null || id.length == 0) {
            return
        }
        if (value == null || value.length == 0) {
            return
        }
        if (cache != null) {
            cache!!.put(id, value)
        }
    }

    private fun getValue(key: String?): String? {
        var value: String? = null
        if (key == null || key.length == 0) {
            return value
        }
        if (cache != null) {
            value = cache!![key]
        }
        return value
    }

    fun list(): List<String> {
        val mess = ArrayList<String>()
        if (cache != null) {
            mess.addAll(cache!!.values)
        }
        return mess
    }

    fun removeCache(value: String) {
        if (cache != null) {
            if (cache!!.values.contains(value)) {
                val origSize = cache!!.values.size
                val c = ArrayList<String>()
                c.add(value)
                try {
                    cache!!.values.removeAll(c)
                } catch (e: Exception) {
                }

                val currSize = cache!!.values.size
                for (i in currSize until origSize) {
                    Log.d("PDML", "remove cache " + value)
                }
            }
        }
    }

    fun remove(key: String): Boolean {
        var value: String? = null
        if (cache != null) {
            try {
                value = cache!!.remove(key)
            } catch (e: Exception) {
            }

        }
        return value != null && value.length > 0
    }

    @Synchronized
    fun downloadSimpleFile(context: Context, identify: String, suffix: String?, md5: String?,
                       url: String?, listener: DownloadListener?) {
        initHandler("init thread handler at download request")
        if (url == null || url.length == 0) {
            return
        }
        var l: DownloadListener? = listener
        if (l == null) {
            l = object : DownloadAdapter() {
            }
        }
        if (l.checkInitialized(context, url)) {
            return
        }
        var ls: List<DownloadListener>? = pair[url]
        if (ls == null || ls.size == 0) {
            if (ls == null) {
                ls = ArrayList()
            }
            Log.d("SDM", "SimpleDownloadManager add task:" + l.hashCode() + "|" + l.releaseCode + "|" + identify)
            ls += l
            pair.put(url, ls)
            val worker = createFileDownloadWorker(url,
                    getDownloadCacheDir(context), identify, suffix, md5)
            if (downloadExecutor == null) {
                downloadExecutor = Executors.newFixedThreadPool(2) { runnable ->
                    val thread = Thread(runnable,
                            "SDM download-worker")
                    thread.priority = Thread.MAX_PRIORITY - 1
                    thread
                }
            }
            // 相同的url，跑一个任务
            downloadExecutor!!.submit(worker)

        } else {
            Log.d("SDM", "SimpleDownloadManager add task+" + l.hashCode() + "|" + l.releaseCode + "|" + identify)
            ls += l
            pair.put(url, ls)
            Log.d("SDM", "ls size|" + ls.size)
        }
    }

    protected fun createFileDownloadWorker(url: String?, cachePath: String?,
                                           fileName: String?, suffix: String?, md5: String?): DownloadWorker {
        return URLConnectionWorker(url, cachePath, fileName, suffix, md5)
    }

    override fun notifyDownloadStart(url: String) {
        initHandler("init thread handler at download start")
        val ls = pair[url]
        if (ls != null) {
            mHandler?.post {
                for (l in ls) {
                    l.onDownloadStart(url)
                }
            }
        }
    }

    override fun notifyDownloadFailure(url: String, message: String?) {
        initHandler("init thread handler at download failure")
        val ls = pair[url]
        if (ls != null) {
            mHandler?.post {
                for (l in ls) {
                    l.onDownloadFailure(url, message)
                }
            }
        }
    }

    fun notifyDownloadCancel(url: String) {
        initHandler("init thread handler at download cancel")
        val ls = pair[url]
        if (ls != null) {
            mHandler?.post {
                for (l in ls) {
                    l.onDownloadCancel(url)
                }
            }
        }
    }

    override fun notifyDownloadProgress(url: String, progress: Int) {
        initHandler("init thread handler at download progress")
        val ls = pair[url]
        if (ls != null) {
            mHandler?.post {
                for (l in ls) {
                    l.onDownloadProgress(url, progress)
                }
            }
        }
    }

    override fun notifyDownloadSuccess(url: String, path: String) {
        initHandler("init thread handler at download success")
        writeCache(url, path)
        writeCache(path, path)
        val ls = pair[url]
        if (ls != null) {
            mHandler?.post {
                Log.d("SDM", "download success ls size:" + ls.size)
                for (l in ls) {
                    l.onDownloadSuccess(url, path)
                }
            }
        }
    }

    override fun notifyDownloadClear(success: Boolean, url: String,
                                     path: String?) {
        initHandler("init thread handler at download clear")
        var ls = pair.remove(url)
        if (ls != null) {
            mHandler?.post {
                for (l in ls) {
                    Log.d("SDM", "download complete ls size:" + ls.size)
                    l.onDownloadClear(success, url, path)
                    Log.d("SDM", "SimpleDownloadManager runned task " + l.hashCode() + "|" + l.releaseCode)
                }
            }
        }
    }
}