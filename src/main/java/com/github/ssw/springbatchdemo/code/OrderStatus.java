package com.github.ssw.springbatchdemo.code;

/**
 * 주문상태
 */
public enum OrderStatus {
    REQUESTED("001"),
    COMPLETED("002"),
    CANCELED("003");


    private String code;

    OrderStatus(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
