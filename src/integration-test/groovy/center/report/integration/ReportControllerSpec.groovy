package center.report.integration

import center.report.Report
import center.report.ReportController
import center.report.ReportDatasource
import center.report.ReportModule
import center.report.co.ReportConditionCO
import center.report.co.ReportCtrlLogCO
import center.report.common.CommonValue
import center.report.enst.CtrlKindEnum
import center.report.enst.CtrlStatusEnum
import center.report.enst.DatasourceConfigKindEnum
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.test.mongodb.MongoSpec
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.web.context.WebApplicationContext
import spock.lang.Ignore
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@Integration
@Rollback
//@Ignore
class ReportControllerSpec extends Specification {
    @Autowired
    MessageSource messageSource

    @Autowired
    WebApplicationContext ctx

    def setupSpec() {
        Locale.setDefault(Locale.CHINA)
    }

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

        def report1 = new Report(code: "test", name: "test", runway: 1, ctrlStatus: 2, grpCode: "1", cloudId: "1")
        report1.save(flush: true)
    }

    def cleanup() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes()
        Report.collection.deleteMany([:])
        ReportDatasource.collection.deleteMany([:])
        ReportModule.collection.deleteMany([:])
    }

    void "controller保存报表输入参数-测试1"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()

        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("KZRZB", "1")
        then:
        assert test?.inputList?.size() == 2
        def data = test.inputList.get(0)
        assert data.name == "org"
        assert data.caption == "机构"
        assert data.seqNum == 0
        assert data.dataType == "21"
        assert data.inputType == 3
        assert data.sqlText == "select id col_value,name col_title from org_list where kind='H'"
        assert data.defType == "我的机构"

        def data1 = test.inputList.get(1)
        assert data1.name == "kind"
        assert data1.caption == "种类"
        assert data1.seqNum == 0
        assert data1.dataType == "21"
        assert data1.inputType == 3
        assert data1.sqlText == "select id col_value,name col_title from org_list"
        assert data1.defType == "默认种类"
    }

    void "controller保存报表输入参数-测试2"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = "02"
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "参数数据源有误;"
    }

    void "controller保存报表输入参数-测试2-1"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = ""
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "参数数据源不能为空;"
    }

    void "controller保存报表输入参数-测试3"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        Report.Input input = new Report.Input(name: "kind", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "参数名称已存在;"
    }

    void "controller保存报表输入参数-测试4"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        Report.Input input = new Report.Input(name: "", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "参数名称不能为空;"
    }

    void "controller保存报表输入参数-测试5"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        Report.Input input = new Report.Input(name: "org", caption: "", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "参数标签不能为空;"
    }

    void "controller保存报表输入参数-测试6"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "参数数据类型不能为空;"
    }

    void "controller保存报表输入参数-测试7"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind=[]", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "动态查询参数sql错误;"
    }

    void "controller保存报表输入参数-测试7-1"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind=[kind]", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "动态查询参数sql错误;"
    }

    void "controller保存报表输入参数-测试8"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code
        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code
        report.inputList.add(input1)

        List<ReportModule.ReportLinkInput> reportLinkInputList = []
        reportLinkInputList.add(new ReportModule.ReportLinkInput(name: "org"))
        reportLinkInputList.add(new ReportModule.ReportLinkInput(name: "kind"))

        List<ReportModule.ReportLink> reportLinkList = []
        reportLinkList.add(new ReportModule.ReportLink(rptId: report.id, showName: "显示名称", comment: "说明", status: 0, linkInputList: reportLinkInputList))

        Map<String, ReportModule.ReportInputData> reportInputDataMap = [:]
        reportInputDataMap[input.name] = new ReportModule.ReportInputData(name: input.name, caption: "模块输入标签1", seqNum: 9, dataType: "31")
        reportInputDataMap[input1.name] =  new ReportModule.ReportInputData(name: input1.name, caption: "模块输入标签2", seqNum: 10, dataType: "32")

        Map<String, ReportModule.ReportData> reportDataMap = [(report.id.toString()) : new ReportModule.ReportData(rptId: report.id, name: report.name, code: report.code, reportInputDataMap: reportInputDataMap)]
        ReportModule module = new ReportModule(moduleId: "01", cloudId: "1", cloudName: "区域云名称", reportLinkList: reportLinkList, reportDataMap: reportDataMap)
        module.save(flush: true)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("KZRZB", "1")

        ReportModule testModule = ReportModule.findByModuleId("01")
        then:
        assert test?.inputList?.size() == 2
        def data = test.inputList.get(0)
        assert data.name == "org"
        assert data.caption == "机构"
        assert data.seqNum == 0
        assert data.dataType == "21"
        assert data.inputType == 3
        assert data.sqlText == "select id col_value,name col_title from org_list where kind='H'"
        assert data.defType == "我的机构"

        def data1 = test.inputList.get(1)
        assert data1.name == "kind"
        assert data1.caption == "种类"
        assert data1.seqNum == 0
        assert data1.dataType == "21"
        assert data1.inputType == 3
        assert data1.sqlText == "select id col_value,name col_title from org_list"
        assert data1.defType == "默认种类"

//        assert testModule?.reportDataMap.get("$test.id".toString()).reportInputDataMap.size() == 2
//        def testInputMap1 = testModule?.reportDataMap.get("$test.id".toString()).reportInputDataMap.get(input.name)
//        assert testInputMap1?.name == input.name
//        assert testInputMap1?.caption == input.caption
//        assert testInputMap1?.seqNum == input.seqNum
//        assert testInputMap1?.dataType == input.dataType
//
//        def testInputMap2 = testModule?.reportDataMap.get("$test.id".toString()).reportInputDataMap.get(input1.name)
//        assert testInputMap2?.name == input1.name
//        assert testInputMap2?.caption == input1.caption
//        assert testInputMap2?.seqNum == input1.seqNum
//        assert testInputMap2?.dataType == input1.dataType
    }

    void "controller保存报表输入参数-测试9"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        Report.Input input = new Report.Input(name: "org", caption: "机构", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = datasource.code

        Report.Input input1 = new Report.Input(name: "kind", caption: "种类", seqNum: 0, dataType: "21", inputType: 3, sqlText: "select id col_value,name col_title from org_list", defType: "默认种类")
        input1.datasourceDataCode = datasource.code

        List<ReportModule.ReportLinkInput> reportLinkInputList = []
        reportLinkInputList.add(new ReportModule.ReportLinkInput(name: "org"))
        reportLinkInputList.add(new ReportModule.ReportLinkInput(name: "kind"))

        List<ReportModule.ReportLink> reportLinkList = []
        reportLinkList.add(new ReportModule.ReportLink(rptId: report.id, showName: "显示名称", comment: "说明", status: 0, linkInputList: reportLinkInputList))

        Map<String, ReportModule.ReportInputData> reportInputDataMap = [:]
        reportInputDataMap[input.name] = new ReportModule.ReportInputData(name: input.name, caption: "模块输入标签1", seqNum: 9, dataType: "31")
        reportInputDataMap[input1.name] =  new ReportModule.ReportInputData(name: input1.name, caption: "模块输入标签2", seqNum: 10, dataType: "32")

        Map<String, ReportModule.ReportData> reportDataMap = [(report.id.toString()) : new ReportModule.ReportData(rptId: report.id, name: report.name, code: report.code, reportInputDataMap: reportInputDataMap)]
        ReportModule module = new ReportModule(moduleId: "01", cloudId: "1", cloudName: "区域云名称", reportLinkList: reportLinkList, reportDataMap: reportDataMap)
        module.save(flush: true)

        report.inputList = new ArrayList<Report.Input>()
        report.inputList.add(input)
        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("KZRZB", "1")

        ReportModule testModule = ReportModule.findByModuleId("01")
        then:
        assert test?.inputList?.size() == 1
        def data = test.inputList.get(0)
        assert data.name == "org"
        assert data.caption == "机构"
        assert data.seqNum == 0
        assert data.dataType == "21"
        assert data.inputType == 3
        assert data.sqlText == "select id col_value,name col_title from org_list where kind='H'"
        assert data.defType == "我的机构"

//        assert testModule?.reportDataMap.get("$test.id".toString()).reportInputDataMap.size() == 1
//        Map<Long, ReportData> testReportDataMap = testModule?.reportDataMap
//        def testInputMap1 = testReportDataMap.get("$test.id".toString()).reportInputDataMap.get(input.name)
//        assert testInputMap1?.name == input.name
//        assert testInputMap1?.caption == input.caption
//        assert testInputMap1?.seqNum == input.seqNum
//        assert testInputMap1?.dataType == input.dataType
    }

    void "controller保存报表数据表-测试1"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 2)
        table.datasourceDataCode = datasource.code
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        def table1 = new Report.Table(name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1)
        table1.datasourceDataCode = datasource.code
        report.tableList.add(table1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("KZRZB", "1")

        then:
        assert test?.tableList?.size() == 2
        def data = test.tableList.get(0)
        assert data.name == "tb0"
        assert data.seqNum == 1
        assert data.sqlText == "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30"
        assert data.datasourceDataCode == "01"

        def data1 = test.tableList.get(1)
        assert data1.name == "table"
        assert data1.seqNum == 2
        assert data1.sqlText == "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')"
        assert data1.datasourceDataCode == "01"
    }

    void "controller保存报表数据表-测试2"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 2)
        table.datasourceDataCode = ""
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        def table1 = new Report.Table(name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1)
        table1.datasourceDataCode = datasource.code
        report.tableList.add(table1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "数据表数据源不能为空;"
    }

    void "controller保存报表数据表-测试3"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 2)
        table.datasourceDataCode = "02"
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        def table1 = new Report.Table(name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1)
        table1.datasourceDataCode = datasource.code
        report.tableList.add(table1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "数据表数据源有误;"
    }

    void "controller保存报表数据表-测试4"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")
        def table = new Report.Table(name: "", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 2)
        table.datasourceDataCode = datasource.code
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        def table1 = new Report.Table(name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1)
        table1.datasourceDataCode = datasource.code
        report.tableList.add(table1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "数据表名称不能为空;"
    }

    void "controller保存报表数据表-测试5"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def table = new Report.Table(name: "tb0", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 2)
        table.datasourceDataCode = datasource.code
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        def table1 = new Report.Table(name: "tb0", sqlText: "select name,abbr,gender,birthday,archive_address from resident_list LIMIT 30", seqNum: 1)
        table1.datasourceDataCode = datasource.code
        report.tableList.add(table1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "数据表名称已存在;"
    }

    void "controller保存报表样式-测试1"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def style = new Report.Style(scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt", chart: "<chart></chart>", comment: "样式1说明")
        report.styleList = new ArrayList<>()
        report.styleList.add(style)

        def style1 = new Report.Style(scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt", chart: "<chart></chart>", comment: "样式2说明")
        report.styleList.add(style1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("KZRZB", "1")

        then:
        assert test?.styleList?.size() == 2
        def data = test.styleList.get(0)
        assert data.scene == 0
        assert data.fileUrl == "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt"
        assert data.chart == "<chart></chart>"
        assert data.comment == "样式1说明"

        def data1 = test.styleList.get(1)
        assert data1.scene == 1
        assert data1.fileUrl == "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt"
        assert data1.chart == "<chart></chart>"
        assert data1.comment == "样式2说明"
    }

    void "controller保存报表样式-测试2"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def style = new Report.Style(scene: null, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt", chart: "<chart></chart>", comment: "样式1说明")
        report.styleList = new ArrayList<>()
        report.styleList.add(style)

        def style1 = new Report.Style(scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt", chart: "<chart></chart>", comment: "样式2说明")
        report.styleList.add(style1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "场景不能为空;"
    }

    void "controller保存报表样式-测试3"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def style = new Report.Style(scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt", chart: "<chart></chart>", comment: "样式1说明")
        report.styleList = new ArrayList<>()
        report.styleList.add(style)

        def style1 = new Report.Style(scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt", chart: "<chart></chart>", comment: "样式2说明")
        report.styleList.add(style1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "场景已存在;"
    }

    void "controller保存报表样式-测试4"() {
        given: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)

        def report = Report.findByCodeAndCloudId("KZRZB", "1")

        def style = new Report.Style(scene: 0, fileUrl: "", chart: "<chart></chart>", comment: "样式1说明")
        report.styleList = new ArrayList<>()
        report.styleList.add(style)

        def style1 = new Report.Style(scene: 1, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/1c70fe2710664418bd73964db133065e/f83b5ed92be2412babe7aba2e837f94f.xslt", chart: "<chart></chart>", comment: "样式2说明")
        report.styleList.add(style1)

        when: "执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "文件不能为空;"
    }

    void "controller保存报表基本信息-测试1"() {
        given:
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "02", cloudId: "1", runway: 2)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("JCYCD", "1")
        then:
        assert test?.code == "JCYCD"
        assert test?.name == "监测依从度统计"
        assert test?.grpCode == "02"
        assert test?.cloudId == "1"
        assert test?.runway == 2
    }

    void "controller保存报表基本信息-测试2"() {
        given:
        def report = new Report(code: "", name: "监测依从度统计", grpCode: "02", cloudId: "1", runway: 2)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        println "[Report test]" + java.util.Locale.getDefault(Locale.Category.FORMAT)
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("report.code.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("report.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller保存报表基本信息-测试3"() {
        given:
        def report = new Report(code: "JCYCD", name: "", grpCode: "02", cloudId: "1", runway: 2)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("report.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("report.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller保存报表基本信息-测试4"() {
        given:
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "", cloudId: "1", runway: 2)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("report.grpCode.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("report.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller保存报表基本信息-测试5"() {
        given:
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "01", cloudId: "", runway: 2)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1
    }

    void "controller保存报表基本信息-测试6"() {
        given:
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "01", cloudId: "1", runway: null)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3

        def errAttribute = messageSource.getMessage("report.runway.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("report.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller保存报表基本信息-测试7"() {
        given:
        def report = new Report(code: "JCYCD", name: "监测依从度统计", grpCode: "01", cloudId: "1", runway: 3)
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "报表执行方式不在取值范围内;"

        def errAttribute = messageSource.getMessage("report.runway.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("report.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.inlist.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller保存报表基本信息-测试8"() {
        given:
        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, ctrlStatus: 2, grpCode: "1", cloudId: "2")
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.save(report)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        when:
        Report test = Report.findByCodeAndCloudId("KZRZB", "2")
        then:
        assert test?.code == "KZRZB"
        assert test?.name == "科主任周报"
        assert test?.runway == 1
        assert test?.ctrlStatus == 0
        assert test?.grpCode == "1"
    }

    void "controller条件查询报表-测试1"() {
        given:
        String grpCode = ""
        String cloudId = ""
//        Integer isFixed = null
        String rptName = ""
        Integer ctrlStatus = 0
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        ReportConditionCO reportConditionCO = new ReportConditionCO(cloudId: cloudId, grpCode: grpCode, rptName: rptName, ctrlStatus: ctrlStatus, pageNow: 0, pageSize: 1)
        controller.getByCondition(reportConditionCO)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size()==0
    }

    void "controller条件查询报表-测试2"() {
        given:
        String grpCode = ""
        String cloudId = "1"
//        Integer isFixed = null
        String rptName = ""
        Integer ctrlStatus = null
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        ReportConditionCO reportConditionCO = new ReportConditionCO(cloudId: cloudId, grpCode: grpCode, rptName: rptName, ctrlStatus: ctrlStatus, pageNow: 0, pageSize: 1)
        controller.getByCondition(reportConditionCO)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size()==1
        def data = list.get(0)
        assert data.name == "科主任周报"
        assert data.code == "KZRZB"
        assert data.runway == 1
        assert data.ctrlStatus == 2
        assert data.grpCode == "1"

        assert jsonData.total == 2
    }

    void "controller条件查询报表-测试3"() {
        given:
        String grpCode = ""
        String cloudId = "1"
//        Integer isFixed = null
        String rptName = "科主任周报"
        Integer ctrlStatus = null
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        ReportConditionCO reportConditionCO = new ReportConditionCO(cloudId: cloudId, grpCode: grpCode, rptName: rptName, ctrlStatus: ctrlStatus, pageNow: 0, pageSize: 1)
        controller.getByCondition(reportConditionCO)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size()==1
        def data = list.get(0)
        assert data.name == "科主任周报"
        assert data.code == "KZRZB"
        assert data.runway == 1
        assert data.ctrlStatus == 2
        assert data.grpCode == "1"
    }

    void "controller条件查询报表-测试4"() {
        given:
        String grpCode = "1"
        String cloudId = "1"
//        Integer isFixed = null
        String rptName = ""
        Integer ctrlStatus = null
        when:
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        ReportConditionCO reportConditionCO = new ReportConditionCO(cloudId: cloudId, grpCode: grpCode, rptName: rptName, ctrlStatus: ctrlStatus, pageNow: 0, pageSize: 1)
        controller.getByCondition(reportConditionCO)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size()==1
        def data = list.get(0)
        assert data.name == "科主任周报"
        assert data.code == "KZRZB"
        assert data.runway == 1
        assert data.ctrlStatus == 2
        assert data.grpCode == "1"

        assert jsonData.total == 2
    }

    void "controller报表提交-测试1"() {
        given:
        def report = new Report(code: "KZRZB", name: "科主任周报", grpCode: "01", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.BZZCY.code)
        report.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: report.id, accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.submit(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response

        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 1
        when:"验证"
        Report actualReport = Report.findByCodeAndCloudId("KZRZB", "2")
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code

        assert actualReport.ctrlLogList?.size() == 1
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "2"
        assert log.ctrlKind == CtrlKindEnum.SUBMIT.kind
        assert log.preStatus == CtrlStatusEnum.BZZCY.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.accountName == "王"
        assert log.accountId == "1"
    }

    void "controller报表提交-测试2"() {
        given:
        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null)
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.submit(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response

        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表提交-测试3"() {
        given:
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.submit(null)
        then:"结果"
        HttpServletResponse response = controller.response

        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表提交-测试4"() {
        given:
        def report = new Report(code: "KZRZB", name: "科主任周报", grpCode: "01", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        report.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: report.id, accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.submit(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response

        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData.code == 3
        assert jsonData.message == "当前报表不是草稿状态;"
    }

    void "controller报表停用-测试1"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "停用理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.stop(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport =  Report.findByCodeAndCloudId("TZYB", "2")
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.STOP_USING.code

        assert actualReport.ctrlLogList?.size() == 1
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "2"
        assert log.ctrlKind == CtrlKindEnum.STOP_USING.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.STOP_USING.code
        assert log.adscript == "停用理由"
        assert log.accountId == "1"
        assert log.accountName == "王"
    }

    void "controller报表停用-测试2"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null, adscript: "停用理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.stop(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表停用-测试3"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null, adscript: "停用理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.stop(null)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表通过-测试1"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "通过理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditSuccess(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCodeAndCloudId("TZYB", "2")

        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
        assert actualReport.ctrlLogList?.size() == 1
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "2"
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.AUDIT_SUCCESS.code
        assert log.adscript == "通过理由"
        assert log.accountId == "1"
        assert log.accountName == "王"
    }

    void "controller报表通过-测试2"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null, adscript: "通过理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditSuccess(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表通过-测试3"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null, adscript: "通过理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditSuccess(null)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表通过-测试4"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.BZZCY.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "通过理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditSuccess(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "当前报表需要提交审核;"
    }

    void "controller报表未通过-测试1"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "未通过理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditRollback(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCodeAndCloudId("TZYB", "2")
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.AUDIT_ERROR.code
        assert actualReport.ctrlLogList?.size() == 1
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "2"
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.AUDIT_ERROR.code
        assert log.adscript == "未通过理由"
        assert log.accountId == "1"
        assert log.accountName == "王"
    }

    void "controller报表未通过-测试2"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)
        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null, adscript: "未通过理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditRollback(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表未通过-测试3"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code);
        rpt.save(flush: true)
        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: null, adscript: "未通过理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditRollback(null)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表未通过-测试4"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", cloudId: "2", runway: 1, ctrlStatus: CtrlStatusEnum.BZZCY.code);
        rpt.save(flush: true)
        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "未通过理由")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.auditRollback(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "当前报表需要提交审核;"
    }

    void "controller报表重启用-测试1"() {
        given:"参数"
        Report rpt = new Report(code: "YPFX", name: "药品分析", grpCode: "01", runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        def report5CtrlLog = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.BZZCY.code, newStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, ctrlKind: CtrlKindEnum.AUDIT.kind, logTime: new Date()-2, accountId: "1", accountName: "王")
        def report5CtrlLog2 = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, newStatus: CtrlStatusEnum.STOP_USING.code, ctrlKind: CtrlKindEnum.STOP_USING.kind, logTime: new Date()-1, accountId: "1", accountName: "王")
        rpt.ctrlLogList = [report5CtrlLog2, report5CtrlLog]
        rpt.save(flush: true)
        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "重启用理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.reboot(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCodeAndCloudId("YPFX", "1")
        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert actualReport.ctrlLogList?.size() == 3
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.AUDIT.kind
        assert log.preStatus == CtrlStatusEnum.STOP_USING.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.adscript == "重启用理由"
        assert log.accountId == "1"
        assert log.accountName == "王"
    }

    void "controller报表重启用-测试2"() {
        given:"参数"
        Report rpt = new Report(code: "YPFX", name: "药品分析", grpCode: "01", runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code)
        def report5CtrlLog = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.BZZCY.code, newStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, ctrlKind: CtrlKindEnum.AUDIT.kind, logTime: new Date()-2, accountId: "1", accountName: "王")
        def report5CtrlLog2 = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, newStatus: CtrlStatusEnum.STOP_USING.code, ctrlKind: CtrlKindEnum.STOP_USING.kind, logTime: new Date()-1, accountId: "1", accountName: "王")
        rpt.ctrlLogList = [report5CtrlLog2, report5CtrlLog]
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "重启用理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.reboot(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表未停用;"
    }

    void "controller报表重启用-测试3"() {
        given:"参数"
        Report rpt = new Report(code: "YPFX", name: "药品分析", grpCode: "01", runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        def report5CtrlLog = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.BZZCY.code, newStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, ctrlKind: CtrlKindEnum.AUDIT.kind, logTime: new Date()-2, accountId: "1", accountName: "王")
        rpt.ctrlLogList = [report5CtrlLog]
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "重启用理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.reboot(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "停用记录为空，数据有误;"
    }

    void "controller报表重启用-测试4"() {
        given:"参数"
        Report rpt = new Report(code: "YPFX", name: "药品分析", grpCode: "01", runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        def report5CtrlLog = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.BZZCY.code, newStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, ctrlKind: CtrlKindEnum.AUDIT.kind, logTime: new Date()-2, accountId: "1", accountName: "王")
        rpt.ctrlLogList = [report5CtrlLog]
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "重启用理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.reboot(null)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表重启用-测试5"() {
        given:"参数"
        Report rpt = new Report(code: "YPFX", name: "药品分析", grpCode: "01", runway: 1, cloudId: "1", ctrlStatus: CtrlStatusEnum.STOP_USING.code)
        def report5CtrlLog = new Report.CtrlLog(cloudId: "1", preStatus: CtrlStatusEnum.BZZCY.code, newStatus: CtrlStatusEnum.SUBMIT_AUDIT.code, ctrlKind: CtrlKindEnum.AUDIT.kind, logTime: new Date()-2, accountId: "1", accountName: "王")
        rpt.ctrlLogList = [report5CtrlLog]
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: 99, adscript: "重启用理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.reboot(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表区域转中心-测试1"() {
        given:"参数"
        Report rpt = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: "01", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code)

        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.toCenterReport(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCodeAndCloudId("YYYCD", "")

        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert actualReport.cloudId == ""
        assert actualReport.ctrlLogList?.size() == 1
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "2"
        assert log.ctrlKind == CtrlKindEnum.TO_CENTER.kind
        assert log.preStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.newStatus == CtrlStatusEnum.SUBMIT_AUDIT.code
        assert log.adscript == "区域转中心理由"
        assert log.accountId == "1"
        assert log.accountName == "王"
    }

    void "controller报表区域转中心-测试2"() {
        given:"参数"
        Report rpt = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: "01", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code)
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: 89, adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.toCenterReport(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表区域转中心-测试3"() {
        given:"参数"
        Report rpt = new Report(code: "YYYCD", name: "用药依从性统计", grpCode: "01", runway: 1, cloudId: "2", ctrlStatus: CtrlStatusEnum.SUBMIT_AUDIT.code)

        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.toCenterReport(null)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表取消中心共享-测试1"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: CommonValue.CENTER, ctrlStatus: CtrlStatusEnum.BZZCY.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, cloudId: "1", adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.cancelCenterReport(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 1
        assert jsonData.message == "执行成功"
        when:"验证"
        Report actualReport = Report.findByCodeAndCloudId("TZYB", "1")

        then:"验证结果"
        assert actualReport.ctrlStatus == CtrlStatusEnum.BZZCY.code
        assert actualReport.cloudId == "1"
        assert actualReport.ctrlLogList?.size() == 1
        def log = actualReport.ctrlLogList.get(0)
        assert log
        assert log.cloudId == "1"
        assert log.ctrlKind == CtrlKindEnum.CANCEL_CENTER.kind
        assert log.preStatus == CtrlStatusEnum.BZZCY.code
        assert log.newStatus == CtrlStatusEnum.BZZCY.code
        assert log.adscript == "区域转中心理由"
        assert log.accountId == "1"
        assert log.accountName == "王"
    }

    void "controller报表取消中心共享-测试2"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: CommonValue.CENTER, ctrlStatus: CtrlStatusEnum.BZZCY.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: rpt.id, cloudId: "", adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.cancelCenterReport(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "区域云标识不能为空;"
    }

    void "controller报表取消中心共享-测试3"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: CommonValue.CENTER, ctrlStatus: CtrlStatusEnum.BZZCY.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: 100, cloudId: "1", adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.cancelCenterReport(reportCtrlLogCO)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }

    void "controller报表取消中心共享-测试4"() {
        given:"参数"
        Report rpt = new Report(code: "TZYB", name: "医生团队月报", grpCode: "1", runway: 1, cloudId: CommonValue.CENTER, ctrlStatus: CtrlStatusEnum.BZZCY.code);
        rpt.save(flush: true)

        ReportCtrlLogCO reportCtrlLogCO = new ReportCtrlLogCO(reportId: 100, cloudId: "1", adscript: "区域转中心理由", accountId: "1", accountName: "王")
        when:"执行"
        ReportController controller = new ReportController()
        controller.messageSource = messageSource
        controller.cancelCenterReport(null)
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        def jsonData = JSON.parse(response.content.toString())
        assert jsonData
        assert jsonData.code == 3
        assert jsonData.message == "报表不存在"
    }
}
