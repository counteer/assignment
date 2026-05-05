package com.hotel.assignment.mappers;

import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoomReservationMapper {
    ReservationResponse toResponse(RoomReservation entity);

    @Mapping(target = "reservationId", ignore = true)
    @Mapping(target = "reservationStatus", ignore = true)
    RoomReservation toEntity(ReservationRequest request);
}
