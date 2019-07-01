package center.report

/**
 * 报表组别
 */
class ReportGroups {
    // 编码
    String code
    // 名称
    String name
    // 备注
    String comment

    static constraints = {
        code(unique: true, blank: false)
        name(unique: true, blank: false)
        comment(nullable: true)
    }

    static mapping = {
        // 关闭缓存
        stateless true
    }

    /**
     * 根据条件查询列表
     * @param params
     * @return
     */
    static List<ReportGroups> getListByCondition(params) {
        def code = params."code"
        def name = params."name"
        return ReportGroups.createCriteria().list {
            and {
                if (code){
                    eq("code", code)
                }
                if (name) {
                    eq("name", name)
                }
            }
            order "code", "asc"
        }
    }
}
