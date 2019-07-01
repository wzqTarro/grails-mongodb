package center.report.co

import grails.validation.Validateable

class DistributeModuleCO implements Validateable {
    Long sourceModuleCloudId
    // 分发区域ID
    String cloudId
    // 分发区域名称
    String cloudName

    static constraints = {
        cloudId nullable: true
        cloudName nullable: true
    }
}
