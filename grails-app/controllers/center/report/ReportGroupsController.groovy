package center.report

import center.report.result.BaseResult
import center.report.result.Result
import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

class ReportGroupsController {

    MessageSource messageSource

    static responseFormats = ['json', 'xml']
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def show(ReportGroups reportGroups) {
        BaseResult<ReportGroups> result = new BaseResult<>()
        result.one = reportGroups
        render result as JSON
    }

    /**
     * 根据名称或编码查询列表
     */
    def getListByCondition() {
        BaseResult<ReportGroups> result = new BaseResult()
        result.list = ReportGroups.getListByCondition(params)
        render result as JSON
    }

    /**
     * 保存分组
     * @param reportGroups
     * @return
     */
    def save(ReportGroups reportGroups) {
        if (!reportGroups) {
            render Result.error("分组信息不能为空") as JSON
            return
        }
        reportGroups.save(flush:true)
        if (reportGroups.hasErrors()) {
            def errorMsg = hasError(reportGroups)
            render Result.error(errorMsg) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 删除分组
     * @param reportGroups
     * @return
     */
    def delete(ReportGroups reportGroups) {
        if (!reportGroups) {
            render Result.error("分组不存在") as JSON
            return
        }
        reportGroups.delete(flush: true)
        render Result.success() as JSON
    }
}
