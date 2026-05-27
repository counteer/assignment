package com.hotel.assignment.repository;

import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomReservationRepository extends JpaRepository<RoomReservation, UUID> {
     Optional<RoomReservation> findByPaymentReferenceAndReservationStatus(String reference, ReservationStatus reservationStatus);
     List<RoomReservation> findByModeOfPaymentAndReservationStatusAndReservationStartDateLessThanEqual(
             PaymentMode mode,
             ReservationStatus status,
             LocalDate cutoffDate
     );
}
