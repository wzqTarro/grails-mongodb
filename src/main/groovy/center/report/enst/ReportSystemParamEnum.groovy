package center.report.enst

enum ReportSystemParamEnum {
    CURRENT_USER("my_staff_id", "我的员工Id"),

    CURRENT_ORG("my_org_id", "我的机构Id"),

    CURRENT_DEPART("my_dept_id", "我的科室Id"),

    CURRENT_TEAM("my_team_id", "我的团队Id");

    String name
    String title

    private ReportSystemParamEnum(String name, String title) {
        this.name = name
        this.title = title
    }

    static ReportSystemParamEnum getEnumByName(String name) {
        for (ReportSystemParamEnum  e : values()) {
            if (e.name == name) {
                return e
            }
        }
        return null
    }
}