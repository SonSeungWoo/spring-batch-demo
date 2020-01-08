package com.github.ssw.springbatchdemo.code;

public enum OrderStatus {
    REQUESTED("001"),
    COMPLETED("002");

    private String code;

    OrderStatus(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
