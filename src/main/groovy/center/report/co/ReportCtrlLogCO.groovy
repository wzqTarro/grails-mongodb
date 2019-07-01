package center.report.co

import grails.validation.Validateable

class ReportCtrlLogCO implements Validateable{
    // 报表ID
    Long reportId
    // 附加说明
    String adscript
    // 区域云
    String cloudId
    // 操作者ID
    String accountId
    // 账户名称
    String accountName

    static constraints  = {
        adscript nullable: true
        cloudId nullable: true
    }
}
