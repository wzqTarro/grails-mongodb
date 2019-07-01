package center.report

import center.report.enst.DatasourceConfigKindEnum
import grails.converters.JSON
import grails.gorm.multitenancy.Tenant
import grails.gorm.multitenancy.Tenants
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mongodb.MongoSpec
import grails.testing.gorm.DomainUnitTest
import org.grails.datastore.mapping.mongo.MongoDatastore
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(ReportDatasource)
//@Mock([Report, Input, ReportModule, Table, Style, ReportData, ReportInputData])
@Ignore
class ReportDatasourceSpec extends MongoSpec {//implements DomainUnitTest<ReportDatasource> {

    def setup() {
        def datasource = new ReportDatasource(code: "02", name: "区域数据源", kind: DatasourceConfigKindEnum.REGION.kind, config: '{"cloudId":"123"}')
        datasource.save(flush: true)
    }

    def cleanup() {
    }

//    @Override
//    protected List<Class> getDomainClasses() {
//        return [Report.class]
//    }

//    @Tenant(value = { "1" }, datastore = MongoDatastore)
//    void "保存数据源-测试1"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
//
//        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, ctrlStatus: 0, grpCode: "01")//, cloudId: "1"
//
//        //report.save(flush: true)
//
//        Input input = new Input(name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构", datasourceDataCode: datasource.code)
//        report.inputList = new ArrayList<>()
//        report.inputList.add(input)
//
//        Table table = new Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1, datasourceDataCode: datasource.code)
//        report.tableList = new ArrayList<>()
//        report.tableList.add(table)
//
//        report.datasourceDataMap = [:]
//        report.datasourceDataMap.put(datasource.code, new DatasourceData(name: datasource.name, code: datasource.code))
//        report.save(flush: true)
//
//        datasource.name = "测试"
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == false
//        assert report.datasourceDataMap."01".name == "测试"
//    }
//
//    void "保存数据源-测试2"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == false
//    }
//
//    void "保存数据源-测试3"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.SPECIALLY.kind, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("config").defaultMessage == "特定区域数据源需要指定区域ID"
//        }
//    }
//
//    void "保存数据源-测试4"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: null, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("kind").defaultMessage == "配置类型缺失"
//        }
//    }
//
//    void "保存数据源-测试5"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: 0, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("kind").defaultMessage == "配置类型有误"
//        }
//    }
//
//    void "保存数据源-测试6"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: null, kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("name") != null
//        }
//    }
//
//    void "保存数据源-测试7"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: null, name: "pk", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("code") != null
//        }
//    }
//
//    void "保存数据源-测试8"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "02", name: "pk", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("code") != null
//        }
//    }
//
//    void "保存数据源-测试9"() {
//        setup: "模拟数据"
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "区域数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":""}')
//        when: "执行"
//        datasource.saveItem()
//        then: "验证"
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("name") != null
//        }
//    }
//
//    void "根据编码查询-测试1"() {
//        given: "参数"
//        String code = "02"
//        when: "执行"
//        def datasource = ReportDatasource.getByCode(code)
//        then: "验证"
//        assert datasource
//    }
//
//    void "根据编码查询-测试2"() {
//        given: "参数"
//        String code = "01"
//        when: "执行"
//        def datasource = ReportDatasource.getByCode(code)
//        then: "验证"
//        assert datasource == null
//    }
//
//    void "删除数据源-测试1"() {
//        setup:
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
//        datasource.saveItem()
//        when:
//        datasource.deleteItem()
//        then:
//        assert datasource == null
//    }
//
//    void "删除数据源-测试2"() {
//        setup: ""
//        ReportDatasource datasource = new ReportDatasource(code: "01", name: "中心数据源", kind: DatasourceConfigKindEnum.CENTER.kind, config: '{"cloudId":"123"}')
//        datasource.saveItem()
//
//        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, cloudId: "1")
//
//        Input input = new Input(name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
//        input.datasourceData = [name: datasource.name, code: datasource.code]
//        report.addInput(input)
//
//        Table table = new Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1)
//        table.datasourceData = [name: datasource.name, code: datasource.code]
//        report.addTable(table)
//        report.save(flush: true)
//        when:
//        datasource.deleteItem()
//        then:
//        assert datasource.hasErrors() == true
//        assert datasource.errors.find {
//            it.getFieldError("code").defaultMessage == "数据源已被使用, 无法删除"
//        }
//    }
}
