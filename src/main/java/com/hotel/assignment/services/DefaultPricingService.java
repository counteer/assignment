package com.hotel.assignment.services;

import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class DefaultPricingService implements PricingService {

    @Override
    public BigDecimal calculateTotalAmount(RoomReservation reservation) {
        LocalDate startDate = reservation.getReservationStartDate();
        LocalDate endDate = reservation.getReservationEndDate();
        BigDecimal days = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));

        RoomSegment segment = reservation.getRoomSegment();
        BigDecimal dailyRate = switch (segment) {
            case SMALL -> BigDecimal.valueOf(50);
            case MEDIUM -> BigDecimal.valueOf(100);
            case LARGE -> BigDecimal.valueOf(150);
            case EXTRA_LARGE -> BigDecimal.valueOf(200);
        };
        return dailyRate.multiply(days);
    }
}
