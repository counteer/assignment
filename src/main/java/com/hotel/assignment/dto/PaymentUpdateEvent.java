package com.hotel.assignment.dto;

public record PaymentUpdateEvent(
        String paymentId,
        String debtorAccountnumber,
        Double amountReceived,
        String transactionDescription
) {}
