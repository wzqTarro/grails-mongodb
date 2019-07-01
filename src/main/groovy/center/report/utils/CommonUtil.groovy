package center.report.utils

import center.report.common.CommonValue
import center.report.dto.ReportParamValueDTO
import center.report.enst.QueryInputDefTypeEnum
import center.report.enst.ReportSystemParamEnum

import static center.report.enst.ReportSystemParamEnum.*
import static center.report.enst.QueryInputDefTypeEnum.*

import java.util.regex.Pattern

class CommonUtil {
    /**
     * 解析sql中的参数，参数包含[]
     * @param sql
     * @return
     */
    static ArrayList<String> analysisSql(String sql) {
        List<String> paramList = new ArrayList<>()
        if (sql) {
            // sql 中包含参数
            if (sql.contains(CommonValue.PARAM_PREFIX) && sql.contains(CommonValue.PARAM_SUFFIX)) {
                String[] sqlArray = sql.split(Pattern.quote(CommonValue.PARAM_PREFIX))
                for (int i = 1; i < sqlArray.length; i++) {
                    String param = sqlArray[i]
                    if (sqlArray[i].contains(CommonValue.PARAM_SUFFIX)) {
                        paramList.add(CommonValue.PARAM_PREFIX + param[0..param.indexOf(CommonValue.PARAM_SUFFIX)])
                    }
                }
            }
        }
        return paramList
    }

    /**
     * 将包含下划线_的字符串转为驼峰格式
     * @param str
     * @return
     */
    static String toHumpStr(String str) {
        if (str && str.contains("_")) {
            String s = str[str.indexOf("_") + 1]
            String upperStr = str[str.indexOf("_") + 1].toUpperCase()
            str = str.replace("_" + s, upperStr)
            return str
        }
        return str
    }
    /**
     * 获取系统参数的值
     * @param paramName
     * @return
     */
    static ReportParamValueDTO getSystemParamValue(String paramName) {
        if (!paramName) {
            return null
        }
        ReportParamValueDTO paramValue = new ReportParamValueDTO()
        paramValue.name = paramName
        ReportSystemParamEnum systemParamEnum = ReportSystemParamEnum.getEnumByName(paramName)
        if (systemParamEnum) {
            /**
             * TODO 系统参数值来源
             */
            switch (systemParamEnum) { // 枚举使用switch，需要引入枚举类的静态包import static com.report.enst.ReportSystemParamEnum.*
                case CURRENT_DEPART :
                    paramValue.setTitle("显示的科室名称");
                    paramValue.setValue("科室的标识");
                    break;
                case CURRENT_ORG :
                    paramValue.setTitle("显示的机构名称");
                    paramValue.setValue("ef325711521b11e6bbd8d017c2939671");
                    break;
                case CURRENT_TEAM :
                    paramValue.setTitle("显示的团队名称");
                    paramValue.setValue("团队的标识");
                    break;
                case CURRENT_USER :
                    paramValue.setTitle("显示的职员名称");
                    paramValue.setValue("b1dcc74abedc4899a259e96d2c6f18dc");
                    break;
            }
        }
    }

    /**
     * 获取输入参数值
     * @param inputs
     * @return
     */
    static ReportParamValueDTO getInputParamValue(QueryInputDefTypeEnum queryInputDefTypeEnum) {
        ReportParamValueDTO paramValue = new ReportParamValueDTO()
        if (queryInputDefTypeEnum) {
            /**
             * TODO 数据来源
             */
            def nowCalendar = new Date().toCalendar()
            switch (queryInputDefTypeEnum) {
                case ORG_ALL :
                    paramValue.setTitle("所有机构");
                    paramValue.setValue("");
                    break;
                case ORG_MY :
                    /**
                     * TODO
                     */
                    paramValue.setTitle("my_org_name");
                    paramValue.setValue("ef325711521b11e6bbd8d017c2939671");
                    break;
                case DEPAMENT_ALL :
                    paramValue.setTitle("所有科室");
                    paramValue.setValue("");
                    break;
                case DEPAMENT_MY :
                    /**
                     * TODO
                     */
                    paramValue.setTitle("我的科室的名称");
                    paramValue.setValue("我的科室的值");
                    break;
                case TEAM_ALL :
                    paramValue.setTitle("所有团队");
                    paramValue.setValue("");
                    break;
                case TEAM_MY :
                    /**
                     * TODO
                     */
                    paramValue.setTitle("我的团队的名称");
                    paramValue.setValue("我的团队的值");
                    break;
                case YEAR_LAST :
                    nowCalendar.add(Calendar.YEAR, -1)
                    paramValue.setValue(nowCalendar.getTime().format("yyyy"));
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy"));
                    break;
                case YEAR_NOW :
                    paramValue.setTitle(new Date().format("yyyy"));
                    paramValue.setValue(new Date().format("yyyy"));
                    break;
                case MONTH_LAST :
                    nowCalendar.add(Calendar.MONTH, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM"));
                    break;
                case MONTH_NEXT :
                    nowCalendar.add(Calendar.MONTH, 1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM"));
                    break;
                case MONTH_NOW :
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM"));
                    break;
                case DATE_LAST :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_NEXT :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_LAST_7 :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, -7);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_LAST_MONTH :
                    nowCalendar.add(Calendar.MONTH, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_MONTH_FIRST :
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-01"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMM01"));
                    break;
                case DATE_MONTH_LAST :
                    nowCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    nowCalendar.add(Calendar.MONTH, 1);
                    nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_NEXT_7 :
                    nowCalendar.add(Calendar.DAY_OF_YEAR, 7);
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
                case DATE_NOW :
                    paramValue.setTitle(nowCalendar.getTime().format("yyyy-MM-dd"));
                    paramValue.setValue(nowCalendar.getTime().format("yyyyMMdd"));
                    break;
            }
            return paramValue
        }
        return null
    }
}
