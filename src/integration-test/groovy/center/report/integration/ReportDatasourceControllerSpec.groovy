package center.report.integration

import center.report.Application
import center.report.Report
import center.report.ReportDatasource
import center.report.ReportDatasourceController
import center.report.enst.DatasourceConfigKindEnum
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.test.mongodb.MongoSpec
import grails.testing.mixin.integration.Integration
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.context.support.StaticMessageSource
import org.springframework.web.context.WebApplicationContext
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@Integration
@Rollback
class ReportDatasourceControllerSpec extends Specification{

    @Autowired
    MessageSource messageSource

    @Autowired
    WebApplicationContext ctx

    @Shared
    GrailsWebRequest request

    def setupSpec() {
        java.util.Locale.setDefault(Locale.CHINA)
//        URL url = new File('grails-app/i18n').toURI().toURL()
//        messageSource = new ResourceBundleMessageSource()
//        messageSource.bundleClassLoader = new URLClassLoader(url)
//        messageSource.basename = 'messages'
    }

    def setup() {
        grails.util.GrailsWebMockUtil.bindMockWebRequest(ctx)
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)
        ReportDatasource datasource1 = new ReportDatasource(code: "03", name: "特定数据源", kind: DatasourceConfigKindEnum.SPECIALLY.kind, config: '{"cloudId":"1"}')
        datasource1.save(flush: true)
    }

    def cleanup() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes()
        ReportDatasource.collection.deleteMany([:])
        Report.collection.deleteMany([:])
    }

    void "controller修改数据源-测试1"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        datasource.name = "测试"
        datasource.kind = DatasourceConfigKindEnum.CENTER.kind
        datasource.config = '{"cloudId":"123"}'

        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, ctrlStatus: 0, grpCode: "01", cloudId: "1")

        Report.Input input = new Report.Input(name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构", datasourceDataCode: datasource.code)
        report.inputList = new ArrayList<>()
        report.inputList.add(input)

        Report.Table table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1, datasourceDataCode: datasource.code)
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        report.datasourceDataMap = [:]
        report.datasourceDataMap.put(datasource.code, "中心数据源")
        report.save(flush: true)

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1

        when: "验证"
        def testDatasource = ReportDatasource.findByCode("01")
        def testReport = Report.findByCloudIdAndCode("1" ,"KZRZB")

        then:
        assert testDatasource?.code == "01"
        assert testDatasource?.name == "测试"
        assert testDatasource?.kind == DatasourceConfigKindEnum.CENTER.kind
        assert testDatasource?.config == '{"cloudId":"123"}'

        assert testReport?.datasourceDataMap."01" == "测试"
    }

    void "controller修改数据源-测试2"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        datasource.name = null

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportDatasource.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportDatasource.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller修改数据源-测试3"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        datasource.kind = -1

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3
        assert jsonData?.message == "配置类型有误;"

    }

    void "controller修改数据源-测试4"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        datasource.kind = null

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3
        assert jsonData?.message == "配置类型有误;"
    }

    void "controller修改数据源-测试5"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        datasource.kind = 10

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3
        assert jsonData?.message == "配置类型有误;"
    }

    void "controller修改数据源-测试6"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        datasource.kind = DatasourceConfigKindEnum.SPECIALLY.kind
        datasource.config = '{"cloudId":""}'

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3
        assert jsonData?.message == "特定区域数据源需要指定区域ID;"
    }

    void "controller修改数据源-测试7"() {
        setup: "模拟数据"
        ReportDatasource datasource = ReportDatasource.findByCode("03")
        datasource.name="中心数据源"

        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        println "[ReportDatasource test]" + java.util.Locale.getDefault(Locale.Category.FORMAT)
        controller.save(datasource)

        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportDatasource.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportDatasource.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller新增数据源-测试1"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1
        when:
        def test = ReportDatasource.findByCode("02")
        then:
        assert test?.code == "02"
        assert test?.name == "区域数据源"
        assert test?.kind == DatasourceConfigKindEnum.CENTER.kind
        assert test?.config == '{"cloudId":"123"}'
    }

    void "controller新增数据源-测试2"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "01", name: "区域数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportDatasource.code.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportDatasource.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller新增数据源-测试3"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportDatasource.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportDatasource.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller新增数据源-测试4"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: 10, config: '{"cloudId":"123"}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        assert jsonData?.message == "配置类型有误;"
    }

    void "controller新增数据源-测试5"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: null, config: '{"cloudId":"123"}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        assert jsonData?.message == "配置类型有误;"
    }

    void "controller新增数据源-测试6"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: -1, config: '{"cloudId":"123"}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        assert jsonData?.message == "配置类型有误;"
    }

    void "controller新增数据源-测试7"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: DatasourceConfigKindEnum.SPECIALLY.kind, config: '{"cloudId":""}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        assert jsonData?.message == "特定区域数据源需要指定区域ID;"
    }

    void "controller新增数据源-测试8"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: null)
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        assert jsonData?.message == "配置不能为空;"
    }

    void "controller新增数据源-测试9"() {
        setup: "模拟数据"
        ReportDatasource datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"kind":""}')
        when: "执行"
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.save(datasource)
        then: "验证"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        assert jsonData?.message == "配置有误;"
    }

    void "controller删除数据源-测试1"() {
        given:
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        when:
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.delete(datasource)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1
        when:
        ReportDatasource test = ReportDatasource.findByCode("01")
        then:
        assert test == null
    }

    void "controller删除数据源-测试2"() {
        given:
        ReportDatasource datasource = ReportDatasource.findByCode("01")
        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, ctrlStatus: 0, grpCode: "01", cloudId: "1")

        Report.Input input = new Report.Input(name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构", datasourceDataCode: datasource.code)
        report.inputList = new ArrayList<>()
        report.inputList.add(input)

        Report.Table table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1, datasourceDataCode: datasource.code)
        report.tableList = new ArrayList<>()
        report.tableList.add(table)

        report.datasourceDataMap = [:]
        report.datasourceDataMap.put(datasource.code, "中心数据源")
        report.save(flush: true)
        when:
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.delete(datasource)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3
        assert jsonData?.message == "数据源已被使用, 无法删除;"
    }

    void "controller查询数据源"() {
        given:

        when:
        ReportDatasourceController controller = new ReportDatasourceController()
        controller.messageSource = messageSource
        controller.getByCondition()
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1
        def list = jsonData.list

        assert list?.size() == 2

        def data = list.get(0)
        assert data.code == "01"
        assert data.name == "中心数据源"
        assert data.kind == DatasourceConfigKindEnum.CENTER.kind
        assert data.config == '{"cloudId":"123"}'

        def data1 = list.get(1)
        assert data1.code == "03"
        assert data1.name == "特定数据源"
        assert data1.kind == DatasourceConfigKindEnum.SPECIALLY.kind
        assert data1.config == '{"cloudId":"1"}'
    }
}
