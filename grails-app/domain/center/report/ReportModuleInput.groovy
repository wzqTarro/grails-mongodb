package center.report

import grails.validation.Validateable

class ReportModuleInput {
    // 模块ID
    String moduleId
    // 模块名称
    String moduleName
    // 插座性质
    Integer slotKind
    // 说明
    String comment
    // 模块参数
    List<ModuleInput> inputList

    static embedded = ['inputList']

    static constraints = {
        moduleId(unique: true, blank: false)
        moduleName(unique: true, blank: false)
        slotKind(inList: [1, 2])
        comment(nullable: true)
        inputList(nullable: true)

        inputList validator: { List<ModuleInput> vals, ReportModuleInput obj, errors ->
            for (def input: vals) {
                if (!input.name) {
                    errors.rejectValue("inputList", "error", "模块参数名不能为空")
                    return
                }
                if (!input.caption) {
                    errors.rejectValue("inputList", "error", "模块参数标签不能为空")
                    return
                }
                if (!input.dataType) {
                    errors.rejectValue("inputList", "error", "模块参数数据类型不能为空")
                    return
                }
                def other = obj.inputList.findAll {
                    it.name == input.name
                }
                if (other.size() > 1) {
                    errors.rejectValue("inputList", "error", "模块参数名称已存在")
                    return
                }
                if (!input.validate()) {
                    for (def err: input.errors.allErrors) {
                        errors.rejectValue("inputList", err.code, err.arguments, err.defaultMessage)
                        return
                    }
                }
            }
        }
    }

    static mapping = {
        stateless true
    }

    /**
     * 模块参数-禁止当做领域对象使用
     */
    static class ModuleInput implements Validateable{
        // 名称
        String name
        // 标签
        String caption
        // 数据类型
        String dataType
        // 示范值
        String example
        // 说明
        String comment
        // 排列号
        Integer seqNum

        static constraints = {
            example(nullable: true)
            comment(nullable: true)
            seqNum(nullable: true)
        }
    }

    /**
     * 根据名称查询
     * @param name
     * @return
     */
    static List<ReportModuleInput> getByName(String name) {
        return ReportModuleInput.createCriteria().list {
            and {
                if (name) {
                    like("moduleName", "%"+name+"%")
                }
            }
            order("code", "asc")
        }
    }

    /**
     * 根据模块ID查询
     * @param params
     * @return
     */
    static ReportModuleInput getByModuleId(moduleId) {
        if (!moduleId) {
            return null;
        }
        return ReportModuleInput.findByModuleId(moduleId)
    }
}
