package center.report.enst

/**
 * 查询参数数据类型
 */
enum ParamDataTypeEnum {
    TYPE_STRING(11, "字符串"),

    TYPE_NUMBER(12, "数字"),

    TYPE_DATE_YEAR(21, "年度"),

    TYPE_DATE_MONTH(22, "月份"),

    TYPE_DATE_DAY(23, "日期"),

    TYPE_ORGANIZE(31, "机构"),

    TYPE_DEPARTMENT(32, "科室"),

    TYPE_DOCTOR(33, "医生团队");

    Integer code

    String name

    ParamDataTypeEnum(Integer code, String name) {
        this.code = code;
        this.setName(name);
    }

    static ParamDataTypeEnum getEnumByDataType(Integer dataType) {
        for (ParamDataTypeEnum e : values()) {
            if (e.code == dataType) {
                return e
            }
        }
        return null
    }
}