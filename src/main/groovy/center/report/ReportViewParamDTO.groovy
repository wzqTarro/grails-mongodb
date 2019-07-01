package center.report

import center.report.dto.ReportParamValueDTO
import grails.validation.Validateable

class ReportViewParamDTO  implements Serializable, Validateable{
    List<ReportParamValueDTO> paramValues
}
