package com.hotel.assignment.tasks;

import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.repository.RoomReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationCancellationTask {

    private final RoomReservationRepository repository;

    // Runs every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cancelUnpaidBankTransfers() {
        log.info("Starting scheduled task: Checking for overdue bank transfer payments...");
        LocalDate cutoffDate = LocalDate.now().plusDays(2);

        List<RoomReservation> overdueReservations = repository.findByModeOfPaymentAndReservationStatusAndReservationStartDateLessThanEqual(
                PaymentMode.BANK_TRANSFER,
                ReservationStatus.PENDING_PAYMENT,
                cutoffDate
        );

        if (overdueReservations.isEmpty()) {
            log.info("No overdue reservations found.");
            return;
        }

        overdueReservations.forEach(reservation -> {
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
            log.info("Automatically cancelled reservation ID: {} because payment was not received in time.", reservation.getReservationId());
        });

        repository.saveAll(overdueReservations);
        log.info("Finished scheduled task: {} reservations cancelled.", overdueReservations.size());
    }
}
