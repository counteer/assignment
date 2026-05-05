package com.hotel.assignment;

import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import com.hotel.assignment.repository.RoomReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = "bank-transfer-payment-update")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, PaymentUpdateEvent> kafkaTemplate;

    @Autowired
    private RoomReservationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testKafkaListener_ShouldConsumeMessageAndConfirmReservation() {
        RoomReservation reservation = new RoomReservation();
        reservation.setNameOfCustomer("Kafka Tester");
        reservation.setRoomNumber("101");
        reservation.setReservationStartDate(LocalDate.now());
        reservation.setReservationEndDate(LocalDate.now().plusDays(5));
        reservation.setRoomSegment(RoomSegment.MEDIUM);
        reservation.setModeOfPayment(PaymentMode.BANK_TRANSFER);

        reservation.setPaymentReference("REF-KAFKA-123");
        reservation.setTotalAmount(500.0);
        reservation.setPaidAmount(0.0);
        reservation.setReservationStatus(ReservationStatus.PENDING_PAYMENT);

        RoomReservation savedReservation = repository.save(reservation);

        PaymentUpdateEvent event = new PaymentUpdateEvent(
                "TRX-TEST-1",
                "HU1234567890",
                500.0,
                "HOTEL REF-KAFKA-123"
        );
        kafkaTemplate.send("bank-transfer-payment-update", event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            RoomReservation updatedReservation = repository.findById(savedReservation.getReservationId()).orElse(null);

            assertNotNull(updatedReservation);

            assertEquals(500.0, updatedReservation.getPaidAmount());
            assertEquals(ReservationStatus.CONFIRMED, updatedReservation.getReservationStatus());
        });
    }
}
