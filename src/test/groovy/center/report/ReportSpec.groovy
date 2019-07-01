package center.report

import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class ReportSpec extends Specification implements DomainUnitTest<Report> {

    def setup() {
        def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, cloudId: "1")

        Report.Input input = new Report.Input(name: "orgid", caption: "机构", seqNum: 0, dataType: "31", inputType: 3, sqlText: "select id col_value,name col_title from org_list where kind='H'", defType: "我的机构")
        input.datasourceDataCode = "中心数据源"
        report.inputList = [input]

        Report.Style style = new Report.Style(scene: 0, fileUrl: "a837ed3a521b11e6bbd8d017c2930236/xslt/cf5a4c0414ec4cdb9ee79244b1479928/f5750f185c5d4bb5ae80fcb3599ae08e.xslt")
        report.styleList[style]

        Report.Table table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1)
        table.datasourceDataCode = "中心数据源"
        report.tableList = [table]
    }

    def cleanup() {
    }

//    void "新增报表模型-测试1"() {
//        setup: "模拟数据"
//
//    }

    void "删除数据表-测试1"() {
        setup: "参数"
         def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, cloudId: "1", ctrlStatus: 0)
        Report.Table table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1)
        table.datasourceDataCode = "中心数据源"
        report.tableList = [table]

        String name = "table"
        when: "执行"
        report.deleteTable(name)
        then: "结果"
        assert report.tableList.find {
            it.name == name
        } == null
    }

    void "删除数据表-测试2"() {
        setup: "参数"
            def report = new Report(code: "KZRZB", name: "科主任周报", runway: 1, cloudId: "1", ctrlStatus: 0)
            Report.Table table = new Report.Table(name: "table", sqlText: "select A.* from org_list as A,(SELECT CODE FROM org_list as B where id=[orgid]) as C where A.code like CONCAT(C.code,'%')", seqNum: 1)
            table.datasourceDataCode = "中心数据源"
            report.tableList = [table]

            String name = ""
        when: "执行"
            report.deleteTable(name)
        then: "结果"
            assert report.errors.find {
                it.getFieldError("tableList").defaultMessage == "数据表不能为空"
            }
    }
}
