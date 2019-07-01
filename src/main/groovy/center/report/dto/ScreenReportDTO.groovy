package center.report.dto

import grails.validation.Validateable

/**
 * 大屏报表数据请求参数
 */
class ScreenReportDTO implements Serializable, Validateable{
    // 数据表名
    List<String> tableNames;
    // 参数值
    List<ReportParamValueDTO> paramValues;
}
