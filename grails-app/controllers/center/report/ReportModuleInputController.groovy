package center.report

import center.report.result.BaseResult
import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

class ReportModuleInputController {

    MessageSource messageSource

    static responseFormats = ['json', 'xml']
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 保存
     * @param reportModule
     * @return
     */
    def save(ReportModuleInput reportModuleInput) {
        BaseResult<ReportModuleInput> result = new BaseResult<>()
        if (!reportModuleInput) {
            result.setError("模块不能为空") as JSON
            render result as JSON
            return
        }
        if (!reportModuleInput.validate()) {
            result.setError(hasError(reportModuleInput))
            render result as JSON
            return
        }
        reportModuleInput.save(flush: true)
        result.one = reportModuleInput
        render result as JSON
    }

    /**
     * 根据模块ID查询详情
     * @param id
     * @return
     */
    def show(id) {
        BaseResult<ReportModuleInput> result = new BaseResult<>()
        ReportModuleInput reportModuleInput = ReportModuleInput.getByModuleId(id)
        result.one = reportModuleInput
        render result as JSON
    }

    /**
     * 根据名称查询
     * @param name
     * @param code
     */
    def getByCondition(String name) {
        BaseResult<ReportModuleInput> result = new BaseResult<>()
        result.list = ReportModuleInput.getByName(name)
        render result as JSON
    }
}
