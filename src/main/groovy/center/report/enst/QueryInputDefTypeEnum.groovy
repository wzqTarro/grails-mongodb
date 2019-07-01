package center.report.enst

enum QueryInputDefTypeEnum {
    /**
     * 所有机构
     */
    ORG_ALL("所有机构", "31"),
    /**
     * 我的机构
     */
    ORG_MY("我的机构", "31"),
    /**
     * 所有科室
     */
    DEPAMENT_ALL("所有科室", "32"),
    /**
     * 我的科室
     */
    DEPAMENT_MY("我的科室", "32"),
    /**
     * 所有团队
     */
    TEAM_ALL("所有团队", "33"),
    /**
     * 我的团队
     */
    TEAM_MY("我的团队", "33"),
    /**
     * 本年
     */
    YEAR_NOW("本年", "21"),
    /**
     * 上年
     */
    YEAR_LAST("上年", "21"),
    /**
     * 上月
     */
    MONTH_LAST("上月", "22"),
    /**
     * 本月
     */
    MONTH_NOW("本月", "22"),
    /**
     * 下月
     */
    MONTH_NEXT("下月", "22"),
    /**
     * 今天
     */
    DATE_NOW("今天", "23"),
    /**
     * 昨天
     */
    DATE_LAST("昨天", "23"),
    /**
     * 明天
     */
    DATE_NEXT("明天", "23"),
    /**
     * 倒七天
     */
    DATE_LAST_7("倒七天", "23"),
    /**
     * 进七天
     */
    DATE_NEXT_7("进七天", "23"),
    /**
     * 月初
     */
    DATE_MONTH_FIRST("月初", "23"),
    /**
     * 月末
     */
    DATE_MONTH_LAST("月末", "23"),
    /**
     * 上月今天
     */
    DATE_LAST_MONTH("上月今天", "23");

    String name

    String dataType

    QueryInputDefTypeEnum(String name, String dataType) {
        this.name = name
        this.dataType = dataType
    }

    static QueryInputDefTypeEnum getEnumByDefType(String defType) {
        for (QueryInputDefTypeEnum e : values()) {
            if (e.name == defType) {
                return e
            }
        }
        return null
    }
}