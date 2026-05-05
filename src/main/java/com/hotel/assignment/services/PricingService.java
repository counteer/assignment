package com.hotel.assignment.services;

import com.hotel.assignment.entities.RoomReservation;

public interface PricingService {
    Double calculateTotalAmount(RoomReservation reservation);
}
