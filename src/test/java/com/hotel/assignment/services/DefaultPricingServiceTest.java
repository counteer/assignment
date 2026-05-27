package com.hotel.assignment.services;

import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultPricingServiceTest {

    private final PricingService pricingService = new DefaultPricingService();

    @Test
    void calculateTotalAmount_MediumRoomForFiveDays_ShouldReturn500() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 6);

        RoomReservation roomReservation = createRoomReservation(start, end, RoomSegment.MEDIUM);
        BigDecimal total = pricingService.calculateTotalAmount(roomReservation);

        assertEquals(BigDecimal.valueOf(500), total);
    }

    @Test
    void calculateTotalAmount_SameDayCheckout_ShouldChargeForOneDay() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 2);
        RoomReservation roomReservation = createRoomReservation(start, end, RoomSegment.LARGE);

        BigDecimal total = pricingService.calculateTotalAmount(roomReservation);

        assertEquals(BigDecimal.valueOf(150), total);
    }

    private static @NonNull RoomReservation createRoomReservation(LocalDate start, LocalDate end, RoomSegment roomSegment) {
        RoomReservation reservation = new RoomReservation();
        reservation.setReservationStartDate(start);
        reservation.setReservationEndDate(end);
        reservation.setRoomSegment(roomSegment);
        return reservation;
    }
}
