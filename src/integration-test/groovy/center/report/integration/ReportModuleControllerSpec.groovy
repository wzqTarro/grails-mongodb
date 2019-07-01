package center.report.integration

import center.report.Report
import center.report.ReportDatasource
import center.report.ReportModule
import center.report.ReportModuleController
import center.report.co.DistributeModuleCO
import center.report.co.ReportLinkCO
import center.report.co.UsingReportLinkCO
import center.report.enst.CtrlStatusEnum
import center.report.enst.DatasourceConfigKindEnum
import center.report.enst.ReportLinkStatusEnum
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.test.mongodb.MongoSpec
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@Integration
@Rollback
class ReportModuleControllerSpec extends Specification {

    @Autowired
    MessageSource messageSource

    @Autowired
    WebApplicationContext ctx

    def setup() {
        grails.util.GrailsWebMockUtil.bindMockWebRequest(ctx)
        ReportDatasource datasource = new ReportDatasource(code: "08", name: "特殊数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)
        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, ctrlStatus: 2, grpCode: "1", cloudId: "1")
        Report.Style style = new Report.Style(scene: 0, fileUrl: "/text", chart: "<chart/>", comment: "说明")
        Report.Input input = new Report.Input(name: "endtime", caption: "结束时间", seqNum: 3, dataType: "23", inputType: 1, defType: "今天", datasourceDataCode: "08")
        Report.Table table = new Report.Table(datasourceDataCode: "08", name:"createRecord", seqNum: 1, sqlText: "select ol.`id` as name, COUNT(rl.`id`) as count FROM org_list ol INNER JOIN `resident_list` rl on rl.`org_id`= ol.`id` where ol.`id` = [orgID] and rl.`commit_time` BETWEEN '2019-03-01' and '2019-03-30' GROUP BY ol.`id`")
        report.inputList = [input]
        report.tableList = [table]
        report.styleList = [style]
        report.save(flush: true)

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
        module.save(flush: true)

        ReportModule module1 = new ReportModule(moduleId: "02", cloudId: "2", cloudName: "众康云test")
        module1.save(flush: true)
    }
    def cleanup() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes()
        ReportDatasource.collection.deleteMany([:])
        ReportModule.collection.deleteMany([:])
        Report.collection.deleteMany([:])
    }

    void "根据模块ID查询报表模块-测试1"() {
        given:
        String moduleId = "01"
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.show(moduleId)
        Report report = Report.findByCode("KZRZB")
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.moduleId == "01"
        assert data.cloudId == "1"
        assert data.cloudName == "众康云区域"

        assert data.reportLinkList?.size() == 1
        def reportLink = data.reportLinkList.get(0)
        assert reportLink.rptId == report.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "关联报表说明"
        assert reportLink.status == 0

        assert reportLink.linkInputList?.size() == 1
        def linkInput = reportLink.linkInputList.get(0)
        assert linkInput.name == "endtime"

        def reportDataMap = data.reportDataMap
        def reportMap = reportDataMap.("${report.id}".toString())
        assert reportMap.rptId == report.id
        assert reportMap.name == report.name
        assert reportMap.code == report.code

        def endtimeMap = reportMap.reportInputDataMap.get("endtime")
        assert endtimeMap.name == "endtime"
        assert endtimeMap.caption == "结束时间"
        assert endtimeMap.dataType == "23"
        assert endtimeMap.seqNum == 3
    }

    void "新增模块分发区域-测试1"() {
        given:
        ReportModule module = new ReportModule(moduleId: "02", cloudId: "1", cloudName: "众康云区域")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(module)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("02", "1")
        then:
        assert test.moduleId == "02"
        assert test.cloudId == "1"
        assert test.cloudName == "众康云区域"
    }

    void "新增模块分发区域-测试2"() {
        given:
        ReportModule module = new ReportModule(moduleId: "", cloudId: "3", cloudName: "众康云区域3")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(module)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("reportModule.moduleId.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportModule.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "新增模块分发区域-测试3"() {
        given:
        ReportModule module = new ReportModule(moduleId: "01", cloudId: "1", cloudName: "众康云")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        println "[ReportModule test]" + java.util.Locale.getDefault(Locale.Category.FORMAT)
        controller.save(module)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3
        assert jsonData.message == "报表分发区域必须是唯一的;"

        def errAttribute = messageSource.getMessage("reportModule.cloudId.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportModule.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "新增模块分发区域-测试4"() {
        given:
        ReportModule module = new ReportModule(moduleId: "01", cloudId: "2", cloudName: "众康云区域")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(module)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("reportModule.cloudName.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportModule.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "新增模块分发区域-测试5"() {
        given:
        ReportModule module = new ReportModule(moduleId: "01", cloudId: "", cloudName: "众康云")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(module)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
    }

    void "编辑模块分发区域-测试1"() {
        given:
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        reportModule.cloudId = "2"
        reportModule.cloudName = "众康云区域test"
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(reportModule)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleId("01")
        then:
        assert test.moduleId == "01"
        assert test.cloudId == "2"
        assert test.cloudName == "众康云区域test"
    }

    void "编辑模块分发区域-测试2"() {
        given:
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        reportModule.moduleId = "02"
        reportModule.cloudId = "2"
        reportModule.cloudName = "众康云区域test"
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(reportModule)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("reportModule.cloudId.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportModule.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "编辑模块分发区域-测试3"() {
        given:
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        reportModule.moduleId = ""
        reportModule.cloudId = "1"
        reportModule.cloudName = "众康云区域test"
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.save(reportModule)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("reportModule.moduleId.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportModule.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "新增模块使用报表-测试1"() {
        given:
        Report report = Report.findByCode("KZRZB")
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("02", "2")
        ReportLinkCO reportLinkCO = new ReportLinkCO(id: reportModule.id, reportLink: new ReportModule.ReportLink(rptId: report.id, showName: "科主任情况", comment: "关联报表说明"))
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.saveReportLink(reportLinkCO)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("02", "2")
        then:
        assert test.moduleId == "02"
        assert test.cloudId == "2"
        assert test.cloudName == "众康云test"

        def reportLinkList = test.reportLinkList
        assert reportLinkList?.size() == 1

        def reportLink = reportLinkList.get(0)
        assert reportLink.rptId == report.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "关联报表说明"

        def linkInputList = reportLink.linkInputList
        assert linkInputList?.size() == 1

        def input = linkInputList.get(0)
        assert input.name == "endtime"

        def reportDataMap = test.reportDataMap
        def reportData = reportDataMap.get("${report.id}".toString())
        assert reportData.rptId == report.id
        assert reportData.name == "科主任周报"
        assert reportData.code == "KZRZB"

        def reportInputDataMap = reportData.reportInputDataMap?.get("endtime")
        assert reportInputDataMap.name == "endtime"
        assert reportInputDataMap.caption == "结束时间"
        assert reportInputDataMap.seqNum == 3
        assert reportInputDataMap.dataType == "23"
    }

    void "新增模块使用报表-测试2"() {
        given:
        // 数据源
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)
        // 报表信息
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "02", cloudId: "1", runway: 2, ctrlStatus: CtrlStatusEnum.AUDIT_SUCCESS.code)
        // 输入参数
        Report.Input reportInput = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        reportInput.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(reportInput)
        report.save(flush: true)

        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        ReportLinkCO reportLinkCO = new ReportLinkCO(id: reportModule.id, reportLink: new ReportModule.ReportLink(rptId: report.id, showName: "新增链接报表", comment: "关联报表说明"))
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.saveReportLink(reportLinkCO)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("01", "1")
        Report kzrReport = Report.findByCode("KZRZB")
        then:
        assert test.moduleId == "01"
        assert test.cloudId == "1"
        assert test.cloudName == "众康云区域"

        def reportLinkList = test.reportLinkList
        assert reportLinkList?.size() == 2

        def reportLink = reportLinkList.get(0)
        assert reportLink.rptId == kzrReport.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "关联报表说明"

        def linkInputList = reportLink.linkInputList
        assert linkInputList?.size() == 1

        def input = linkInputList.get(0)
        assert input.name == "endtime"

        def reportDataMap = test.reportDataMap
        def reportData = reportDataMap.get("${kzrReport.id}".toString())
        assert reportData.rptId == kzrReport.id
        assert reportData.name == "科主任周报"
        assert reportData.code == "KZRZB"

        def reportInputDataMap = reportData.reportInputDataMap?.get("endtime")
        assert reportInputDataMap.name == "endtime"
        assert reportInputDataMap.caption == "结束时间"
        assert reportInputDataMap.seqNum == 3
        assert reportInputDataMap.dataType == "23"

        def reportLink1 = reportLinkList.get(1)
        assert reportLink1.rptId == report.id
        assert reportLink1.showName == "新增链接报表"
        assert reportLink1.comment == "关联报表说明"

        def linkInputList1 = reportLink1.linkInputList
        assert linkInputList1?.size() == 1

        def input1 = linkInputList1.get(0)
        assert input1.name == "org"

        def reportData1 = reportDataMap.get("${report.id}".toString())
        assert reportData1.rptId == report.id
        assert reportData1.name == "监测依从度统计"
        assert reportData1.code == "JCYCD"

        def reportInputDataMap1 = reportData1.reportInputDataMap?.get("org")
        assert reportInputDataMap1.name == "org"
        assert reportInputDataMap1.caption == "机构"
        assert reportInputDataMap1.seqNum == 0
        assert reportInputDataMap1.dataType == "21"
    }

//    void "新增模块使用报表-测试3"() {
//        given:
//        // 数据源
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
//        datasource.save(flush: true)
//        // 报表信息
//        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "02", cloudId: "1", runway: 2, ctrlStatus: CtrlStatusEnum.AUDIT_SUCCESS.code)
//        // 输入参数
//        Report.Input reportInput = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
//        reportInput.datasourceDataCode = datasource.code
//        report.inputList = new ArrayList<Report.Input>()
//        report.inputList.add(reportInput)
//        report.save(flush: true)
//
//        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
//        ReportLinkCO reportLinkCO = new ReportLinkCO(id: reportModule.id, reportLink: new ReportModule.ReportLink(rptId: report.id, showName: "科主任情况", comment: "关联报表说明"))
//        when:
//        ReportModuleController controller = new ReportModuleController()
//        controller.saveReportLink(reportLinkCO)
//        then:
//        HttpServletResponse response = controller.response
//        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
//        assert response.status == 200
//        def jsonData = JSON.parse(response.content.toString())
//
//        assert jsonData.code == 3
//        assert jsonData.message == "链接报表名称已存在;"
//    }

    void "新增模块使用报表-测试4"() {
        given:
        // 数据源
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)
        // 报表信息
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "02", cloudId: "1", runway: 2, ctrlStatus: CtrlStatusEnum.AUDIT_SUCCESS.code)
        // 输入参数
        Report.Input reportInput = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        reportInput.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(reportInput)
        report.save(flush: true)

        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        ReportLinkCO reportLinkCO = new ReportLinkCO(id: reportModule.id, reportLink: new ReportModule.ReportLink(rptId: report.id, showName: "", comment: "关联报表说明"))
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.saveReportLink(reportLinkCO)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3
        assert jsonData.message == "链接报表显示名称不能为空;"
    }

    void "新增模块使用报表-测试5"() {
        given:
        // 数据源
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)
        // 报表信息
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "02", cloudId: "1", runway: 2, ctrlStatus: CtrlStatusEnum.AUDIT_SUCCESS.code)
        // 输入参数
        Report.Input reportInput = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        reportInput.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(reportInput)
        report.save(flush: true)

        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        ReportLinkCO reportLinkCO = new ReportLinkCO(id: reportModule.id, reportLink: new ReportModule.ReportLink(rptId: report.id, showName: "新增链接报表2", comment: "关联报表说明"))
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.saveReportLink(reportLinkCO)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("01", "1")
        Report kzrReport = Report.findByCode("KZRZB")
        then:
        assert test.moduleId == "01"
        assert test.cloudId == "1"
        assert test.cloudName == "众康云区域"

        def reportLinkList = test.reportLinkList
        assert reportLinkList?.size() == 2

        def reportLink = reportLinkList.get(0)
        assert reportLink.rptId == kzrReport.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "关联报表说明"

        def linkInputList = reportLink.linkInputList
        assert linkInputList?.size() == 1

        def input = linkInputList.get(0)
        assert input.name == "endtime"

        def reportDataMap = test.reportDataMap
        def reportData = reportDataMap.get("${kzrReport.id}".toString())
        assert reportData.rptId == kzrReport.id
        assert reportData.name == "科主任周报"
        assert reportData.code == "KZRZB"

        def reportInputDataMap = reportData.reportInputDataMap?.get("endtime")
        assert reportInputDataMap.name == "endtime"
        assert reportInputDataMap.caption == "结束时间"
        assert reportInputDataMap.seqNum == 3
        assert reportInputDataMap.dataType == "23"

        def reportLink1 = reportLinkList.get(1)
        assert reportLink1.rptId == report.id
        assert reportLink1.showName == "新增链接报表2"
        assert reportLink1.comment == "关联报表说明"

        def linkInputList1 = reportLink1.linkInputList
        assert linkInputList1?.size() == 1

        def input1 = linkInputList1.get(0)
        assert input1.name == "org"

        def reportData1 = reportDataMap.get("${report.id}".toString())
        assert reportData1.rptId == report.id
        assert reportData1.name == "监测依从度统计"
        assert reportData1.code == "JCYCD"

        def reportInputDataMap1 = reportData1.reportInputDataMap?.get("org")
        assert reportInputDataMap1.name == "org"
        assert reportInputDataMap1.caption == "机构"
        assert reportInputDataMap1.seqNum == 0
        assert reportInputDataMap1.dataType == "21"
    }

    void "编辑模块使用报表-测试1"() {
        given:
        Report report = Report.findByCode("KZRZB")
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        ReportLinkCO reportLinkCO = new ReportLinkCO(id: reportModule.id, reportLink: new ReportModule.ReportLink(rptId: report.id, showName: "科主任情况", comment: "修改报表说明", linkInputList:[
                new ReportModule.ReportLinkInput(name: "endtime", valLet: 2, formula:"test")
        ]))
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.saveReportLink(reportLinkCO)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("01", "1")
        then:
        assert test.moduleId == "01"
        assert test.cloudId == "1"
        assert test.cloudName == "众康云区域"

        def reportLinkList = test.reportLinkList
        assert reportLinkList?.size() == 1

        def reportLink = reportLinkList.get(0)
        assert reportLink.rptId == report.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "修改报表说明"

        def linkInputList = reportLink.linkInputList
        assert linkInputList?.size() == 1

        def testInput = linkInputList.get(0)
        assert testInput.name == "endtime"
        assert testInput.valLet == 2
        assert testInput.formula == "test"

        def reportDataMap = test.reportDataMap
        def reportData = reportDataMap.get("${report.id}".toString())
        assert reportData.rptId == report.id
        assert reportData.name == report.name
        assert reportData.code == report.code

        def reportInputDataMap = reportData.reportInputDataMap?.get("endtime")
        assert reportInputDataMap.name == "endtime"
        assert reportInputDataMap.caption == "结束时间"
        assert reportInputDataMap.seqNum == 3
        assert reportInputDataMap.dataType == "23"
    }

    void "启用链接报表-测试1"() {
        given:
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.startUsingReportLink(new UsingReportLinkCO(id: reportModule.id, showName: "科主任情况"))
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("01", "1")
        then:
        def reportLink = test.reportLinkList.find {
            it.showName == "科主任情况"
        }
        assert reportLink
        assert reportLink.status == ReportLinkStatusEnum.NORMAL.code
    }

    void "删除链接报表-测试1"() {
        given:
        Report report = Report.findByCode("KZRZB")
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("02", "2")
        reportModule.reportLinkList = []
        reportModule.reportLinkList.add(new ReportModule.ReportLink(rptId: report.id, showName: "科主任情况", comment: "关联报表说明", status: ReportLinkStatusEnum.MAINTAIN.code))
        reportModule.save(flush: true)
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.deleteReportLink(new UsingReportLinkCO(id: reportModule.id, showName: "科主任情况"))
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("02", "2")
        then:
        assert test.reportLinkList == []
    }

    void "删除链接报表-测试2"() {
        given:
        Report report = Report.findByCode("KZRZB")
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("02", "2")
        reportModule.reportLinkList = []
        reportModule.reportLinkList.add(new ReportModule.ReportLink(rptId: report.id, showName: "科主任情况", comment: "关联报表说明", status: ReportLinkStatusEnum.NORMAL.code))
        reportModule.save(flush: true)
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.deleteReportLink(new UsingReportLinkCO(id: reportModule.id, showName: "科主任情况"))
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 3
        assert jsonData.message == "使用状态下报表无法删除;"
    }

    void "模块分发-测试1"() {
        given:
        Report report = Report.findByCode("KZRZB")
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.distributeModule(new DistributeModuleCO(sourceModuleCloudId: reportModule.id, cloudId: "", cloudName: ""))
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("01", "")
        then:
        assert test.moduleId == "01"
        assert test.cloudId == ""
        assert test.cloudName == ""

        def reportLinkList = test.reportLinkList
        assert reportLinkList.size() == 1

        def reportLink = reportLinkList.get(0)
        assert reportLink.rptId == report.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "关联报表说明"
        assert reportLink.status == ReportLinkStatusEnum.MAINTAIN.code

        def linkReportList = reportLink.linkInputList
        assert linkReportList.size() == 1

        def linkReport = linkReportList.get(0)
        assert linkReport.name == "endtime"

        def reportDataMap = test.reportDataMap
        def reportData = reportDataMap.("${report.id}".toString())
        assert reportData.rptId == report.id
        assert reportData.name == report.name
        assert reportData.code == report.code

        def inputMap = reportData.reportInputDataMap."endtime"
        assert inputMap.name == "endtime"
        assert inputMap.caption == "结束时间"
        assert inputMap.seqNum == 3
        assert inputMap.dataType == "23"
    }

    void "模块分发-测试2"() {
        given:
        Report report = Report.findByCode("KZRZB")
        ReportModule reportModule = ReportModule.findByModuleIdAndCloudId("01", "1")
        when:
        ReportModuleController controller = new ReportModuleController()
        controller.messageSource = messageSource
        controller.distributeModule(new DistributeModuleCO(sourceModuleCloudId: reportModule.id, cloudId: "2", cloudName: "2"))
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModule test = ReportModule.findByModuleIdAndCloudId("01", "2")
        then:
        assert test.moduleId == "01"
        assert test.cloudId == "2"
        assert test.cloudName == "2"

        def reportLinkList = test.reportLinkList
        assert reportLinkList.size() == 1

        def reportLink = reportLinkList.get(0)
        assert reportLink.rptId == report.id
        assert reportLink.showName == "科主任情况"
        assert reportLink.comment == "关联报表说明"
        assert reportLink.status == ReportLinkStatusEnum.MAINTAIN.code

        def linkReportList = reportLink.linkInputList
        assert linkReportList.size() == 1

        def linkReport = linkReportList.get(0)
        assert linkReport.name == "endtime"

        def reportDataMap = test.reportDataMap
        def reportData = reportDataMap.("${report.id}".toString())
        assert reportData.rptId == report.id
        assert reportData.name == report.name
        assert reportData.code == report.code

        def inputMap = reportData.reportInputDataMap."endtime"
        assert inputMap.name == "endtime"
        assert inputMap.caption == "结束时间"
        assert inputMap.seqNum == 3
        assert inputMap.dataType == "23"
    }
}
