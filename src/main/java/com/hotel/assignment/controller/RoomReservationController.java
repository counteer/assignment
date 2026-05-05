package com.hotel.assignment.controller;

import com.hotel.assignment.api.server.ReservationApi;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.mappers.RoomReservationMapper;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import com.hotel.assignment.services.RoomReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomReservationController implements ReservationApi {
    private static final Logger log = LoggerFactory.getLogger(RoomReservationService.class);

    private final RoomReservationService reservationService;
    private final RoomReservationMapper mapper;

    public RoomReservationController(RoomReservationService reservationService, RoomReservationMapper mapper) {
        this.reservationService = reservationService;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<ReservationResponse> createReservation(ReservationRequest request) {
        log.info("New reservation incoming: {}", request);
        RoomReservation savedEntity = reservationService.createReservation(mapper.toEntity(request));
        ReservationResponse response = mapper.toResponse(savedEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}

