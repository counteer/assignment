package com.hotel.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import com.hotel.assignment.payment.client.api.DefaultApi;
import com.hotel.assignment.repository.RoomReservationRepository;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.dto.PaymentUpdateEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "payment-service.base-url=http://localhost:9999"
        }
)
@ActiveProfiles("test")
@AutoConfigureRestTestClient
@EmbeddedKafka(
        topics = "bank-transfer-payment-update"
)
class KafkaIntegrationTest {

    @Autowired
    private RestTestClient restClient;

    @Autowired
    private KafkaTemplate<String, PaymentUpdateEvent> kafkaTemplate;

    @Autowired
    private RoomReservationRepository repository;

    @MockitoBean
    private DefaultApi paymentStatusApi;

    @Test
    void completeBookingLifecycle_EndToEnd() throws Exception {
        ReservationRequest request = new ReservationRequest(
                "John Doe",
                "101",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                ReservationRequest.RoomSegmentEnum.MEDIUM,
                ReservationRequest.ModeOfPaymentEnum.BANK_TRANSFER
                );
        request.setPaymentReference("REF-KAFKA-123");
        ReservationResponse createdReservation = restClient
                .post()
                .uri("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(ReservationResponse.class)
                .getResponseBody();

        assertNotNull(createdReservation);

        UUID reservationId = createdReservation.getReservationId();
        assertNotNull(reservationId);
        assertNotNull(createdReservation.getReservationStatus());
        assertEquals(
                ReservationStatus.PENDING_PAYMENT.name(),
                createdReservation.getReservationStatus().name()
        );

        PaymentUpdateEvent paymentEvent = new PaymentUpdateEvent(
                "TRX1",
                "IBAN",
                BigDecimal.valueOf(80000),
                "HOTEL REF-KAFKA-123"
        );

        kafkaTemplate
                .send("bank-transfer-payment-update", paymentEvent)
                .get(10, TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomReservation finalReservation = repository
                            .findById(reservationId)
                            .orElseThrow();

                    assertEquals(
                            ReservationStatus.CONFIRMED,
                            finalReservation.getReservationStatus()
                    );
                });
    }
}