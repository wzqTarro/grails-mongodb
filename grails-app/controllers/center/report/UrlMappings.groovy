package center.report

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"edit")
        patch "/$controller/$id(.$format)?"(action:"patch")

        /* 数据源 */
        get "/reportDatasource/condition"(controller: "reportDatasource", action: "getByCondition")

        /* 分组 */
        post "/reportGroups/condition"(controller: "reportGroups", action: "getListByCondition")

        /* 报表 */
        post "/report/condition"(controller: "report", action: "getByCondition")
        post "/report/submit"(controller: "report", action: "submit")
        post "/report/stop"(controller: "report", action: "stop")
        post "/report/auditSuccess"(controller: "report", action: "auditSuccess")
        post "/report/auditRollback"(controller: "report", action: "auditRollback")
        post "/report/reboot"(controller: "report", action: "reboot")
        post "/report/toCenterReport"(controller: "report", action: "toCenterReport")
        post "/report/cancelCenterReport"(controller: "report", action: "cancelCenterReport")
        post "/report/data"(controller: "report", action: "getReportData")
        post "/report/screenStruct"(controller: "report", action: "getScreenReportStruct")
        post "/report/screenData"(controller: "report", action: "getScreenReportData")
        post "/report/inputValue"(controller: "report", action: "getReportQueryInputValue")

        /* 模块区域 */
        post "/reportModule/reportLink"(controller: "reportModule", action: "saveReportLink")
        post "/reportModule/getByModuleIdAndCloudId"(controller: "reportModule", action: "getByModuleIdAndCloudId")
        post "/reportModule/cloud"(controller: "reportModule", action: "save")
        post "/reportModule/distributeModuleCloud"(controller: "reportModule", action: "distributeModule")
        post "/reportModule/reportLink"(controller: "reportModule", action: "saveReportLink") // 保存链接报表
        post "/reportModule/deleteReportLink"(controller: "reportModule", action: "deleteReportLink") // 删除链接报表
        post "/reportModule/startUsingReportLink"(controller: "reportModule", action: "startUsingReportLink") // 删除链接报表
        put "/reportModule/reportLinkStatus"(controller: "reportModule", action: "startUsingReportLink") // 启用链接报表

        /* 模块 */
        post "/reportModuleInput/getByCondition"(controller: "reportModuleInput", action: "getByCondition")


        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
