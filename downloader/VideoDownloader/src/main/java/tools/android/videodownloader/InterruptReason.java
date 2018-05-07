package tools.android.videodownloader;

public enum InterruptReason {
    IGNORE, // 不用关心的原因
    MANUAL_OPERATE, // 用户手动暂停
    CONVERT_TO_MOBILE, // 切换成数据网络
    REMOVE_DATA, // 删除了一个数据，必须连带停止该任务（如果正在运行）
    SOME_ONE_JUMP_THE_QUEUE, // 出现插队现象，该任务被暂停
}
