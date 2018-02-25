package tool.async2sync.activity;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Java原生的API可用于发送HTTP请求，即java.net.URL、java.net.URLConnection，这些API很好用、很常用，
 * 但不够简便；
 *
 * 1.通过统一资源定位器（java.net.URL）获取连接器（java.net.URLConnection） 2.设置请求的参数 3.发送请求
 * 4.以输入流的形式获取返回内容 5.关闭输入流
 *
 * @author H__D
 *
 */
public class HttpConnectionUtil {


    /**
     *
     * @param urlPath
     *            下载路径
     * @param downloadDir
     *            下载存放目录
     * @return 返回下载文件
     */
    public static File downloadFile(String urlPath, String downloadDir) {
        File file = null;
        try {
            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            // 设定请求的方法，默认是GET
//            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Connection", "close");
//            httpURLConnection.setDoOutput(true);
//
// 设置是否从httpUrlConnection读入，默认情况下是true;
//            httpURLConnection.setDoInput(true);

// Post 请求不能使用缓存
//            httpURLConnection.setUseCaches(false);

// 设定传送的内容类型是可序列化的java对象
// (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
//            httpURLConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
            // 设置字符编码
//            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();

            // 文件大小
            int fileLength = httpURLConnection.getContentLength();

            // 文件名
            String filePathUrl = httpURLConnection.getURL().getFile();
            String fileFullName = filePathUrl.substring(filePathUrl.lastIndexOf(File.separatorChar) + 1);

            Log.d("DDD", "file length---->" + fileLength);

            InputStream is = httpURLConnection.getInputStream();
            Log.d("DDD", "file inputstream---->" + is);
            BufferedInputStream bin = new BufferedInputStream(is);
            Log.d("DDD", "file bufferedinputstream---->" + bin);
            String path = downloadDir + File.separatorChar + fileFullName;
            Log.d("DDD", "file path---->" + path);
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                // 打印下载百分比
                 Log.d("DDD", "下载了-------> " + len * 100 / fileLength + "%\n");
            }
            bin.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DDD", "Exception|" + e.getMessage());
        } finally {
            return file;
        }

    }

    public static void main(String[] args) {

        // 下载文件测试
        downloadFile("http://localhost:8080/images/1467523487190.png", "/Users/H__D/Desktop");

    }

}
