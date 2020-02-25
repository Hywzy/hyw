package com.offcn.consts;

public enum RedisProductkeyEnum {
    SKUS("product:skus"),
    ISTRUE("product:isTrue");
    private String value;

    RedisProductkeyEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }


}
