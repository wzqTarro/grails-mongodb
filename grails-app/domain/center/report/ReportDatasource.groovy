package center.report

import center.report.dto.DatasourceConfigDTO
import center.report.enst.DatasourceConfigKindEnum
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper

/**
 * 报表数据源
 */
class ReportDatasource {
    // 编码
    String code
    // 名称
    String name
    // 数据源类型
    Integer kind
    // 配置
    String config
    static constraints = {
        code(unique: true, blank: false)
        name(unique: true, blank: false)
        config(nullable: true)
    }

    static mapping = {
        // 关闭缓存
        stateless true
    }

    /**
     * 保存
     */
    @Transactional
    void saveItem() {
//        String code = this.code
//        if (!code) {
//            this.errors.rejectValue("code", "error", "编码不能为空")
//            return
//        }
//        String name = this.name
//        if (!name) {
//            this.errors.rejectValue("name", "error", "名称不能为空")
//            return
//        }
//        if (!this.id) {
//            if (ReportDatasource.findByCode(code)) {
//                this.errors.rejectValue("code", "error", "数据源编码已存在")
//                return
//            }
//        }
//        if (this.hasChanged("name")) {
//            if (ReportDatasource.findByName(name)) {
//                this.errors.rejectValue("name", "error", "数据源名称已存在")
//                return
//            }
//        }
//
//        // 配置类型
//        Integer kind = this.kind
//        if (kind < 0) {
//            this.errors.rejectValue("kind", "kind", "配置类型缺失")
//            return
//        }
        DatasourceConfigKindEnum datasourceConfigKindEnum = DatasourceConfigKindEnum.getEnumByKind(kind)
        if (!datasourceConfigKindEnum) {
            this.errors.rejectValue("kind", "kind", "配置类型有误")
            return
        }

        //中心数据源、区域数据源、特定区域数据源
        if (datasourceConfigKindEnum.equals(DatasourceConfigKindEnum.SPECIALLY) || datasourceConfigKindEnum.equals(DatasourceConfigKindEnum.CENTER)
                || datasourceConfigKindEnum.equals(DatasourceConfigKindEnum.REGION)) {
            if (!this.config) {
                this.errors.rejectValue("config", "config", "配置不能为空")
                return
            }

            def configMap = new JsonSlurper().parseText(this.config)
            DatasourceConfigDTO datasourceConfig
            try {
                datasourceConfig = new DatasourceConfigDTO(configMap)
            } catch(MissingPropertyException e) {
                this.errors.rejectValue("config", "config", "配置有误")
                return
            }


            if (!datasourceConfig) {
                this.errors.rejectValue("config", "config", "配置有误")
                return
            }
            // 配置区域ID
            String cloudId = datasourceConfig.cloudId
            // 配置类型为特定区域数据源时，需要特定的区域ID
            if (datasourceConfigKindEnum.equals(DatasourceConfigKindEnum.SPECIALLY)) {
                if (!cloudId) {
                    this.errors.rejectValue("config", "config", "特定区域数据源需要指定区域ID")
                    return
                }
            }
        }

        if (this.validate()) {
            Long preVersion = this.version
            if (this.save(flush: true)) {
                if (this.version != preVersion) {
                    Report.updateDatasourceData(this.code, this.name)
                }
            } else {
                this.errors.allErrors.each { err ->
                    this.errors.rejectValue(err.objectName, err.code, err.arguments, err.defaultMessage)
                    return
                }
            }
        }
    }

    /**
     * 根据编码查询
     * @param code
     * @return
     */
    static ReportDatasource getByCode(String code) {
        return ReportDatasource.findByCode(code)
    }

    /**
     * 条件查询
     * @return
     */
    static List<ReportDatasource> getByCondition() {
        return Report.withCriteria {
            order("code", "asc")
        }
    }

    /**
     * 删除
     * @return
     */
    ReportDatasource deleteItem() {
        if (Report.isHasDatasourceCode(this.code)) {
            this.errors.rejectValue("code", "error", "数据源已被使用, 无法删除")
            return
        }
        this.delete(flush: true)
    }
}
