package com.itle.credit.enums;

/**
 * create by Luler on 2023/2/8 17:48
 *
 * @description 贷款明细的枚举
 */
public enum LoansDetailEnum {

    LOANS_MONEY("贷款余额","loansMoney"),
    LOANS_RD("贷款发放日期","loansRd"),
    LOANS_COMP("贷款发放机构","loansComp"),
    LOANS_STATUS("贷款状态","loansStatus"),
    LOANS_TYPE("贷款类型","loansType"),
    LOANS_CLR_DATE("贷款结清日期","loansClrDate"),
    LOANS_MON_TYPE("贷款账户币种","loansMonType"),
    LOANS_AMT("贷款金额","loansAmt")
    ;

    public String findKeyByVal(String value) {
        for (LoansDetailEnum infoEnum: LoansDetailEnum.values()){
            if (infoEnum.getValue().equals(value)){
                return infoEnum.getKey();
            }
        }
        return null;
    }

    public String findValByKey(String key) {
        for (LoansDetailEnum infoEnum: LoansDetailEnum.values()){
            if (infoEnum.getKey().equals(key)){
                return infoEnum.getValue();
            }
        }
        return null;
    }

    private String key;
    private String value;

    LoansDetailEnum(String key, String value) {
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
