package center.report.co

import grails.validation.Validateable

class ReportConditionCO implements Validateable{
    // 分组编码
    String grpCode
    // 区域云ID
    String cloudId
    // 是否固化报表
//        Integer isFixed
    // 报表名称
    String rptName
    // 管理状态
    Integer ctrlStatus
    Integer pageNow
    Integer pageSize

    static constraints = {
        grpCode nullable: true
        cloudId nullable: true
        rptName nullable: true
        ctrlStatus nullable: true
        pageNow nullable: true
        pageSize nullable: true
    }
}
