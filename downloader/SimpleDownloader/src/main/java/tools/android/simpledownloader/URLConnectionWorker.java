package tools.android.simpledownloader;

import android.compact.utils.FileCompactUtil;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLConnectionWorker implements DownloadWorker, FileDownloadImpl {
    protected boolean debug = DownloadAdapter.Companion.getDebug();
    protected String url, dir, key;
    protected int requsetTimes = 0;
    protected int bufferSize = 4096;

    protected final int READ_TIME_OUT = 10 * 60 * 1000; // millis
    protected final int CONNECT_TIME_OUT = 20 * 1000; // millis

    public URLConnectionWorker(String url, String dir, String key, String suffix, String md5) {
        this.url = url;
        this.dir = dir;
        this.key = key;

        this.suffix = suffix;
        this.md5 = md5;
        if (debug) {
            Log.d("FILEDOWNLOAD", "1|url|" + url + "|dir|" + dir + "|key|" + key + "|suffix|" + suffix + "|md5|" + md5);
        }
    }

    protected String suffix, md5;

    @Override
    public String getFileName() {
        return suffix == null ? key : key + "." + suffix;
    }

    @Override
    public void run() {
        notifyDownloadStart(url);
        boolean success = false;
        String savePath = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        while (requsetTimes >= 0 && requsetTimes < 3) {
            long totalSize = -1;
            if (Patterns.WEB_URL.matcher(url).matches()) {
                totalSize = getDownloadFileSize(url);
                if (totalSize < 0) {
                    requsetTimes++;
                    notifyDownloadFailure(url, "totalSize < 0");
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
            String path = isFileExist(dir, getFileName(), totalSize, md5, "check exist");
            //如果有此文件返回
            if (!TextUtils.isEmpty(path)) {
                Log.d("FILEDOWNLOAD", "file already exist --- " + key + ", " + path);
                notifyDownloadSuccess(url, path);
                notifyDownloadClear(true, url, path);
                return;
            }
            long completeSize = getlocalCacheFileSize(dir, getFileName());
            if (completeSize > totalSize) {
                requsetTimes++;
                notifyDownloadFailure(url, "completeSize > totalSize, completeSize=" + completeSize + ", totalSize=" + totalSize);
                try {
                    Thread.sleep(1000L);
                    new File(dir, getFileName()).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            } else if (completeSize == totalSize && completeSize > 0) {
                path = isFileExist(dir, getFileName(), totalSize, md5, "check completed local cache");
                if (!TextUtils.isEmpty(path)) {
                    notifyDownloadSuccess(url, path);
                    notifyDownloadClear(true, url, path);
                    return;
                } else {
                    requsetTimes++;
                    notifyDownloadFailure(url, "completeSize == totalSize, completeSize=" + completeSize + ", totalSize=" + totalSize + ", but verify fail");
                    try {
                        Thread.sleep(1000L);
                        new File(dir, getFileName()).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
            try {
                URL downloadUrl = new URL(url);
                conn = (HttpURLConnection) downloadUrl.openConnection();
//                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(CONNECT_TIME_OUT);
                conn.setReadTimeout(READ_TIME_OUT);
                String range = "bytes=" + completeSize + "-" + (totalSize - 1);
                conn.setRequestProperty("Range", range);
                int responseCode = conn.getResponseCode();
                if (responseCode == 301 || responseCode == 302) {
//                    String warnning = " Illegal ResponseCode " + responseCode + " is it a hijack?";
//                    Log.d("URLC", url + warnning);
//                    ImageDownloadManager.getInstance().notifyDownloadFailure(url, warnning);
//                    throw new IllegalStateException(url + warnning);
                }
                is = conn.getInputStream();
                File target = new File(dir, getFileName());
                randomAccessFile = new RandomAccessFile(target.getAbsolutePath(), "rwd");
                randomAccessFile.seek(completeSize);
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                int origin_percent = 0;
                while ((length = is.read(buffer)) != -1) {
                    randomAccessFile.write(buffer, 0, length);
                    completeSize += length;
                    int progress = (int) (((double) (completeSize) / totalSize) * 100);
                    if (progress - origin_percent >= 1) {
                        origin_percent = progress;
                        notifyDownloadProgress(url, Math.max(0, Math.min(progress, 99)));
                    }
                }
                requsetTimes = -1;
                success = true;
                path = isFileExist(dir, getFileName(), totalSize, md5, "check new download");
                if (!TextUtils.isEmpty(path)) {
                    notifyDownloadSuccess(url, path);
                    notifyDownloadClear(true, url, path);
                    return;
                } else {
                    notifyDownloadFailure(url, "download complete but verify fail, should not happen");
                }
            } catch (Exception e) {
                e.printStackTrace();
                notifyDownloadFailure(url, "Exception:" + e.getMessage() + "|dir|" + dir + "|filename|" + getFileName());
                if (requsetTimes >= 2) {
                    notifyDownloadFailure(url, "requestTimes >= 2");
                }
            } finally {
                requsetTimes++;
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d("FILEDOWNLOAD", "requsetTimes enough " + url + ", " + savePath + ", " + success);
        notifyDownloadClear(success, url, savePath);
    }

    @Override
    public long getDownloadFileSize(String inputUrl) {
        HttpURLConnection conn = null;
        long size = -1;
        try {
            URL url = new URL(inputUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIME_OUT);
            size = conn.getContentLength();
        } catch (Exception e) {
            e.printStackTrace();
            size = -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return size;
    }

    @Override
    public String isFileExist(String dir, String fileName, long targetSize) {
        File file = new File(dir, fileName);
        if (file != null && file.exists()) {
            long fileSize = file.length();
            if (targetSize == fileSize) {
                return file.getPath();
            }
        }
        return null;
    }

    protected String isFileExist(String dir, String fileName, long targetSize, String md5, String desc) {
        Log.d("FILEDOWNLOAD", "isFileExist(" + desc + ")|" + fileName + "|" + targetSize + "|" + md5);
        if (targetSize > 0 && (md5 == null || md5.length() == 0)) {
            // 大小合法但是md5缺失，只检查大小
            return isFileExist(dir, fileName, targetSize);
        } else if (targetSize > 0 && md5 != null && md5.length() > 0) {
            // 检查md5
            File file = new File(dir, fileName);
            if (file != null) {
                Log.d("FILEDOWNLOAD", fileName + " current length " + file.length());
            }
            boolean validate = FileCompactUtil.verifyBinaryFile(file.getAbsolutePath(), md5);
            Log.d("FILEDOWNLOAD", "verifyBinaryFile - " + validate);
            if (validate) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public long getlocalCacheFileSize(String dir, String fileName) {
        long fileSize = 0;
        File dF = new File(dir, fileName);
        if (dF.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dF);
                fileSize = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
                fileSize = 0;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return fileSize;
    }

    @Override
    public void notifyDownloadFailure(String url, String message) {
        SimpleDownloadManager.Companion.get().notifyDownloadFailure(url, message);
    }

    @Override
    public void notifyDownloadStart(String url) {
        SimpleDownloadManager.Companion.get().notifyDownloadStart(url);
    }

    @Override
    public void notifyDownloadProgress(String url, int progress) {
        SimpleDownloadManager.Companion.get().notifyDownloadProgress(url, progress);
    }

    @Override
    public void notifyDownloadSuccess(String url, String path) {
        SimpleDownloadManager.Companion.get().notifyDownloadSuccess(url, path);
    }

    @Override
    public void notifyDownloadClear(boolean success, String url, String path) {
        SimpleDownloadManager.Companion.get().notifyDownloadClear(success, url, path);
    }
}