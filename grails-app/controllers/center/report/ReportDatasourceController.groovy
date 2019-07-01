package center.report

import center.report.result.BaseResult
import center.report.result.Result
import grails.converters.JSON
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

class ReportDatasourceController {

//    @Autowired
    MessageSource messageSource

    static responseFormats = ['json', 'xml']
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 根据主键查询详情
     * @param datasource
     * @return
     */
    def show(ReportDatasource datasource) {
        BaseResult<ReportDatasource> result = new BaseResult<>()
        if (!datasource) {
            def errorMsg = hasError(datasource)
            result.setError(errorMsg)
            render result as JSON
            return
        }
        result.one = datasource
        render result as JSON
    }

    /**
     * 添加
     * @param datasource
     * @return
     */
    def save(ReportDatasource datasource) {
        BaseResult<ReportDatasource> result = new BaseResult<>()
        if (!datasource) {
            result.setError("报表信息不能为空")
            render result as JSON
            return
        }
        if (datasource.hasErrors()) {
            def errorMsg = hasError(datasource)
            result.setError(errorMsg)
            render result as JSON
            return
        }
        datasource.saveItem()
        if (datasource.hasErrors()) {
            datasource.errors.allErrors.each {
                println "controller " + messageSource.getMessage(it, request.locale)
            }
            def errorMsg = hasError(datasource)
            result.setError(errorMsg)
            render result as JSON
            return
        }
        result.one = datasource
        render result as JSON
    }

    /**
     * 删除
     * @param datasource
     * @return
     */
    def delete(ReportDatasource datasource) {
        if (!datasource) {
            render Result.error("数据源不能为空") as JSON
            return
        }
        datasource.deleteItem()
        if (datasource.hasErrors()) {
            render Result.error(hasError(datasource)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 查询
     */
    def getByCondition() {
        BaseResult<ReportDatasource> result = new BaseResult<>()
        result.list = ReportDatasource.findAll()
        render result as JSON
    }
}
