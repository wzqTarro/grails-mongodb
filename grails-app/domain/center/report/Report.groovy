package center.report

import center.report.common.CommonValue
import center.report.enst.CtrlKindEnum
import center.report.enst.CtrlStatusEnum
import center.report.enst.ReportSystemParamEnum
import center.report.utils.CommonUtil
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import grails.gorm.transactions.Transactional
import grails.validation.Validateable

/**
 * 报表目录
 */
class Report {//implements MultiTenant<Report> {
    // 编码
    String code
    // 名称
    String name
    // 分组编码
    String grpCode
    // 执行方式
    Integer runway
    // 说明
    String comment
    // 区域Id
    String cloudId
    // 固化报表
    Integer isFixed
    // 管理状态
    Integer ctrlStatus
    // 样式
    List<Style> styleList
    // 输入参数列表
    List<Input> inputList
    // 数据表
    List<Table> tableList
    // 管控记录
    List<CtrlLog> ctrlLogList
    // 数据源冗余数据
    Map<String, String> datasourceDataMap

    static embedded = ['inputList', 'tableList', 'styleList', 'ctrlLogList', 'datasourceDataMap']
    static constraints = {
        code(unique: 'cloudId', blank: false)
        name(unique: 'cloudId', blank: false)
        comment(nullable: true)
        runway(nullable: false, inList: [1, 2])
        cloudId(nullable: true)
        isFixed(nullable: true)
        ctrlStatus(nullable: false, inList: [0, 1, -1, 2, -2]) // 0-编制中草稿，1-提请审核，-1-审核退回，2-审核通过，-2-停止使用

        inputList(nullable: true)
        styleList(nullable: true)
        tableList(nullable: true)
        ctrlLogList(nullable: true)
        datasourceDataMap(nullable: true)

        inputList validator: { List<Input> vals, Report obj, errors ->
            for (def input: vals){
                String name = input.name
                if (!name) {
                    errors.rejectValue("inputList", "name", "参数名称不能为空")
                    return
                }
                // 参数名称唯一验证
                def other = obj.inputList.findAll { otherInput ->
                    otherInput.name == name
                }
                if (other?.size() > 1) {
                    errors.rejectValue("inputList", "unique", "参数名称已存在")
                    return
                }

                // 参数标签
                String caption = input.caption
                if (!caption) {
                    errors.rejectValue("inputList", "caption", "参数标签不能为空")
                    return
                }

                // 数据类型
                String dataType = input.dataType
                if (!dataType) {
                    errors.rejectValue("inputList", "dataType", "参数数据类型不能为空")
                    return
                }

                // 数据源
                String datasourceDataCode = input.datasourceDataCode
                if (!datasourceDataCode) {
                    errors.rejectValue("inputList", "datasourceData", "参数数据源不能为空")
                    return
                }
                if (!obj.datasourceDataMap) {
                    obj.datasourceDataMap = [:]
                }
                if (!obj.datasourceDataMap.containsKey(datasourceDataCode)) {
                    ReportDatasource datasource = ReportDatasource.getByCode(datasourceDataCode)
                    if (!datasource) {
                        errors.rejectValue("inputList", "datasourceData", "参数数据源有误")
                        return
                    }
                    obj.datasourceDataMap.put(datasource.code, datasource.name)
                }

                String sql = input.sqlText
                if (sql) {
                    // 输入参数动态查询参数解析
                    List<String> paramList = CommonUtil.analysisSql(sql)

                    for (def param: paramList){
                        // 去掉[]，获取参数名称
                        String paramName = param[param.indexOf(CommonValue.PARAM_PREFIX) + 1..param.indexOf(CommonValue.PARAM_SUFFIX) - 1]

                        // 系统参数
                        ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)

                        // 输入参数查询语句中的参数只能是系统参数
                        if (!systemParamEnum) {
                            errors.rejectValue("inputList", "sqlText", "动态查询参数sql错误")
                            return
                        }
                    }
                }

                if (!input.validate()) {
                    for (def err: input.errors.allErrors) {
                        errors.rejectValue("inputList", err.code, err.arguments, err.defaultMessage)
                        return
                    }
                }
            }
        }

        styleList validator: { List<Style> vals, Report obj, errors ->
            for (def style: vals) {
                Integer scene = style.scene
                if (scene < 0) {
                    errors.rejectValue("styleList", "not.null.scene", "场景不能为空")
                    return
                }
                // 参数名称唯一验证
                def other = obj.styleList.findAll { otherStyle ->
                    otherStyle.scene == scene
                }
                if (other?.size() > 1) {
                    errors.rejectValue("styleList", "not.unique.scene", "场景已存在")
                    return
                }
                // 文件地址
                def fileUrl = style.fileUrl
                if (!fileUrl) {
                    errors.rejectValue("styleList", "error", "文件不能为空")
                    return
                }
                if (!style.validate()) {
                    for (def err: style.errors.allErrors) {
                        errors.rejectValue("styleList", err.code, err.arguments, err.defaultMessage)
                        return
                    }
                }
            }
        }

        tableList validator: { List<Table> vals, Report obj, errors ->
            for (def table: vals) {
                String name = table.name
                if (!name) {
                    errors.rejectValue("tableList", "name", "数据表名称不能为空")
                    return
                }
                // 参数名称唯一验证
                def other = obj.tableList.findAll { otherTable ->
                    otherTable.name == name
                }
                if (other?.size() > 1) {
                    errors.rejectValue("tableList", "unique", "数据表名称已存在")
                    return
                }
                // 数据源
                String datasourceDataCode = table.datasourceDataCode
                if (!datasourceDataCode) {
                    errors.rejectValue("tableList", "datasourceData", "数据表数据源不能为空")
                    return
                }
                if (!obj.datasourceDataMap) {
                    obj.datasourceDataMap = [:]
                }
                if (!obj.datasourceDataMap.containsKey(datasourceDataCode)) {
                    ReportDatasource datasource = ReportDatasource.getByCode(datasourceDataCode)
                    if (!datasource) {
                        errors.rejectValue("tableList", "datasourceData", "数据表数据源有误")
                        return
                    }
                    obj.datasourceDataMap.put(datasource.code, datasource.name)
                }
                if (!table.validate()) {
                    for (def err: table.errors.allErrors) {
                        errors.rejectValue("tableList", err.code, err.arguments, err.defaultMessage)
                        return
                    }
                }
            }
        }
    }
    static mapping = {
//        tenantId name: 'cloudId'

        // 关闭缓存
        stateless true
    }
    /**
     * 输入参数-报表内嵌类，禁止当做领域对象使用直接
     */
    static class Input implements Validateable{
        // 名称
        String name
        // 参数标签
        String caption
        // 排列号
        Integer seqNum
        // 数据类型
        String dataType
        // 输入类型
        Integer inputType
        // 选项列表
        String optionList
        // 查询语句
        String sqlText
        // 默认值文本
        String defValue
        // 默认值类型
        String defType
        // 数据源编码
        String datasourceDataCode

        static constraints = {
            seqNum(nullable: true, min: 0)
            dataType(inList: ['11', '12', '21', '22', '23', '31', '32', '33'])
            inputType(inList: [0, 1, 2, 3])
            optionList(nullable: true)
            defValue(nullable: true)
            defType(nullable: true)
            sqlText(nullable: true)
        }
    }


    /**
     * 样式-报表内嵌类，禁止当做领域对象使用直接
     */
    static class Style implements Validateable {
        // 场景
        Integer scene
        // 文件URL
        String fileUrl
        // 图标设置
        String chart
        // 说明
        String comment

        static constraints = {
            scene(inList: [0, 1, 2, 3])
            fileUrl(nullable: false)
            chart(nullable: true)
            comment(nullable: true)
        }
    }

/**
 * 数据表-报表内嵌类，禁止当做领域对象使用直接
 */
    static class Table implements Validateable {
        // 名称
        String name
        // 查询语句
        String sqlText
        // 数据源编码
        String datasourceDataCode
        // 排列号
        Integer seqNum

        static constraints = {
            seqNum(nullable: true, min: 0)
            sqlText(nullable: true)
        }

        def asType(Class target) {
            if (target == Map) {
                return [name: this.name, datasourceData: [code: this.datasourceData.code, name: this.datasourceData.name], sqlText: this.sqlText, seqNum: this.seqNum]
            }
            return null
        }
    }

/**
 * 管控记录-报表内嵌类，禁止当做领域对象使用直接
 */
    static class CtrlLog implements Validateable {
        // 原状态
        Integer preStatus
        // 管控性质
        String ctrlKind
        // 附加说明
        String adscript
        // 区域云ID
        String cloudId
        // 现状态
        Integer newStatus
        // 记录时间
        Date logTime
        // 账户ID
        String accountId
        // 账户姓名
        String accountName

        static constraints = {
            cloudId(nullable: true)
            adscript(nullable: true)
        }
    }

    /**
     * 保存
     * @return
     */
    @Transactional
    void saveItem() {
        if (!this.cloudId) {
            this.cloudId = ""
        }
        if (!this.id) {
            this.ctrlStatus = CtrlStatusEnum.BZZCY.code
        }
        if (this.inputList) {
            this.inputList.sort{ a, b ->
                if (a.seqNum == null) {
                    a.seqNum = 0
                }
                if (b.seqNum == null) {
                    b.seqNum = 0
                }
                return a.seqNum - b.seqNum
            }
        }
        if (this.tableList) {
            this.tableList.sort{ a, b ->
                if (a.seqNum == null) {
                    a.seqNum = 0
                }
                if (b.seqNum == null) {
                    b.seqNum = 0
                }
                return a.seqNum - b.seqNum
            }
        }
        if (!this.validate()) {
            return
        }
        boolean thisFlag = this.hasChanged()
        boolean inputFlag = this.hasChanged("inputList")
        this.save(flush: true)
        if (thisFlag) {
            /**
             * TODO 通知模块修改输入参数
             */
            //ReportModule.updateReportLink(this.id, this.name, this.code)
        }
        if (inputFlag) {
            /**
             * TODO 通知模块修改输入参数
             */
            //ReportModule.updateReportLinkInput(this.id, this.inputList)
        }
    }

    /**
     * 提交
     */
    void submit(String adscript, String accountId, String accountName) {
        // 报表提交审核，原状态必须为草稿状态
        if (CtrlStatusEnum.BZZCY.code != this.ctrlStatus) {
            this.errors.rejectValue("ctrlLogList", "error", "当前报表不是草稿状态")
            return
        }
        CtrlLog ctrlLog = new CtrlLog()
        ctrlLog.adscript = adscript
        ctrlLog.ctrlKind = CtrlKindEnum.SUBMIT.kind
        // 原状态
        ctrlLog.preStatus = this.ctrlStatus
        // 管理状态
        this.ctrlStatus = CtrlStatusEnum.SUBMIT_AUDIT.code
        // 现状态
        ctrlLog.newStatus = CtrlStatusEnum.SUBMIT_AUDIT.code
        ctrlLog.cloudId = this.cloudId
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = accountId
        ctrlLog.accountName = accountName

        if (!this.ctrlLogList) {
            this.ctrlLogList = new LinkedList<>()
        }
        this.ctrlLogList.add(0, ctrlLog)
        this.save(flush: true)
    }

    /**
     * 停用、审核
     * @param ctrlLogKind
     * @param reportCtrlStatus
     * @param reportId
     * @param adscript
     * @return
     */
    private def commonJudge(CtrlKindEnum ctrlLogKind, CtrlStatusEnum reportCtrlStatus, String adscript, String accountId, String accountName) {
        // 审核状态
        if (CtrlKindEnum.AUDIT == ctrlLogKind) {
            if (CtrlStatusEnum.SUBMIT_AUDIT.code != this.ctrlStatus) {
                this.errors.rejectValue("tableList", "error", "当前报表需要提交审核")
                return this
            }
        }
        CtrlLog ctrlLog = new CtrlLog()
        ctrlLog.ctrlKind = ctrlLogKind.kind
        // 原状态
        ctrlLog.preStatus = this.ctrlStatus
        // 管理状态
        this.ctrlStatus = reportCtrlStatus.code
        // 现状态
        ctrlLog.newStatus = reportCtrlStatus.code
        ctrlLog.cloudId = this.cloudId
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = accountId
        ctrlLog.accountName = accountName
        ctrlLog.adscript = adscript

        if (!this.ctrlLogList) {
            this.ctrlLogList = new LinkedList<>()
        }
        this.ctrlLogList.add(0, ctrlLog)
        this.save(flush: true)
        return this
    }
    /**
     * 停用
     * @param reportId
     * @param cloudId
     * @param adscript 附加说明
     * @return
     */
    def stop(String adscript, String accountId, String accountName) {
        return commonJudge(CtrlKindEnum.STOP_USING, CtrlStatusEnum.STOP_USING, adscript, accountId, accountName)
    }
    /**
     * 审核通过
     * @param reportId
     * @param cloudId
     * @param adscript
     */
    def auditSuccess(String adscript, String accountId, String accountName) {
        return commonJudge(CtrlKindEnum.AUDIT, CtrlStatusEnum.AUDIT_SUCCESS, adscript, accountId, accountName)
    }
    /**
     * 审核退回
     * @param reportId
     * @param cloudId
     * @param adscript
     */
    def auditRollback(String adscript, String accountId, String accountName) {
        return commonJudge(CtrlKindEnum.AUDIT, CtrlStatusEnum.AUDIT_ERROR, adscript, accountId, accountName)
    }
    /**
     * 重启用
     * @param reportId
     * @param adscript
     * @return
     */
    void reboot(String adscript, String accountId, String accountName) {
        // 管理状态
        if (this.ctrlStatus != CtrlStatusEnum.STOP_USING.code) {
            this.errors.rejectValue("tableList", "error", "报表未停用")
            return
        }
        // 最新停用记录
        if (!this.ctrlLogList || this.ctrlLogList.size() < 2) {
            this.errors.rejectValue("tableList", "error", "停用记录为空，数据有误")
            return
        }
        CtrlLog stopCtrlLog = this.ctrlLogList.get(0)
        CtrlLog preStopCtrlLog = this.ctrlLogList.get(1)

        CtrlLog ctrlLog = new CtrlLog()
        // 停用前管理日志状态
        ctrlLog.ctrlKind = preStopCtrlLog.ctrlKind
        // 原状态
        ctrlLog.preStatus = this.ctrlStatus
        // 停用前报表状态状态
        this.ctrlStatus = stopCtrlLog.preStatus
        // 现状态
        ctrlLog.newStatus = stopCtrlLog.preStatus
        ctrlLog.cloudId = this.cloudId
        ctrlLog.adscript = adscript
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = accountId
        ctrlLog.accountName = accountName

        this.ctrlLogList.add(0, ctrlLog)
        this.save(flush: true)
    }
    /**
     * 将区域报表转为中心报表
     * @param reportId
     * @param adscript
     * @return
     */
    void toCenterReport(String adscript, String accountId, String accountName) {
        CtrlLog ctrlLog = new CtrlLog()
        ctrlLog.ctrlKind = CtrlKindEnum.TO_CENTER.kind
        // 原状态
        ctrlLog.preStatus = this.ctrlStatus
        // 现状态
        ctrlLog.newStatus = this.ctrlStatus
        ctrlLog.cloudId = this.cloudId
        this.cloudId = ""
        ctrlLog.adscript = adscript
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = accountId
        ctrlLog.accountName = accountName

        if (!this.ctrlLogList) {
            this.ctrlLogList = new LinkedList<>()
        }
        this.ctrlLogList.add(0, ctrlLog)
        this.save(flush: true)
    }
    /**
     * 取消中心共享
     * @param reportId
     * @param cloudId
     * @param adscript
     * @return
     */
    void cancelCenterReport(String cloudId, String adscript, String accountId, String accountName) {
        if (!cloudId) {
            this.errors.rejectValue("tableList", "error", "区域云标识不能为空")
            return
        }

        CtrlLog ctrlLog = new CtrlLog()
        ctrlLog.ctrlKind = CtrlKindEnum.CANCEL_CENTER.kind
        // 原状态
        ctrlLog.preStatus = this.ctrlStatus
        // 现状态
        ctrlLog.newStatus = this.ctrlStatus
        ctrlLog.cloudId = cloudId
        this.cloudId = cloudId
        ctrlLog.adscript = adscript
        ctrlLog.logTime = new Date()
        ctrlLog.accountId = accountId
        ctrlLog.accountName = accountName

        if (!this.ctrlLogList) {
            this.ctrlLogList = new LinkedList<>()
        }
        this.ctrlLogList.add(0, ctrlLog)
        this.save(flush: true)
    }

    /**
     * 条件查询
     * @param params
     * @return
     */
    static List<Report> getByCondition(String grpCode, String cloudId, String rptName, Integer ctrlStatus, Integer pageNow, Integer pageSize) {
        return Report.createCriteria().list {
                and {
                    if (grpCode) {
                        eq("grpCode", grpCode)
                    }
                    if (cloudId) {
                        eq("cloudId", cloudId)
                    }
//                    if (isFixed > -1) {
//                        eq("isFixed", isFixed)
//                    }
                    if (rptName) {
                        like("name", "%"+rptName+"%")
                    }
                    if (ctrlStatus > -1) {
                        eq("ctrlStatus", ctrlStatus)
                    }
                }
                order "code", "asc"
                if (pageNow > -1 && pageSize > -1) {
                    firstResult(pageNow * pageSize)
                    maxResults(pageSize)
                }
        }
    }

    /**
     * 条件获取数量
     * @param params
     * @return
     */
    static Integer countByCondition(String grpCode, String cloudId, String rptName, Integer ctrlStatus) {
        return Report.createCriteria().count{
            and {
                if (grpCode) {
                    eq("grpCode", grpCode)
                }
                if (cloudId) {
                    eq("cloudId", cloudId)
                }
//                if (isFixed > -1) {
//                    eq("isFixed", isFixed)
//                }
                if (rptName) {
                    eq("name", rptName)
                }
                if (ctrlStatus > -1) {
                    eq("ctrlStatus", ctrlStatus)
                }
            }
            order "code", "asc"
        }
    }

    /**
     * 更新数据源冗余数据
     * @param code
     */
    static void updateDatasourceData(String code, String name) {
        if (!code) {
            return
        }
        if (!name) {
            return
        }
        Report.collection.updateMany(
                Filters.exists("datasourceDataMap.$code"),
                Updates.set("datasourceDataMap.${code}", name)
        )
    }

    /**
     * 是否使用指定数据源
     * @return
     */
    static boolean isHasDatasourceCode(String datasourceCode) {
        List list = Report.collection.find(
                Filters.or(
                        Filters.eq("inputList.datasourceDataCode", datasourceCode),
                        Filters.eq("tableList.datasourceDataCode", datasourceCode)
                )
        ).toList()
        return list.size()>0?true:false
    }
    /**
     * 根据输入参数名称获取输入参数
     * @param name
     * @return
     */
    static Input getInputByName(Long rptId, String name) {
        Report report = Report.get(rptId)
        if (!report) {
            return null
        }
        return report.inputList?.find{
            it.name == name
        }
    }

    /**
     * 筛选数据表
     * @param tableNames
     * @return
     */
    Report screenTable(List<String> tableNames) {
        if (!tableNames) {
            this.errors.rejectValue("tableList", "error", "数据表名不能为空")
            return this
        }
        if (!this.tableList) {
            return this
        }
        tableNames.each { name ->
            def table = this.tableList.find{
                it.name == name
            }
            this.tableList.remove(table)
        }
        return this
    }
}



