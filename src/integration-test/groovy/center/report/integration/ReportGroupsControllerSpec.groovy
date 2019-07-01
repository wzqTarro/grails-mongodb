package center.report.integration

import center.report.ReportGroups
import center.report.ReportGroupsController
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
class ReportGroupsControllerSpec extends Specification {
    @Autowired
    MessageSource messageSource

    @Autowired
    WebApplicationContext ctx

    def setupSpec() {
        Locale.setDefault(Locale.CHINA)
    }

    def setup() {
        grails.util.GrailsWebMockUtil.bindMockWebRequest(ctx)
        def group3 = new ReportGroups(code:"99", name:"监控大屏", comment:"内置专门存放监控大屏报表的分组");
        group3.save(flush: true);
        def group4 = new ReportGroups(code:"88", name:"客户行为", comment:"行为");
        group4.save(flush: true);
    }

    def cleanup() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes()
        ReportGroups.collection.deleteMany([:])
    }

    void "controller新增报表分组-测试1"() {
        given:
        def group = new ReportGroups(code: "03", name:"质量管理", comment:"说明")
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1
    }

    void "controller新增报表分组-测试2"() {
        given:
        def group = new ReportGroups(code: null, name:"质量管理", comment:"说明")
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.code.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller新增报表分组-测试3"() {
        given:
        def group = new ReportGroups(code: "03", name:"", comment:"说明")
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller新增报表分组-测试4"() {
        given:
        def group = new ReportGroups(code: "99", name:"质量管理", comment:"说明")
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.code.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller新增报表分组-测试5"() {
        given:
        def group = new ReportGroups(code: "03", name:"监控大屏", comment:"说明")
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller编辑报表分组-测试1"() {
        given:
        def group = ReportGroups.findByCode("99")
        group.name = "测试"
        group.comment = "测试说明"
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1
        when:
        def testGroups = ReportGroups.findByCode("99")
        then:
        assert testGroups?.comment == "测试说明"
        assert testGroups?.name == "测试"
    }

    void "controller编辑报表分组-测试2"() {
        given:
        def group = ReportGroups.findByCode("99")
        group.name = ""
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller编辑报表分组-测试3"() {
        given:
        def group = ReportGroups.findByCode("99")
        group.code = ""
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.code.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.null.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller编辑报表分组-测试4"() {
        given:
        def group = ReportGroups.findByCode("99")
        group.name = "客户行为"
        when:
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.save(group)
        then:
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 3

        def errAttribute = messageSource.getMessage("reportGroups.name.label", null, Locale.CHINA)
        def errClass = messageSource.getMessage("reportGroups.label", null, Locale.CHINA)
        assert jsonData?.message == (messageSource.getMessage("default.not.unique.message", [errAttribute, errClass] as Object[], Locale.CHINA) + ";")
    }

    void "controller根据名称或编码查询分组列表-测试1"() {
        given: "模拟数据"
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");

        group1.save(flush: true);
        group2.save(flush: true);

        when:"执行"
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.getListByCondition()
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1

        def list = jsonData.list
        assert list?.size() == 4

        def data = list.get(0)
        assert data.code == "01"
        assert data.name == "运营简报"
        assert data.comment == "提现整体经营服务规模效果效益等内容的报表"

        def data1 = list.get(1)
        assert data1.code == "02"
        assert data1.name == "服务管理"
        assert data1.comment == "有关服务工作开展情况和开展内容等信息的呈现"

        def data2 = list.get(3)
        assert data2.code == "99"
        assert data2.name == "监控大屏"
        assert data2.comment == "内置专门存放监控大屏报表的分组"

        def data3 = list.get(2)
        assert data3.code == "88"
        assert data3.name == "客户行为"
        assert data3.comment == "行为"
    }

    void "controller根据名称或编码查询分组列表-测试2"() {
        given: "模拟数据"
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");
        group1.save(flush: true);
        group2.save(flush: true);
        when:"执行"
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.params["code"] = "01"
        controller.getListByCondition()
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data = list.get(0)
        assert data.code == "01"
        assert data.name == "运营简报"
        assert data.comment == "提现整体经营服务规模效果效益等内容的报表"
    }

    void "controller根据名称或编码查询分组列表-测试3"() {
        given: "模拟数据"
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");
        group1.save();
        group2.save(flush: true);
        when:"执行"
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.params["name"] = "服务管理"
        controller.getListByCondition()
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1

        def list = jsonData.list
        assert list?.size() == 1

        def data1 = list.get(0)
        assert data1.code == "02"
        assert data1.name == "服务管理"
        assert data1.comment == "有关服务工作开展情况和开展内容等信息的呈现"
    }

    void "controller根据名称或编码查询分组列表-测试4"() {
        given: "模拟数据"
        def group1 = new ReportGroups(code:"01", name:"运营简报", comment:"提现整体经营服务规模效果效益等内容的报表");
        def group2 = new ReportGroups(code:"02", name:"服务管理", comment:"有关服务工作开展情况和开展内容等信息的呈现");
        group1.save();
        group2.save(flush: true);
        when:"执行"
        ReportGroupsController controller = new ReportGroupsController()
        controller.messageSource = messageSource
        controller.params["name"] = "服务管理"
        controller.params["code"] = "01"
        controller.getListByCondition()
        then:"结果"
        HttpServletResponse response = controller.response
        assert response.status == 200
        assert response.getHeader("Content-Type") == "application/json;charset=UTF-8"

        def jsonData = JSON.parse(response.content.toString())
        assert jsonData?.code == 1

        def list = jsonData.list
        assert list?.size() == 0
    }
}
