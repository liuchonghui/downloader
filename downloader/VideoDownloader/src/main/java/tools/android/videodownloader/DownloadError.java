package tools.android.videodownloader;

public enum DownloadError {
    UNKNOWN, // 预设ordinal=0为未知，防止ordinal=0干扰正常判断
    CONNECTION_ERROR, // 各种设备当前网络原因（属于ErrorForNetwork）（ConnectTimeoutException／SocketTimeoutException／SocketException）
    INTERRUPT_IGNORE_REASON, // 任务被interrupt，不用在乎interrupt原因
    INTERRUPT_FOR_MANUAL, // 任务被interrupt，原因是用户手动暂停
    INTERRUPT_FOR_MOBILE, // 任务被interrupt，原因是切换至数据网络（属于ErrorForNetwork）
    TOTAL_SIZE_ILLEGAL, // 向服务器端取目标长度时出现错误
    COMPLETE_SIZE_ILLEGAL, // 完成下载后，实际文件的长度出现错误
    FILE_REPLACE_FAIL, // 临时文件切换为实际文件时发生错误
    FILE_VERIFY_FAIL, // 验证下载完文件时发生错误
    SERVER_ERROR, // 服务端错误（属于ErrorForNetwork）（FileNotFoundException服务器找不到文件）
    OTHER_ERROR, // 其他错误（下载任务中的其他Exception，可能其中也有属于网络原因，需要继续确认）

    // 注意：ordinal被写入数据表中，所以此后的状态添加，必须按照当前顺序，不能打乱！

    INTERRUPT_FOR_REMOVE_DATA, // 任务被interrupt，原因是数据被删除
    INTERRUPT_FOR_JUMP_THE_QUEUE, // 任务被interrupt，原因是被其他任务插队
    PROCESS_KILLED_ERROR; // 进程被强杀，导致运行时任务中断，但是状态仍旧为"下载中"，启动初始化被查到时再纠错

    public static boolean errorForInterrupt(DownloadError error) {
        return DownloadError.INTERRUPT_FOR_MOBILE == error
                || DownloadError.INTERRUPT_IGNORE_REASON == error
                || DownloadError.INTERRUPT_FOR_JUMP_THE_QUEUE == error;
    }

    public static boolean errorForProcessKilled(DownloadError error) {
        return DownloadError.PROCESS_KILLED_ERROR == error;
    }

    public static DownloadError parse(int ordinal) {
        for (DownloadError error: DownloadError.values()) {
            if (ordinal == error.ordinal()) {
                return error;
            }
        }
        return DownloadError.UNKNOWN;
    }

    public static DownloadError parse(InterruptReason interruptReason) {
        DownloadError error = DownloadError.INTERRUPT_IGNORE_REASON;
        if (InterruptReason.CONVERT_TO_MOBILE == interruptReason) {
            error = DownloadError.INTERRUPT_FOR_MOBILE;
        } else if (InterruptReason.MANUAL_OPERATE == interruptReason) {
            error = DownloadError.INTERRUPT_FOR_MANUAL;
        } else if (InterruptReason.REMOVE_DATA == interruptReason) {
            error = DownloadError.INTERRUPT_FOR_REMOVE_DATA;
        } else if (InterruptReason.SOME_ONE_JUMP_THE_QUEUE == interruptReason) {
            error = DownloadError.INTERRUPT_FOR_JUMP_THE_QUEUE;
        }
        return error;
    }
}
