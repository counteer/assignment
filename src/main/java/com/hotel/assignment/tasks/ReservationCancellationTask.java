package com.hotel.assignment.tasks;

import com.hotel.assignment.repository.RoomReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.PaymentMode;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservationCancellationTask {

    private static final Logger log = LoggerFactory.getLogger(ReservationCancellationTask.class);
    private final RoomReservationRepository repository;

    public ReservationCancellationTask(RoomReservationRepository repository) {
        this.repository = repository;
    }

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
