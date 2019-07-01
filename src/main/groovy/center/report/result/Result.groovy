package center.report.result

class Result {
    Integer code = center.report.enst.ResultEnum.SUCCESS.getCode()
    String message = center.report.enst.ResultEnum.SUCCESS.getMessage()

    static Result success() {
        Result result = new Result();
        result.setCode(center.report.enst.ResultEnum.SUCCESS.getCode());
        result.setMessage(center.report.enst.ResultEnum.SUCCESS.getMessage());
        return result;
    }

    static Result error() {
        Result result = new Result();
        result.setCode(center.report.enst.ResultEnum.PARAM_ERROR.getCode());
        result.setMessage(center.report.enst.ResultEnum.PARAM_ERROR.getMessage());
        return result;
    }

    static Result error(String msg) {
        Result result = new Result();
        result.setCode(center.report.enst.ResultEnum.PARAM_ERROR.getCode());
        result.setMessage(msg);
        return result;
    }

    void setError(String msg) {
        this.code = center.report.enst.ResultEnum.PARAM_ERROR.getCode()
        this.message = msg;
    }
}
