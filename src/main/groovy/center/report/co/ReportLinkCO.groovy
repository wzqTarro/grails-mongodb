package center.report.co

import center.report.ReportModule
import grails.validation.Validateable

class ReportLinkCO implements Validateable{
    //
    Long id
    ReportModule.ReportLink reportLink
}
