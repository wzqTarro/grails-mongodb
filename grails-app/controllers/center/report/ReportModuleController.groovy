package center.report

import center.report.co.DistributeModuleCO
import center.report.co.QueryReportModuleCO
import center.report.co.ReportLinkCO
import center.report.co.UsingReportLinkCO
import center.report.result.BaseResult
import center.report.result.Result
import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

class ReportModuleController {

    MessageSource messageSource

    static responseFormats = ['json', 'xml']
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     *
     * 根据模块ID 查询
     * @param moduleId
     * @return
     */
    def show(String id){
        BaseResult<ReportModule> result = new BaseResult<>()
        List<ReportModule> reportModuleList = ReportModule.getByModuleId(id)
        result.list = reportModuleList
        render result as JSON
    }

    /**
     * 查询模块区域链接报表
     * @param queryReportModuleCO
     */
    def getByModuleIdAndCloudId(QueryReportModuleCO queryReportModuleCO) {
        BaseResult<ReportModule> result = new BaseResult<>()
        if (!queryReportModuleCO) {
            result.error = "参数不能为空"
            render result as JSON
            return
        }
        ReportModule reportModule = ReportModule.get(queryReportModuleCO.moduleCloudId)
        result.one = reportModule
        render result as JSON
    }

    /**
     * 保存链接报表
     * @param reportLinkCO
     */
    def saveReportLink(ReportLinkCO reportLinkCO) {
        if (!reportLinkCO) {
            render Result.error("链接报表不能为空") as JSON
            return
        }
        if (reportLinkCO.hasErrors()) {
            render Result.error(hasError(reportLinkCO)) as JSON
            return
        }
        Long id = reportLinkCO.id
        ReportModule reportModule = ReportModule.get(id)
        if (!reportModule) {
            render Result.error("模块不存在") as JSON
            return
        }
        reportModule.saveReportLink(reportLinkCO.reportLink)
        if (reportModule.hasErrors()) {
            render Result.error(hasError(reportModule)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 删除链接报表
     * @param id
     * @param showName
     */
    def deleteReportLink(UsingReportLinkCO usingReportLinkCO) {
        if (!usingReportLinkCO) {
            render Result.error("参数不能为空") as JSON
            return
        }
        if (usingReportLinkCO.hasErrors()) {
            render Result.error(hasError(usingReportLinkCO)) as JSON
            return
        }
        ReportModule reportModule = ReportModule.get(usingReportLinkCO.id)
        if (!reportModule) {
            render Result.error("区域模块不存在") as JSON
        }
        reportModule.deleteReportLink(usingReportLinkCO.showName)
        if (reportModule.hasErrors()) {
            render Result.error(hasError(reportModule)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 启用链接报表
     * @param reportModule
     */
    def startUsingReportLink(UsingReportLinkCO usingReportLinkCO) {
        if (!usingReportLinkCO) {
            render Result.error("参数不能为空") as JSON
            return
        }
        if (usingReportLinkCO.hasErrors()) {
            render Result.error(hasError(usingReportLinkCO)) as JSON
            return
        }
        ReportModule reportModule = ReportModule.get(usingReportLinkCO.id)
        if (!reportModule) {
            render Result.error("链接报表不存在") as JSON
            return
        }
        // 启用
        reportModule.startUsingReportLink(usingReportLinkCO.showName)
        if (reportModule.hasErrors()) {
            render Result.error(hasError(reportModule)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 保存
     * @param reportModule
     * @return
     */
    def save(ReportModule reportModule) {
        BaseResult<ReportModule> result = new BaseResult<>()
        if (!reportModule) {
            result.setError("模块不能为空") as JSON
            render result as JSON
            return
        }
        reportModule.saveItem()
        if (reportModule.hasErrors()) {
            result.setError(hasError(reportModule))
            render result as JSON
            return
        }
        result.one = reportModule
        render result as JSON
    }

    /**
     * 删除
     * @param reportModule
     */
    def delete(ReportModule reportModule) {
        if (!reportModule) {
            render Result.error("模块不能为空") as JSON
            return
        }
        reportModule.delete(flush: true)
        render Result.success() as JSON
    }

    /**
     * 分发
     * @param moduleId
     * @param sourceCloudId
     * @param cloudId
     * @param cloudName
     */
    def distributeModule(DistributeModuleCO distributeModuleCO) {
        if (!distributeModuleCO) {
            render Result.error("参数不能为空") as JSON
            return
        }
        if (distributeModuleCO.hasErrors()) {
            render Result.error(hasError(distributeModuleCO)) as JSON
            return
        }
        ReportModule reportModule = ReportModule.get(distributeModuleCO.sourceModuleCloudId)
        if (!reportModule) {
            render Result.error("报表模块不存在") as JSON
            return
        }
        ReportModule distributeModule = new ReportModule(moduleId: reportModule.moduleId)
        if (!distributeModuleCO.cloudId) {
            distributeModule.cloudId = ""
        } else {
            distributeModule.cloudId = distributeModuleCO.cloudId
        }
        if (!distributeModuleCO.cloudName) {
            distributeModule.cloudName = ""
        } else {
            distributeModule.cloudName = distributeModuleCO.cloudName
        }
        distributeModule.distributeModule(reportModule.reportLinkList, reportModule.reportDataMap)
        if (distributeModule.hasErrors()) {
            render Result.error(hasError(distributeModule)) as JSON
            return
        }
        render Result.success() as JSON
    }
}
