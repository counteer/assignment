package com.hotel.assignment;

import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import com.hotel.assignment.repository.RoomReservationRepository;
import com.hotel.assignment.tasks.ReservationCancellationTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ScheduledTaskIntegrationTest {

    @Autowired
    private RoomReservationRepository repository;

    @Autowired
    private ReservationCancellationTask cancellationTask;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCancelOnlyOverduePendingReservations() {
        RoomReservation overduePending = saveReservationWithDateAndStatus(-40, ReservationStatus.PENDING_PAYMENT);
        RoomReservation futurePending = saveReservationWithDateAndStatus(10, ReservationStatus.PENDING_PAYMENT);
        RoomReservation oldConfirmed = saveReservationWithDateAndStatus(-50, ReservationStatus.CONFIRMED);

        cancellationTask.cancelUnpaidBankTransfers();

        assertReservationStatus(overduePending.getReservationId(), ReservationStatus.CANCELLED);
        assertReservationStatus(futurePending.getReservationId(), ReservationStatus.PENDING_PAYMENT);
        assertReservationStatus(oldConfirmed.getReservationId(), ReservationStatus.CONFIRMED);
    }


    private RoomReservation saveReservationWithDateAndStatus(int startDaysOffset, ReservationStatus status) {
        RoomReservation reservation = new RoomReservation();

        reservation.setNameOfCustomer("Test User");
        reservation.setRoomNumber("999");
        reservation.setRoomSegment(RoomSegment.MEDIUM);
        reservation.setModeOfPayment(PaymentMode.BANK_TRANSFER);
        reservation.setTotalAmount(500.0);
        reservation.setPaidAmount(250.0);

        reservation.setReservationStartDate(LocalDate.now().plusDays(startDaysOffset));
        reservation.setReservationEndDate(LocalDate.now().plusDays(startDaysOffset + 5));
        reservation.setReservationStatus(status);

        return repository.save(reservation);
    }

    private void assertReservationStatus(Long reservationId, ReservationStatus expectedStatus) {
        RoomReservation updated = repository.findById(reservationId).orElseThrow();
        assertEquals(expectedStatus, updated.getReservationStatus(),
                "Reservation status did not match expected value!");
    }
}
