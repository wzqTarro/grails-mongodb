package center.report.dto

import center.report.Report

class TableParamDTO implements Serializable{
    Report.Table reportTables
    List rowList
    List columnNameList
    Set paramNameSet
}
