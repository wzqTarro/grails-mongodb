package center.report.integration

import center.report.ReportModuleInput
import center.report.ReportModuleInputController
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
class ReportModuleInputControllerSpec extends Specification {

    @Autowired
    MessageSource messageSource

    @Autowired
    WebApplicationContext ctx

    def setup() {
        grails.util.GrailsWebMockUtil.bindMockWebRequest(ctx)

        ReportModuleInput module = new ReportModuleInput(moduleId: "01", moduleName: "分科随访管理", slotKind: 1, comment: "说明", inputList: [
                new ReportModuleInput.ModuleInput(name: "cur_dept_id", caption: "当前部门ID", dataType: "S", comment: "模块参数说明")
        ])
        module.save(flush: true)
    }
    def cleanup() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes()
        ReportModuleInput.collection.deleteMany([:])
    }

    void "根据模块ID查询-测试1"() {
        given:
        String moduleId = "01"
        when:
        ReportModuleInputController controller = new ReportModuleInputController()
        controller.messageSource = messageSource
        controller.show(moduleId)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1

        def data = jsonData.one
        assert data.moduleId == "01"
        assert data.moduleName == "分科随访管理"
        assert data.slotKind == 1
        assert data.comment == "说明"

        def inputList = data.inputList
        assert inputList.size() == 1

        def input = inputList.get(0)
        assert input.name == "cur_dept_id"
        assert input.caption == "当前部门ID"
        assert input.dataType == "S"
        assert input.comment == "模块参数说明"
    }

    void "根据名称查询-测试1"() {
        given:
        String moduleName = "分科"
        when:
        ReportModuleInputController controller = new ReportModuleInputController()
        controller.messageSource = messageSource
        controller.getByCondition(moduleName)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1

        def list = jsonData.list
        assert list.size() == 1

        def data = list.get(0)
        assert data.moduleId == "01"
        assert data.moduleName == "分科随访管理"
        assert data.slotKind == 1
        assert data.comment == "说明"

        def inputList = data.inputList
        assert inputList.size() == 1

        def input = inputList.get(0)
        assert input.name == "cur_dept_id"
        assert input.caption == "当前部门ID"
        assert input.dataType == "S"
        assert input.comment == "模块参数说明"
    }

    void "根据名称查询-测试2"() {
        given:
        String moduleName = "02"
        when:
        ReportModuleInputController controller = new ReportModuleInputController()
        controller.messageSource = messageSource
        controller.getByCondition(moduleName)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1

        def list = jsonData.list
        assert list?.size() == 0
    }

    void "新增模块参数-测试1"() {
        given:
        ReportModuleInput module = new ReportModuleInput(moduleId: "02", moduleName: "集中随访管理", slotKind: 1, comment: "说明",
                inputList: [
                        new ReportModuleInput.ModuleInput(name: "cur_dept_id", caption: "当前部门ID", dataType: "S", comment: "模块参数说明")
                ]
        )
        when:
        ReportModuleInputController controller = new ReportModuleInputController()
        controller.messageSource = messageSource
        controller.save(module)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModuleInput test = ReportModuleInput.findByModuleId("02")
        then:
        test.moduleId == "02"
        test.moduleName == "集中随访管理"
        test.slotKind == 1
        test.comment == "说明"

        test.inputList?.size() == 1
        def input = test.inputList.get(0)
        assert input.name == "cur_dept_id"
        assert input.caption == "当前部门ID"
        assert input.dataType == "S"
        assert input.comment == "模块参数说明"
    }

    void "编辑模块参数-测试1"() {
        given:
        ReportModuleInput reportModuleInput = ReportModuleInput.getByModuleId("01")
        reportModuleInput.inputList = [
                new ReportModuleInput.ModuleInput(name: "cur_dept_jobs", caption: "当前工作类别", dataType: "S", comment: "编辑模块参数说明")
        ]
        when:
        ReportModuleInputController controller = new ReportModuleInputController()
        controller.messageSource = messageSource
        controller.save(reportModuleInput)
        then:
        HttpServletResponse response = controller.response
        response.getHeader("Content-Type") == "application/json;charset=UTF-8"
        assert response.status == 200
        def jsonData = JSON.parse(response.content.toString())

        assert jsonData.code == 1
        when:
        ReportModuleInput test = ReportModuleInput.findByModuleId("01")
        then:
        assert test.moduleId == "01"
        assert test.moduleName == "分科随访管理"
        assert test.slotKind == 1
        assert test.comment == "说明"
        test.inputList?.size() == 1
        def input = test.inputList.get(0)
        assert input.name == "cur_dept_jobs"
        assert input.caption == "当前工作类别"
        assert input.dataType == "S"
        assert input.comment == "编辑模块参数说明"
    }
}
