package com.hotel.assignment.dto;

import java.math.BigDecimal;

public record PaymentUpdateEvent(
        String paymentId,
        String debtorAccountnumber,
        BigDecimal amountReceived,
        String transactionDescription
) {}
