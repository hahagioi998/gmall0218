package com.atguigu.gmall0218.bean.enums;

public enum PaymentWay {
    ONLINE("在线支付"),
    OUTLINE("货到付款" );

    private String comment ;


    PaymentWay(String comment ){
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
