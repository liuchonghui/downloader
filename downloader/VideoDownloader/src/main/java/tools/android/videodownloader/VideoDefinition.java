package tools.android.videodownloader;

public enum VideoDefinition {
    DEFINITION_SUPER(58, "super"),
    DEFINITION_HIGH(54, "high"),
    DEFINITION_NORMAL(52, "normal"),
    DEFINITION_LOW(50, "low");

    public int code;
    public String desc;

    VideoDefinition(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int code() {
        return this.code;
    }

    public String desc() {
        return this.desc;
    }

    public static VideoDefinition ordinal(int o) {
        for (VideoDefinition vd : VideoDefinition.values()) {
            if (vd.ordinal() == o) {
                return vd;
            }
        }
        return null;
    }

    public static VideoDefinition parse(int code) {
        for (VideoDefinition vd : VideoDefinition.values()) {
            if (vd.code == code) {
                return vd;
            }
        }
        return null;
    }

    public static VideoDefinition parse(String desc) {
        for (VideoDefinition vd : VideoDefinition.values()) {
            if (vd.desc.equals(desc)) {
                return vd;
            }
        }
        return null;
    }
}
