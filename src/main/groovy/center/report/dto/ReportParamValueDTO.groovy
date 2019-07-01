package center.report.dto

class ReportParamValueDTO implements Serializable{
    /**
     * 参数名称
     */
    String name;
    /**
     * 参数值
     */
    String value;
    /**
     * 参数显示值(动态列表时使用，传入显示值)
     */
    String title;

}
