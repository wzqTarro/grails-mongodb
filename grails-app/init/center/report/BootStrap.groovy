package center.report

import center.report.enst.DatasourceConfigKindEnum
import center.report.enst.ReportLinkStatusEnum
import com.mongodb.client.model.Filters
import grails.converters.JSON
import org.bson.Document
import org.springframework.validation.FieldError

class BootStrap {

    def grailsApplication

//    def messageSource

    def init = { servletContext ->
        def hasError = { Object domain ->
            def errors = [], errorMessage = new StringBuilder()
            domain.errors.allErrors.collect(errors) { FieldError err ->
                messageSource.getMessage(err, request.locale)
            }
            errors.eachWithIndex { error, index ->
                errorMessage << "${error};"
            }
            errorMessage as String
        }

        grailsApplication.controllerClasses.each { controller ->
            controller.metaClass.hasError = hasError
        }

        JSON.registerObjectMarshaller(Date.class) {
            it.format("YYYY-MM-dd HH:mm:ss")
        }

        ReportDatasource datasource = new ReportDatasource(code: "01", name: "特定数据源", kind: DatasourceConfigKindEnum.SPECIALLY.kind, config: '{"cloudId":"123"}')
        datasource.save()

        ReportGroups group = new ReportGroups(code: "1", name: "name", comment: "comment")
        group.save()

        Report report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, ctrlStatus: 0, grpCode: group.code, cloudId: '1')

        List<Report.Input> inputList = new ArrayList<>()
        Report.Input input = new Report.Input(name: "org", caption: "ad", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<>()
        inputList.add(input)

        Report.Input input1 = new Report.Input(name: "test", caption: "test", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input1.datasourceDataCode = datasource.code
        inputList.add(input1)
        report.inputList = inputList

        report.datasourceDataMap = [(datasource.code):datasource.name]
        report.save()

        ReportModule module = new ReportModule(moduleId: "01", cloudId: "1", cloudName: "众康云区域", reportLinkList: [
                new ReportModule.ReportLink(rptId: report.id, showName: "科主任情况", comment: "关联报表说明", status: ReportLinkStatusEnum.MAINTAIN.code, linkInputList:[
                        new ReportModule.ReportLinkInput(name: input.name)
                ])
        ])
        def rptId = report.id.toString()
        module.reportDataMap = [
                (rptId): new ReportModule.ReportData(rptId: report.id, name: report.name, code: report.code,
                        reportInputDataMap: [
                                (input.name): new ReportModule.ReportInputData(name: input.name, caption: input.caption, seqNum: input.seqNum, dataType: input.dataType)
                        ])
        ]
        module.save()

        ReportModuleInput moduleInput = new ReportModuleInput(moduleId: "01", moduleName: "分科随访管理", slotKind: 1, comment: "说明", inputList: [
                new ReportModuleInput.ModuleInput(name: "cur_dept_id", caption: "当前部门ID", dataType: "S", comment: "模块参数说明")
        ])
        moduleInput.save()

        ReportModuleInput moduleInput1 = new ReportModuleInput(moduleId: "02", moduleName: "集中随访管理", slotKind: 1, comment: "说明",
                inputList: [
                        new ReportModuleInput.ModuleInput(name: "cur_dept_id", caption: "当前部门ID", dataType: "S", comment: "模块参数说明")
                ]
        )
        moduleInput1.save(flush: true)

/*         List<Table> tableList = new ArrayList<>()
        Table table = new Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1)
        table.datasourceDataCode = datasource.code
        tableList.add(table)
        report.tableList = tableList
        Report.withTenant("1") {
            report.save(flush: true)
        }


        def reportlist =  Report.collection.find(
                Filters.or (
                        Filters.eq("inputList.datasourceData.code", "01"),
                        Filters.eq("tableList.datasourceData.code", "01")
                )

        )

        println reportlist.size()
        Report.collection.updateMany(
                Filters.eq("inputList.datasourceData.code", "01"),
                new Document('$set', new Document('inputList.$[].datasourceData.name', "02"))
        )

        Report.Style style = new Report.Style(scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt")
        report.addStyle(style)

        Report.Table table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1)
        def tableDataSource = new Report.Table.DatasourceData(code: "01", name: "中心数据源")
        table.datasourceData = tableDataSource
        report.addTable(table)*/
    }
    def destroy = {
    }
}
