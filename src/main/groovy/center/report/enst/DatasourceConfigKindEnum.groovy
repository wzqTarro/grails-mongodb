package center.report.enst

enum DatasourceConfigKindEnum {
    CENTER(1, "中心数据源"),
    REGION(2, "区域数据源"),
    SPECIALLY(3, "特定的区域数据源"),
    DATA_CENTER_VIRTUAL_TABLE(4, "数据中心虚表"),
    DATA_CENTER_REAL_TABLE(5, "数据中心实表")
    ;
    Integer kind
    String msg

    DatasourceConfigKindEnum(kind, msg) {
        this.kind = kind
        this.msg = msg
    }
    static DatasourceConfigKindEnum getEnumByKind(Integer kind) {
        for (DatasourceConfigKindEnum e: values()) {
            if (e.kind == kind) {
                return e
            }
        }
        return null
    }
}
