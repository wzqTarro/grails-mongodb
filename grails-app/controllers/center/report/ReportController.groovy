package center.report

import center.report.co.ReportConditionCO
import center.report.co.ReportCtrlLogCO
import center.report.common.ReportXmlConstant
import center.report.dto.QueryInputValueDTO
import center.report.dto.ReportParamValueDTO
import center.report.dto.ScreenReportDTO
import center.report.dto.TableParamDTO
import center.report.dto.XmlDataDTO
import center.report.result.BaseResult
import center.report.result.QueryInputValueResult
import center.report.result.Result
import center.report.vo.ReportDataVO
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.xml.MarkupBuilder
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

class ReportController {

    MessageSource messageSource

    DataService dataService

    static responseFormats = ['json', 'xml']
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * 显示报表详情
     * @param report
     * @return
     */
    def show(Report report) {
        BaseResult<Report> result = new BaseResult<>()
        result.one = report
        render result as JSON
    }

    /**
     * 保存
     * @param report
     * @return
     */
    def save(Report report) {
        if (!report) {
            render Result.error("报表信息不能为空") as JSON
            return
        }

        report.saveItem()
//        report.save(flush: true)
        if (report.hasErrors()) {
            def errorMsg = hasError(report)
            render Result.error(errorMsg) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 删除
     * @param report
     * @return
     */
    def delete(Report report) {
        if (!report) {
            render Result.error("报表信息不能为空") as JSON
            return
        }
        report.delete(flush: true)
        render Result.success() as JSON
    }

    /**
     * 根据条件查询
     * @return
     */
    def getByCondition(ReportConditionCO reportConditionCO) {
        BaseResult<Report> result = new BaseResult<>()
        result.list = Report.getByCondition(reportConditionCO.grpCode, reportConditionCO.cloudId, reportConditionCO.rptName,
            reportConditionCO.ctrlStatus, reportConditionCO.pageNow, reportConditionCO.pageSize)
        result.total = Report.countByCondition(reportConditionCO.grpCode, reportConditionCO.cloudId, reportConditionCO.rptName,
                reportConditionCO.ctrlStatus)
        render result as JSON
    }

    /**
     * 提交
     */
    def submit(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.submit(reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 停用
     * @param id
     * @param adscript
     */
    def stop(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.stop(reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 审核通过
     * @param id
     * @param adscript
     */
    def auditSuccess(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.auditSuccess(reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 审核未通过
     * @param id
     * @param adscript
     */
    def auditRollback(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.auditRollback(reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 重启用
     * @param id
     * @param adscript
     */
    @Transactional
    def reboot(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.reboot(reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 将区域报表转为中心报表
     * @param id
     * @param adscript
     */
    def toCenterReport(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.toCenterReport(reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 取消中心共享
     * @param id
     * @param adscript
     */
    def cancelCenterReport(ReportCtrlLogCO reportCtrlLogCO) {
        if (!reportCtrlLogCO) {
            render Result.error("报表不存在") as JSON
            return
        }
        Report report = Report.get(reportCtrlLogCO.reportId)
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        report.cancelCenterReport(reportCtrlLogCO.cloudId, reportCtrlLogCO.adscript, reportCtrlLogCO.accountId, reportCtrlLogCO.accountName)
        if (report.hasErrors()) {
            render Result.error(hasError(report)) as JSON
            return
        }
        render Result.success() as JSON
    }

    /**
     * 获取指定报表的数据
     * @param reportParam
     */
    def getReportData(Report report, ReportViewParamDTO reportParam) {
        BaseResult<String> result = new BaseResult<>()
        if (!report) {
            render Result.error("报表不存在") as JSON
            return
        }
        XmlDataDTO xmlDataDTO = dataService.getTableData(report, reportParam?.paramValues)
        String xml = toXmlData(xmlDataDTO)
        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml
        result.one = reportDataVO
        render result as JSON
    }

    /**
     * 获取指定大屏的所有数据表的结构
     * @param reportId
     */
    def getScreenReportStruct(Report report) {
        BaseResult<ReportDataVO> result = new BaseResult<>()
        if (!report) {
            result.setError("报表不存在")
            render result as JSON
            return
        }

        // 获取报表xml文本填充数据
        XmlDataDTO xmlDataDTO = dataService.getTableData(report, null)
        String xml = toStructData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml
        result.one = reportDataVO
        render result as JSON
    }

    /**
     * 获取大屏指定表数据
     * @param screenReportParam
     */
    def getScreenReportData(Report report, ScreenReportDTO screenReport) {
        BaseResult<ReportDataVO> result = new BaseResult<>()
        if (!screenReport) {
            result.setError("参数为空")
            render result as JSON
            return
        }
        // 筛选数据表
        report.screenTable(screenReport.tableNames)
        if (report.hasErrors()) {
            result.setError(hasError(report))
            render result as JSON
            return
        }

        XmlDataDTO xmlDataDTO = dataService.getTableData(report, screenReport.paramValues)
        String xml = toXmlData(xmlDataDTO)

        ReportDataVO reportDataVO = new ReportDataVO()
        reportDataVO.xmlData = xml
        result.one = reportDataVO
        render result as JSON
    }

    /**
     * 获取动态查询参数的参数值信息
     * @param reportId
     * @param name
     */
    def getReportQueryInputValue(Report report, String inputName) {
        QueryInputValueResult result = new QueryInputValueResult()
        if (!report) {
            result.setError("报表不存在")
            render result as JSON
            return
        }
        if (!inputName) {
            result.setError("输入参数不能为空")
            render result as JSON
            return
        }
        QueryInputValueDTO queryInputValueDTO = dataService.getInputData(report, inputName)
        if (report.hasErrors()) {
            result.setError(hasError(report))
            return
        }
        result.defValue = queryInputValueDTO?.defValue
        result.list = queryInputValueDTO?.inputValueDTOS
        render result as JSON
    }

    /**
     * 生成xml格式报表显示数据
     * @param xmlDataDTO
     * @return
     */
    private String toXmlData(XmlDataDTO xmlDataDTO) {
        if (!xmlDataDTO) {
            return null
        }
        List<TableParamDTO> tableParamDTOList = xmlDataDTO.tableParamDTOList
        List<ReportParamValueDTO> paramValueList = xmlDataDTO.paramValueList
        if (tableParamDTOList && paramValueList) {
            StringWriter out = new StringWriter()
            def xml = new MarkupBuilder(out)
            xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
            xml."${ReportXmlConstant.XML_ROOTNODE}"(){
                "${ReportXmlConstant.XML_PARAMETER}"(){
                    paramValueList.each { param ->
                        "${ReportXmlConstant.XML_PARAMETER_RECORD}"() {
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_NAME}"(param.name)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_VALUE}"(param.value)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_TITLE}"(param.title)
                        }
                    }

                }
                tableParamDTOList.each{ TableParamDTO table ->
                    "${ReportXmlConstant.XML_TABLE}"("${ReportXmlConstant.XML_TABLE_NAME}": table.reportTables.name, "${ReportXmlConstant.XML_TABLE_SEQNUM}": table.reportTables.seqNum){
                        table.rowList.each { row ->
                            "${ReportXmlConstant.XML_TABLE_RECORD}"() {
                                row.each { name, value ->
                                    String key = CommonUtil.toHumpStr(name)
                                    "${key}" value
                                }
                            }
                        }
                    }
                }

            }
            return out.toString()
        }
        return null
    }
    /**
     * 转换为大屏所用的xml格式数据
     * @param xmlDataDTO
     * @return
     */
    private String toStructData(XmlDataDTO xmlDataDTO) {
        if (!xmlDataDTO) {
            return null
        }
        List<TableParamDTO> tableParamDTOList = xmlDataDTO.tableParamDTOList
        List<ReportParamValueDTO> paramValueList = xmlDataDTO.paramValueList
        if (tableParamDTOList && paramValueList) {
            StringWriter out = new StringWriter()
            def xml = new MarkupBuilder(out)
            xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
            xml."${ReportXmlConstant.XML_ROOTNODE}"(){
                "${ReportXmlConstant.XML_PARAMETER}"(){
                    paramValueList.each { param ->
                        "${ReportXmlConstant.XML_PARAMETER_RECORD}"() {
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_NAME}"(param.name)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_VALUE}"(param.value)
                            "${ReportXmlConstant.XML_PARAMETER_RECORD_TITLE}"(param.title)
                        }
                    }

                }
                tableParamDTOList.each{ TableParamDTO table ->
                    "${ReportXmlConstant.XML_TABLE}"(){
                        "${ReportXmlConstant.XML_TABLE_NAME}"(table.reportTables.name)
                        "${ReportXmlConstant.XML_TABLE_INPUT_LIST}"() {
                            table.paramNameSet.each { name ->
                                "${ReportXmlConstant.XML_PARAMETER_RECORD_NAME}"(name)
                            }
                        }
                        "${ReportXmlConstant.XML_TABLE_COLUMNS}"() {
                            table.columnNameList.each { name ->
                                "${ReportXmlConstant.XML_TABLE_COLUMN}"(name)
                            }
                        }

                    }
                }
            }
            return out.toString()
        }
        return null
    }
}
