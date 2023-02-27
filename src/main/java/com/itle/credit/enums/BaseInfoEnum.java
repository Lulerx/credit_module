package com.itle.credit.enums;

/**
 * create by Luler on 2023/2/8 16:54
 *
 * @description 个人基本信息枚举
 */
public enum BaseInfoEnum {

    USER_NAME("姓名","USER_NAME"),
    DEPT_ALLNAME("机构代码","DEPT_ALLNAME"),
    IS_MARRY("婚姻状况","IS_MARRY"),
    CREDIT_DATE("报告时间","CREDIT_DATE"),
    ID_CARD("证件号","ID_CARD"),
    LOANS_CAR_WJQ("个人汽车贷款未结清销户账户数","LOANS_CAR_WJQ"),
    LOANS_CAR_NUM("个人汽车贷款笔数","LOANS_CAR_NUM"),
    LOANS_CONS_WJQ("个人消费贷款未结清销户账户数","LOANS_CONS_WJQ"),
    LOANS_CONS_NUM("个人消费贷笔数","LOANS_CONS_NUM"),
    LOANS_HOUSE_NUM("住房贷款","LOANS_HOUSE_NUM"),
    CREDIT_OVERDUE90_NUM("信用卡发生过90天以上逾期的账户数","CREDIT_OVERDUE90_NUM"),
    CREDIT_OVERDUE_NUM("信用卡发生过逾期的账户数","CREDIT_OVERDUE_NUM"),
//    QUERY_CREDIT_APPLY_NUM("信用卡审批","QUERY_CREDIT_APPLY_NUM"),
    XYKMX("信用卡明细","xykmx"),
    CREDIT_MONTHLY_NUM("信用卡月消费额度超5万张数","CREDIT_MONTHLY_NUM"),
    CREDIT_WJQ_NUM("信用卡未结清销户账户数","CREDIT_WJQ_NUM"),
    CREDIT_ACT_NUM("信用卡激活张数","CREDIT_ACT_NUM"),
    CREDIT_EFF_NUM("信用卡账户数","CREDIT_EFF_NUM"),
    XYGY("信用概要","xygy"),
    XDJL("信贷记录","xdjl"),
    GGJL("公共记录","ggjl"),
    QTYW_90YQ_NUM("其他业务发生过90天以上逾期的账户数","QTYW_90YQ_NUM"),
    QTYW_YQ_NUM("其他业务发生过逾期的账户数","QTYW_YQ_NUM"),
    LOANS_QTYW_NUM("其他业务未结清销户账户数","LOANS_QTYW_NUM"),
    QTYW_NUM("其他业务账户数","QTYW_NUM"),
    LOANS_QTDK90YQ_NUM("其他贷款发生过90天以上逾期的账户数","LOANS_QTDK90YQ_NUM"),
    LOANS_QTYQ_NUM("其他贷款发生过逾期的账户数","LOANS_QTYQ_NUM"),
    LOANS_QTDK_NUM("其他贷款未结清销户账户数","LOANS_QTDK_NUM"),
    LOANS_OTHER_NUM("其他贷款账户数","LOANS_OTHER_NUM"),
    ENFORCE_NUM("强制执行次数","ENFORCE_NUM"),
    QUERY_ALL_NUM("总次数","QUERY_ALL_NUM"),
    DBRMX("担保人明细","dbrmx"),
    DB_NUM("担保次数","DB_NUM"),
//    QUERY_CONTRI_NUM("担保资格审查","QUERY_CONTRI_NUM"),
    QUERY_SELF_NUM("本人查询次数","QUERY_SELF_NUM"),
    QUERY_ORG_NUM("机构查询次数","QUERY_ORG_NUM"),
    CXJL("查询记录","cxjl"),
    XGHKZRRMX("相关还款责任人明细","xghkzrrmx"),
    CREDIT_ZRR_NUM("相关还款责任账户数为个人","CREDIT_ZRR_NUM"),
    LOANS_QYZH_NUM("相关还款责任账户数为企业","LOANS_QYZH_NUM"),
    LOANS_GFDK90YQ_NUM("购房贷款发生过90天以上逾期的账户数","LOANS_GFDK90YQ_NUM"),
    LOANS_GFDKYQ_NUM("购房贷款发生过逾期的账户数","LOANS_GFDKYQ_NUM"),
    LOANS_GFDK_WJQ_NUM("购房贷款未结清销户账户数","LOANS_GFDK_WJQ_NUM"),
    LOANS_GFDK_NUM("购房贷款账户数","LOANS_GFDK_NUM"),
//    QUERY_LOANS_NUM("贷后管理","QUERY_LOANS_NUM"),
//    QUERY_APPLY_NUM("贷款审批","QUERY_APPLY_NUM"),
    DKMX("贷款明细","dkmx")
    ;

    public String findKeyByVal(String value) {
        for (BaseInfoEnum infoEnum: BaseInfoEnum.values()){
            if (infoEnum.getValue().equals(value)){
                return infoEnum.getKey();
            }
        }
        return null;
    }

    public String findValByKey(String key) {
        for (BaseInfoEnum infoEnum: BaseInfoEnum.values()){
            if (infoEnum.getKey().equals(key)){
                return infoEnum.getValue();
            }
        }
        return null;
    }

    private String key;
    private String value;

    BaseInfoEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
