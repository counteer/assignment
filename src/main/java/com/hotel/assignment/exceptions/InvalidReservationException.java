package com.hotel.assignment.exceptions;

public class InvalidReservationException extends RuntimeException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
