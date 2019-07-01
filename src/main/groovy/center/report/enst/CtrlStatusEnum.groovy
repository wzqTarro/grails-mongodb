package center.report.enst

/**
 * 管理状态枚举值
 */
enum CtrlStatusEnum {
    BZZCY(0, "编制中草稿"),
    SUBMIT_AUDIT(1, "提请审核"),
    AUDIT_ERROR(-1, "审核退回"),
    AUDIT_SUCCESS(2, "审核通过"),
    STOP_USING(-2, "停止使用")
    ;
    Integer code
    String msg
    CtrlStatusEnum(code, msg) {
        this.code = code
        this.msg = msg
    }
    static CtrlStatusEnum getEnumByCode(code) {
        for (CtrlStatusEnum e: values()) {
            if (e.code == code) {
                return e
            }
        }
        return null
    }
}