package com.itle.credit.enums;

/**
 * create by Luler on 2023/2/9 10:12
 *
 * @description 信用卡明细相关枚举
 */
public enum CreditCardEnum {
    ITEM_COMP_NAME("信用卡发放公司","itemCompName"),
    ITEM_MONEY_TYPE("信用卡账户币种","itemMoneyType"),
    ITEM_CREDIT_AMT("信用额度","itemCreditAmt"),
    ITEM_CARD_NUM("卡片尾号","itemCardNum"),
    ITEM_USE_AMT("已使用额度","itemUseAmt"),
    ITEM_RD("开卡日期","itemRd"),
    ITEM_ACC_TYPE("账户状态","itemAccType"),
    ITEM_OVER_AMT("透支余额","itemOverAmt")
    ;

    public String findKeyByVal(String value) {
        for (CreditCardEnum enums: CreditCardEnum.values()){
            if (enums.getValue().equals(value)){
                return enums.getKey();
            }
        }
        return null;
    }

    public String findValByKey(String key) {
        for (CreditCardEnum enums: CreditCardEnum.values()){
            if (enums.getKey().equals(key)){
                return enums.getValue();
            }
        }
        return null;
    }

    private String key;
    private String value;

    CreditCardEnum(String key, String value) {
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
