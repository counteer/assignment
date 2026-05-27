package com.hotel.assignment.listeners;


import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.services.RoomReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankTransferListener {

    private final RoomReservationService service;
    
    @KafkaListener(topics = "bank-transfer-payment-update", groupId = "reservation-service-group")
    public void consumePaymentUpdate(PaymentUpdateEvent event) {
        log.info("Received payment update for paymentId: {}, status: {}", event.paymentId(), event.amountReceived());
        service.processBankTransferUpdate(event);
    }
}
