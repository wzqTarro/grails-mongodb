package center.report.enst

enum ResultEnum {
    SUCCESS(1, "执行成功"),
    PARAM_ERROR(3, "参数错误"),
    OTHER_ERROR(6, "其他错误")
    ;
    Integer code
    String message
    private ResultEnum(Integer code, String message){
        this.code = code
        this.message = message
    }

    Integer getCode() {
        return code
    }

    String getMessage() {
        return message
    }
}