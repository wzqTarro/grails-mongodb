package center.report.enst

enum ReportLinkStatusEnum {
    MAINTAIN(0, "维护状态"),
    NORMAL(1, "正常使用")
    ;
    Integer code
    String msg
    private ReportLinkStatusEnum(code, msg) {
        this.code = code
        this.msg = msg
    }
    static ReportLinkStatusEnum getEnumByCode(code) {
        for (CtrlStatusEnum e: values()) {
            if (e.code == code) {
                return e
            }
        }
        return null
    }
}