package center.report


import center.report.enst.CtrlStatusEnum
import center.report.enst.ReportLinkStatusEnum
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import grails.validation.Validateable

/**
 * 报表模块
 */
class ReportModule {
    // 模块ID
    String moduleId
    // 区域云Id
    String cloudId
    // 区域云名称
    String cloudName
    // 关联报表
    List<ReportLink> reportLinkList
    // 报表冗余数据
    Map<String, ReportData> reportDataMap
    static embedded = ['reportLinkList', 'reportDataMap']
    static constraints = {
        moduleId(blank: false)
        cloudId(nullable: true, unique: 'moduleId')
        cloudName(nullable: true, unique: 'moduleId')
        reportDataMap(nullable: true)
        reportLinkList(nullable: true)
        reportLinkList validator: { List<ReportLink> vals, ReportModule obj, errors ->
            for (ReportLink reportLink : vals) {
                Long rptId = reportLink.rptId
                if (!rptId) {
                    errors.rejectValue("reportLinkList", "error", "链接报表标识不能为空")
                    return
                }
                if (!reportLink.showName) {
                    errors.rejectValue("reportLinkList", "error", "链接报表显示名称不能为空")
                    return
                }
                if (reportLink.status < 0) {
                    errors.rejectValue("reportLinkList", "error", "链接报表状态不能为空")
                    return
                }
                // 显示名称是否重复
                def actualReportLink = obj.reportLinkList.findAll { otherReportLink ->
                    otherReportLink.showName == reportLink.showName
                }
                if (actualReportLink.size() > 1) {
                    errors.rejectValue("reportLinkList", "error", "链接报表名称已存在")
                    return
                }
                // 挂接报表参数
                for (def linkInput : reportLink.linkInputList) {
                    if (!linkInput.name) {
                        errors.rejectValue("reportLinkList", "error", "挂接报表参数名称不能为空")
                        return
                    }
                    // 挂接报表参数名称是否唯一
                    def actualReportLinkInput = reportLink.linkInputList.findAll { otherLinkInput ->
                        otherLinkInput.name == linkInput.name
                    }
                    if (actualReportLinkInput.size() > 1) {
                        errors.rejectValue("reportLinkList", "error", "挂接报表参数名称已存在")
                        return
                    }
                }
                if (!reportLink.validate()) {
                    for (def err : reportLink.errors.allErrors) {
                        errors.rejectValue("reportLinkList", err.code, err.arguments, err.defaultMessage)
                        return
                    }
                }
            }

        }
    }
    static mapping = {
        stateless true
    }
    /**
     * 关联报表-禁止当做领域对象使用
     */
    static class ReportLink implements Validateable{
        // 报表ID
        Long rptId
        // 显示名称
        String showName
        // 说明
        String comment
        // 排列号
        Integer seqNum
        // 状态
        Integer status
        // 挂接报表参数
        List<ReportLinkInput> linkInputList
        static embedded = ['linkInputList']
        static constraints = {
            comment(nullable: true)
            seqNum(nullable: true)
            linkInputList(nullable: true)
        }
    }

    /**
     * 关联报表参数-禁止当做领域对象使用
     */
    static class ReportLinkInput implements Validateable {
        // 参数名称
        String name
        // 赋值方式
        Integer valLet
        // 设值公式
        String formula
        // 设定值
        String setValue

        static constraints = {
            valLet(nullable: true)
            formula(nullable: true)
            setValue(nullable: true)
        }
    }

    /**
     * 报表冗余数据
     */
    static class ReportData implements Validateable {
        // 报表ID
        Long rptId
        // 报表名称
        String name
        // 报表编码
        String code
        Map<String, ReportInputData> reportInputDataMap
        static embedded = ['reportInputDataMap']
    }

    /**
     * 报表输入参数冗余数据
     */
    static class ReportInputData implements Validateable {
        // 名称
        String name
        // 参数标签
        String caption
        // 排列号
        Integer seqNum
        // 数据类型
        String dataType

        static constraints = {
            seqNum(nullable: true)
        }
    }

    void saveItem() {
        if (!this.cloudId) {
            this.cloudId = ""
        }
        if (this.reportLinkList) {
            this.reportDataMap = [:]
            // 挂接报表参数
            for (ReportLink reportLink : this.reportLinkList) {
                if (!this.id) {
                    reportLink.status = 0
                }

                reportLink.linkInputList = []

                String rptId = reportLink.rptId
                if (!rptId) {
                    this.errors.rejectValue("reportLinkList", "error", "链接报表ID不能为空")
                    return
                }
                Report report = Report.get(rptId)
                if (!report) {
                    this.errors.rejectValue("reportLinkList", "error", "报表不存在")
                    return
                }
                def reportInputMap = [:]
                for (def input : report.inputList) {
                    reportInputMap.put(input.name, new ReportInputData(name: input.name, caption: input.caption, seqNum: input.seqNum, dataType: input.dataType))
                    reportLink.linkInputList.add(new ReportLinkInput(name: input.name))
                }
                this.reportDataMap.put(report.id.toString(), new ReportData(rptId: report.id, name: report.name, code: report.code, reportInputDataMap: reportInputMap))
            }
//            if (this.id) {
//                ReportModule.collection.updateMany(
//                        Filters.eq("_id", this.id),
//                        Updates.combine()
//                )
//            }
        }
        if (this.validate()) {
            this.save(flush: true)
        }
    }

    /**
     * 保存链接报表
     * @param reportLink
     */
    void saveReportLink(ReportLink reportLink) {
        if (reportLink) {
            if (!this.reportLinkList) {
                this.reportLinkList = []
            }
            def existsReportLink = this.reportLinkList.find {
                it.showName == reportLink.showName
            }
            if (!existsReportLink) {
                reportLink.status = ReportLinkStatusEnum.MAINTAIN.code
            } else {
                // 正常使用中链接报表无法修改
                if (ReportLinkStatusEnum.NORMAL.code == existsReportLink.status) {
                    this.errors.rejectValue("reportLinkList", "error", "使用状态下挂接报表无法修改")
                    return
                }
                this.reportLinkList.remove(existsReportLink)
                reportLink.status = ReportLinkStatusEnum.MAINTAIN.code
            }
            def rptId = reportLink.rptId
            if (!rptId) {
                this.errors.rejectValue("reportLinkList", "error", "挂接报表ID不能为空")
                return
            }
            Report report = Report.get(rptId)
            if (!report) {
                this.errors.rejectValue("reportLinkList", "error", "报表不存在")
                return
            }
            // 审核通过的报表才可以关联
            if (report.ctrlStatus != CtrlStatusEnum.AUDIT_SUCCESS.code) {
                errors.rejectValue("reportLinkList", "error", "报表未审核通过")
                return
            }
            // 报表冗余数据赋值
            def assignReportInputMap = {
                def reportInputMap = [:]
                reportLink.linkInputList = []
                for (def input : report.inputList) {
                    reportInputMap.put(input.name, new ReportInputData(name: input.name, caption: input.caption, seqNum: input.seqNum, dataType: input.dataType))
                    reportLink.linkInputList.add(new ReportLinkInput(name: input.name))
                }
                this.reportDataMap.put(report.id.toString(), new ReportData(rptId: report.id, name: report.name, code: report.code, reportInputDataMap: reportInputMap))
            }
            if (this.reportDataMap) {
                if (!this.reportDataMap.get(report.id.toString())) {
                    assignReportInputMap()
                }
            }else {
                this.reportDataMap = [:]
                assignReportInputMap()
            }
            this.reportLinkList.add(reportLink)
        }
        if (this.validate()) {
            this.reportLinkList.sort{ a, b ->
                if (a.seqNum == null) {
                    a.seqNum = 0
                }
                if (b.seqNum == null) {
                    b.seqNum = 0
                }
                return a.seqNum - b.seqNum
            }
            this.save(flush: true)
        }
    }

    /**
     * 删除链接报表
     * @param name
     */
    void deleteReportLink(showName) {
        if (!this.reportLinkList) {
            this.errors.rejectValue("reportLinkList", "error", "链接报表为空")
            return
        }
        // 要删除的报表
        ReportLink deleteReportLink = this.reportLinkList.find { ReportLink reportLink ->
            reportLink.showName == showName
        }
        if (ReportLinkStatusEnum.NORMAL.code == deleteReportLink.status) {
            this.errors.rejectValue("reportLinkList", "error", "使用状态下报表无法删除")
            return
        }
        this.reportLinkList.remove(deleteReportLink)
        this.save(flush: true)
    }

    /**
     * 启用链接报表
     * @param showName
     */
    void startUsingReportLink(showName) {
        if (!this.reportLinkList) {
            this.errors.rejectValue("reportLinkList", "error", "链接报表为空")
            return
        }
        this.reportLinkList.each {
            if (it.showName == showName) {
                it.status = ReportLinkStatusEnum.NORMAL.code
            }
        }
        this.save(flush: true)
    }

    /**
     * 根据模块ID查询
     * @param params
     * @return
     */
    static List<ReportModule> getByModuleId(moduleId) {
        if (!moduleId) {
            return null;
        }
        return ReportModule.createCriteria().list {
            eq("moduleId", moduleId)
        }
    }

    /**
     * 模块分发
     * @param cloudId
     * @param cloudName
     */
    void distributeModule(reportLinkList, reportDataMap) {
        this.reportLinkList = reportLinkList
        this.reportDataMap = reportDataMap
        if (this.validate()) {
            this.save(flush: true)
        }
    }

    /**
     * 更新关联报表冗余数据
     * @param rptId
     * @param name
     * @param code
     */
//    static void updateReportLink(Long rptId, String name, String code) {
//        ReportModule.collection.updateMany(
//                Filters.exists("reportDataMap.${rptId}"),
//                Updates.combine(
//                        Updates.set("reportDataMap.${rptId}.name", name),
//                        Updates.set("reportDataMap.${rptId}.code", code)
//                )
//        )
//    }

    /**
     * 更新关联输入参数冗余数据
     */
    static void updateReportLinkInput(Long rptId, List<Report.Input> inputList) {
        def map = [:]
        inputList.each {
            map.put(it.name, new ReportInputData(name: it.name, caption: it.caption, seqNum: it.seqNum, dataType: it.dataType))
        }
        ReportModule.collection.updateMany(
                Filters.exists("reportDataMap.${rptId}"),
                Updates.set("reportDataMap.${rptId}.reportInputDataMap", map)
        )
    }
}

