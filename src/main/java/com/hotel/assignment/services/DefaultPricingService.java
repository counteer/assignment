package com.hotel.assignment.services;

import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class DefaultPricingService implements PricingService {

    @Override
    public Double calculateTotalAmount(RoomReservation reservation) {
        LocalDate startDate = reservation.getReservationStartDate();
        LocalDate endDate = reservation.getReservationEndDate();
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        RoomSegment segment = reservation.getRoomSegment();
        double dailyRate = switch (segment) {
            case SMALL -> 50.0;
            case MEDIUM -> 100.0;
            case LARGE -> 150.0;
            case EXTRA_LARGE -> 200.0;
        };
        return days * dailyRate;
    }
}
