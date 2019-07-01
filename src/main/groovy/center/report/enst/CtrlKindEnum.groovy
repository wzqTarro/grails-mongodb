package center.report.enst

/**
 * 管控性质枚举类
 */
enum CtrlKindEnum {
    SUBMIT(1, "提交"),
    AUDIT(2, "审核"),
    STOP_USING(3, "停用"),
    REBOOT(4, "重启用"),
    TO_CENTER(5, "转中心共享"),
    CANCEL_CENTER(6, "取消中心共享")
    ;
    String kind
    String msg
    CtrlKindEnum(kind, msg) {
        this.kind = kind
        this.msg = msg
    }
    static CtrlKindEnum getEnumByKind(kind) {
        for (CtrlKindEnum e: values()) {
            if (e.kind == kind) {
                return e
            }
        }
        return null
    }
}