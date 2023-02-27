package com.itle.credit.enums;

/**
 * create by Luler on 2023/2/9 16:53
 *
 * @description 担保明细的枚举
 */
public enum GuaranteeEnum {

    GUA_LEFT("担保余额","guaLeft"),
    GUA_DATE("担保日期","guaDate"),
    GUA_PER("被担保人","guaPer"),
    GUA_PER_TYPE("责任人类型","guaPerType"),
    GUA_AMT("责任金额","guaAmt")
    ;

    public String findKeyByVal(String value) {
        for (GuaranteeEnum infoEnum: GuaranteeEnum.values()){
            if (infoEnum.getValue().equals(value)){
                return infoEnum.getKey();
            }
        }
        return null;
    }

    public String findValByKey(String key) {
        for (GuaranteeEnum infoEnum: GuaranteeEnum.values()){
            if (infoEnum.getKey().equals(key)){
                return infoEnum.getValue();
            }
        }
        return null;
    }

    private String key;
    private String value;

    GuaranteeEnum(String key, String value) {
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
