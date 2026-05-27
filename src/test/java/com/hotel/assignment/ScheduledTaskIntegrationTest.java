package com.hotel.assignment;

import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import com.hotel.assignment.repository.RoomReservationRepository;
import com.hotel.assignment.tasks.ReservationCancellationTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static com.hotel.assignment.model.server.ReservationRequest.ModeOfPaymentEnum.BANK_TRANSFER;
import static com.hotel.assignment.model.server.ReservationRequest.ModeOfPaymentEnum.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureRestTestClient
class ScheduledTaskIntegrationTest {

    @Autowired
    private RoomReservationRepository repository;

    @Autowired
    private RestTestClient restClient;

    @Autowired
    private ReservationCancellationTask cancellationTask;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCancelOnlyOverduePendingReservations() {
        UUID oldConfirmedReservationUUID = createReservation(-10, 5, CASH);
        UUID overduePendingReservationUUID = createReservation(0, 5, BANK_TRANSFER);
        UUID futurePendingReservationUUID = createReservation(20, 5, BANK_TRANSFER);

        cancellationTask.cancelUnpaidBankTransfers();

        assertReservationStatus(overduePendingReservationUUID, ReservationStatus.CANCELLED);
        assertReservationStatus(futurePendingReservationUUID, ReservationStatus.PENDING_PAYMENT);
        assertReservationStatus(oldConfirmedReservationUUID, ReservationStatus.CONFIRMED);
    }

    private UUID createReservation(int startDaysOffset, int days, ReservationRequest.ModeOfPaymentEnum paymentType) {
        ReservationRequest request = new ReservationRequest(
                "John Doe",
                "101",
                LocalDate.now().plusDays(startDaysOffset),
                LocalDate.now().plusDays(startDaysOffset + days),
                ReservationRequest.RoomSegmentEnum.MEDIUM,
                paymentType
        );

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
        return reservationId;
    }


    private RoomReservation saveReservationWithDateAndStatus(int startDaysOffset, ReservationStatus status) {
        RoomReservation reservation = new RoomReservation();

        reservation.setNameOfCustomer("Test User");
        reservation.setRoomNumber("999");
        reservation.setRoomSegment(RoomSegment.MEDIUM);
        reservation.setModeOfPayment(PaymentMode.BANK_TRANSFER);
        reservation.setTotalAmount(BigDecimal.valueOf(500));
        reservation.setPaidAmount(BigDecimal.valueOf(250));

        reservation.setReservationStartDate(LocalDate.now().plusDays(startDaysOffset));
        reservation.setReservationEndDate(LocalDate.now().plusDays(startDaysOffset + 5));
        reservation.setReservationStatus(status);

        return repository.save(reservation);
    }

    private void assertReservationStatus(UUID reservationId, ReservationStatus expectedStatus) {
        RoomReservation updated = repository.findById(reservationId).orElseThrow();
        assertEquals(expectedStatus, updated.getReservationStatus(),
                "Reservation status did not match expected value!");
    }
}
