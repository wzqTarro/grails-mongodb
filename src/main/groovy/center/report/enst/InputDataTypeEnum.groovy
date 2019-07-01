package center.report.enst

/**
 * 输入参数数据类型
 */
enum InputDataTypeEnum {
    TYPE_TXT("11", "文本"),

    TYPE_NUM("12", "数字"),

    TYPE_YEAR("21", "年度"),

    TYPE_MONTH("22", "月份"),

    TYPE_DAY("23", "日期"),

    TYPE_ORG("31", "机构"),

    TYPE_DEPAMENT("32", "科室"),

    TYPE_TEAM("33", "医生团队");

    String name

    String title

    private InputDataTypeEnum(String name, String title) {
        this.name = name
        this.title = title
    }

    static InputDataTypeEnum getEnumByDataType(String dataType) {
        for (InputDataTypeEnum e : values()) {
            if (e.name == dataType) {
                return e
            }
        }
        return null
    }
}