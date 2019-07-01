package center.report

import center.report.common.CommonValue
import center.report.common.ReportXmlConstant
import center.report.dto.DatasourceConfigDTO
import center.report.dto.InputValueDTO
import center.report.dto.QueryInputValueDTO
import center.report.dto.ReportParamValueDTO
import center.report.dto.TableParamDTO
import center.report.dto.XmlDataDTO
import center.report.enst.DatasourceConfigKindEnum
import center.report.enst.QueryInputDefTypeEnum
import center.report.enst.ReportSystemParamEnum
import center.report.utils.CommonUtil
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

import java.sql.ResultSetMetaData

@Transactional
class DataService {

//    @Autowired
//    IDataCenterClient dataCenterClient

    /**
     * 获取报表显示数据
     * @param report
     * @param usedParamList
     * @return
     */
    @Transactional(readOnly = true)
    XmlDataDTO getTableData(Report report, List<ReportParamValueDTO> usedParamList) {
        if (!report) {
            return null
        }
        List<Report.Table> tableList = report.tableList
        if (!tableList) {
            return null
        }
        // 所有涉及到的参数列表
        Map<String, ReportParamValueDTO> paramNameMap = new HashMap<>()
        usedParamList?.each { param ->
            paramNameMap.put(param.name, param)
        }
        List<TableParamDTO> tableParamDTOList = new ArrayList<>()
        tableList.each { Report.Table table ->
            TableParamDTO tableParamDTO = new TableParamDTO()
            tableParamDTO.reportTables = table

            // 数据源编码
            String datasourceCode = table.datasourceData.code
            // 数据源
            ReportDatasource datasource = ReportDatasource.getByCode(datasourceCode)
            // 数据源类型
            Integer kind = datasource.kind
            // 数据源配置
//            String config = datasource.config
//            def configMap = new JsonSlurper().parseText(config)
//            DatasourceConfigDTO datasourceConfig = new DatasourceConfigDTO(configMap)

            List<String> columnNameList = new ArrayList()
            Set<String> paramNameSet = new HashSet<>()
            // 不是数据中心实表查询
            if (DatasourceConfigKindEnum.DATA_CENTER_REAL_TABLE.kind != kind) {
                /** 解析查询语句中的参数 **/
                // sql语句
                String sql = table.sqlText

                // 输入参数动态查询参数解析
                List<String> paramList = CommonUtil.analysisSql(sql)

                // 参数列表
                List<String> paramValues = new ArrayList<>()

                // 遍历参数
                paramList.each { param ->
                    // 将sql中参数替换为？，变为可执行参数
                    sql = sql.replace(param, "?")
                    // 去掉[]，获取参数名称
                    String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]
                    paramNameSet.add(paramName)

                    if (paramNameMap.containsKey(paramName)) {
                        paramValues.add(paramNameMap.get(paramName).value)
                        return
                    }

                    // 系统参数
                    ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
                    // 参数为系统参数
                    if (systemParamEnum) {
                        // 取默认值
                        ReportParamValueDTO reportParamValue = CommonUtil.getSystemParamValue()
                        if (!reportParamValue) {
                            return
                        }

                        paramNameMap.put(paramName, reportParamValue)
                        paramValues.add(reportParamValue.value)
                    } else {

                        // 查询对应输入参数
                        List reportInputs = report.getInputByName(paramName)

                        // 取默认值
                        ReportParamValueDTO reportParamValue = CommonUtil.getInputParamValue(QueryInputDefTypeEnum.getEnumByDefType(reportInputs.defType))
                        if (!reportParamValue) {
                            return
                        }

                        paramNameMap.put(paramName, reportParamValue)
                        paramValues.add(reportParamValue.value)
                    }
                }
                /**
                 * TODO
                 */
                List rows = null
                // 非数据中心虚表查询
                if (DatasourceConfigKindEnum.DATA_CENTER_VIRTUAL_TABLE.kind != kind) {
                    def grailsApplication = ((GrailsWebRequest) RequestContextHolder.currentRequestAttributes()).getAttributes().getGrailsApplication()
                    // 查询结果
                    def url = grailsApplication.config.getProperty("select.dataSource.url")
                    def user = grailsApplication.config.getProperty("select.dataSource.user")
                    def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
                    def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
                    // 执行sql
                    Sql db = Sql.newInstance(url,
                            user, pwd,
                            driverClassName)
                    rows = db.rows(sql, paramValues, { ResultSetMetaData result ->
                        // 字段名
                        int count = result.getColumnCount()
                        for (i in 1..count) {
                            def name = result.getColumnName(i)
                            String key = CommonUtil.toHumpStr(name)
                            columnNameList.add(key)
                        }
                    })
                } else {
//                    rows = dataCenterClient.getVirtualData(sql, paramValues)
//                    if (rows) {
//                        def value = rows.get(0)
//                        value.each { k, v ->
//                            columnNameList.add(k)
//                        }
//                    }
                }
                tableParamDTO.rowList = rows
                tableParamDTO.columnNameList = columnNameList
                tableParamDTO.paramNameSet = paramNameSet
            } else { // 数据中心
                // 开始时间
//                def startTimeParam = paramNameMap.get("startWith")
//                // 结束时间
//                def endTimeParam = paramNameMap.get("endWith")
//                String startTime = startTimeParam?.value
//                String endTime = endTimeParam?.value
//
//
//                if (startTime && endTime) {
//                   tableParamDTO.rowList = dataCenterClient.getData(startTime, endTime, table.name, new HashMap<String, Object>())
//                    if (tableParamDTO.rowList) {
//                        def value = tableParamDTO.rowList.get(0)
//                       value.each { k, v ->
//                            columnNameList.add(k)
//                        }
//                    }
//                }
//                tableParamDTO.columnNameList = columnNameList
//                tableParamDTO.paramNameSet = paramNameSet
            }
            tableParamDTOList.add(tableParamDTO)
        }
        // 参数列表
        List<ReportParamValueDTO> paramList = new ArrayList<>()
        paramList.addAll(paramNameMap.values())

        XmlDataDTO xmlData = new XmlDataDTO()
        xmlData.paramValueList = paramList
        xmlData.tableParamDTOList = tableParamDTOList
        return xmlData
    }

    /**
     * 获取输入参数显示数据
     * @param report
     * @param inputName
     */
    QueryInputValueDTO getInputData(Report report, String inputName) {
        QueryInputValueDTO queryInputValueDTO = new QueryInputValueDTO()

        Report.Input input = report.getInputByName(inputName)
        if (!input) {
            report.errors.rejectValue("inputList", "error", "输入参数不存在")
        }
        String sql = null
        sql = input.sqlText
        if (!sql) {
            report.errors.rejectValue("inputList", "error", "动态查询参数sql为空")
            return
        }

        // 输入参数动态查询参数解析
        List<String> paramList = CommonUtil.analysisSql(sql)
        // 系统参数列表
        List<String> sysParamValues = new ArrayList<>()
        paramList.each { param ->
            // 将sql中参数替换为？，变为可执行参数
            sql = sql.replace(param, "?")
            // 去掉[]，获取参数名称
            String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

            // 系统参数
            ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)

            // 获取系统参数的值
            ReportParamValueDTO reportParamValue = CommonUtil.getSystemParamValue(paramName)
            sysParamValues.add(reportParamValue.value)
        }

        // 默认值类型
        String defValue = input.defValue
        QueryInputDefTypeEnum defTypeEnum = QueryInputDefTypeEnum.getEnumByDefType(input.defType)
        if (defTypeEnum) {
            // 页面显示文本及其值
            ReportParamValueDTO reportParamValue = CommonUtil.getInputParamValue(defTypeEnum)
            if (!reportParamValue) {
                defValue = input.defValue
            } else {
                defValue = reportParamValue.value
            }
        }
        if (!defValue) {
            defValue = ""
        }
        queryInputValueDTO.defValue = defValue

        // 数据源
        String  datasourceDataCode = input.datasourceDataCode
        ReportDatasource datasource = ReportDatasource.getByCode(datasourceDataCode)
        Integer kind = datasource.kind

        if (DatasourceConfigKindEnum.REGION.kind == kind) {
            /**
             * TODO 执行sql获取查询结果
             */
            def url = grailsApplication.config.getProperty("select.dataSource.url")
            def user = grailsApplication.config.getProperty("select.dataSource.user")
            def pwd = grailsApplication.config.getProperty("select.dataSource.pwd")
            def driverClassName = grailsApplication.config.getProperty("select.dataSource.driverClassName")
            // 执行sql
            Sql db = Sql.newInstance(url,
                    user, pwd,
                    driverClassName)

            // 查询结果
            List rowList = db.rows(sql, sysParamValues)

            // 前端显示的文本及其对应值
            List<InputValueDTO> inputValueVOList = new ArrayList<>()
            rowList.each { row ->
                // 值
                def value = row.getProperty(ReportXmlConstant.QUERYINPUT_VALUE)
                // 显示文本
                def title = row.getProperty(ReportXmlConstant.QUERYINPUT_TITLE)

                InputValueDTO inputValueDTO = new InputValueDTO()
                inputValueDTO.colTitle = title
                inputValueDTO.colValue = value
                inputValueVOList.add(inputValueDTO)
            }
            queryInputValueDTO.inputValueDTOS = inputValueVOList
        }
        return queryInputValueDTO
    }
}