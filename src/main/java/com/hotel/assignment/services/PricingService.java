package com.hotel.assignment.services;

import com.hotel.assignment.entities.RoomReservation;

import java.math.BigDecimal;

public interface PricingService {
    BigDecimal calculateTotalAmount(RoomReservation reservation);
}
