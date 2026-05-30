package com.graduation.smarty_commerce.ui.Model.Request;

import com.graduation.smarty_commerce.shared.PaymentMethod;

public class OrderRequestModel {

    private String addressId;
    private PaymentMethod paymentMethod;
    private String paymentToken; // Simulated dummy token used to charge cards

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
}
