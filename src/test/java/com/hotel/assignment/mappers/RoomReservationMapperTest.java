package com.hotel.assignment.mappers;

import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RoomReservationMapperTest {

    private final RoomReservationMapper mapper = Mappers.getMapper(RoomReservationMapper.class);

    @Test
    void testToEntity_ShouldMapCorrectly() {
        ReservationRequest request = new ReservationRequest();
        request.setNameOfCustomer("John Doe");
        request.setRoomNumber("101");
        request.setReservationStartDate(LocalDate.of(2026, 6, 1));
        request.setReservationEndDate(LocalDate.of(2026, 6, 5));
        request.setRoomSegment(ReservationRequest.RoomSegmentEnum.MEDIUM);
        request.setModeOfPayment(ReservationRequest.ModeOfPaymentEnum.CASH);
        request.setPaymentReference("REF-123");

        RoomReservation entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("John Doe", entity.getNameOfCustomer());
        assertEquals("101", entity.getRoomNumber());
        assertEquals(LocalDate.of(2026, 6, 1), entity.getReservationStartDate());
        assertEquals(RoomSegment.MEDIUM, entity.getRoomSegment());
        assertEquals(PaymentMode.CASH, entity.getModeOfPayment());
        assertEquals("REF-123", entity.getPaymentReference());
        assertNull(entity.getReservationId());
        assertNull(entity.getReservationStatus());
        assertNull(entity.getTotalAmount()); // We calculate this later
    }

    @Test
    void testToResponse_ShouldMapCorrectly() {
        RoomReservation entity = createRoomReservation();

        ReservationResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(105L, response.getReservationId());
        assertEquals(ReservationResponse.ReservationStatusEnum.PENDING_PAYMENT, response.getReservationStatus());
    }

    private static @NonNull RoomReservation createRoomReservation() {
        RoomReservation entity = new RoomReservation();
        entity.setReservationId(105L); // The DB generated this
        entity.setNameOfCustomer("John Doe");
        entity.setRoomNumber("101");
        entity.setReservationStartDate(LocalDate.of(2026, 6, 1));
        entity.setReservationEndDate(LocalDate.of(2026, 6, 5));
        entity.setRoomSegment(RoomSegment.MEDIUM);
        entity.setModeOfPayment(PaymentMode.BANK_TRANSFER);
        entity.setPaymentReference("REF-123");
        entity.setTotalAmount(400.0);
        entity.setPaidAmount(0.0);
        entity.setReservationStatus(ReservationStatus.PENDING_PAYMENT);
        return entity;
    }

}
