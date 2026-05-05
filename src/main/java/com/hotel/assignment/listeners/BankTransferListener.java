package com.hotel.assignment.listeners;


import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.services.RoomReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankTransferListener {

    private static final Logger log = LoggerFactory.getLogger(BankTransferListener.class);

    private final RoomReservationService service;

    public BankTransferListener(RoomReservationService service) {
        this.service = service;
    }

    @Transactional
    @KafkaListener(topics = "bank-transfer-payment-update", groupId = "reservation-service-group")
    public void consumePaymentUpdate(PaymentUpdateEvent event) {
        log.info("Received payment update for paymentId: {}, status: {}", event.paymentId(), event.amountReceived());
        service.processBankTransferUpdate(event);
    }
}
