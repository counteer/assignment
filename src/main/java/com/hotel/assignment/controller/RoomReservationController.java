package com.hotel.assignment.controller;

import com.hotel.assignment.api.server.ReservationApi;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.mappers.RoomReservationMapper;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import com.hotel.assignment.services.RoomReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
public class RoomReservationController implements ReservationApi {

    private final RoomReservationService reservationService;
    private final RoomReservationMapper mapper;

    @Override
    public ResponseEntity<ReservationResponse> createReservation(ReservationRequest request) {
        log.info("New reservation incoming: {}", request);
        RoomReservation savedEntity = reservationService.createReservation(mapper.toEntity(request));
        ReservationResponse response = mapper.toResponse(savedEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}

