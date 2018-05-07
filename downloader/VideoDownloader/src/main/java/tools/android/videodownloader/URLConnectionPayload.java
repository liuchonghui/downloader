package tools.android.videodownloader;

public class URLConnectionPayload {
    public String url, dir, key;
    public VideoDefinition definition;
    public boolean jumpTheQueue = false; // 是否插队

    public URLConnectionPayload(String url, String dir, String key) {
        this.url = url;
        this.dir = dir;
        this.key = key;
    }

    public URLConnectionPayload(String url, String dir, String key, VideoDefinition definition) {
        this.url = url;
        this.dir = dir;
        this.key = key;
        this.definition = definition;
    }

    public URLConnectionPayload(String url, String dir, String key, VideoDefinition definition, boolean jumpTheQueue) {
        this.url = url;
        this.dir = dir;
        this.key = key;
        this.definition = definition;
        this.jumpTheQueue = jumpTheQueue;
    }
}
