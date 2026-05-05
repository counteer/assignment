package com.hotel.assignment.controllers;

import com.hotel.assignment.controller.RoomReservationController;
import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.exceptions.InvalidReservationException;
import com.hotel.assignment.mappers.RoomReservationMapper;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import com.hotel.assignment.services.RoomReservationService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = RoomReservationController.class)
class RoomReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private RoomReservationService service;

    @MockitoBean
    private RoomReservationMapper mapper;

    @MockitoBean
    private KafkaTemplate<String, PaymentUpdateEvent> kafkaTemplate;

    @Test
    void testCreateReservation_ShouldReturn201Created() throws Exception {
        ReservationRequest request = createReservationRequest();

        ReservationResponse response = new ReservationResponse();
        response.setReservationId(1L);
        response.setReservationStatus(ReservationResponse.ReservationStatusEnum.PENDING_PAYMENT);

        when(mapper.toEntity(any(ReservationRequest.class))).thenReturn(new RoomReservation());
        when(service.createReservation(any(RoomReservation.class))).thenReturn(new RoomReservation());
        when(mapper.toResponse(any(RoomReservation.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.reservationStatus").value("PENDING_PAYMENT"));
    }

    @Test
    void testCreateReservation_Exceeds30Days_ShouldReturn400BadRequest() throws Exception {
        ReservationRequest request = createReservationRequest();

        ReservationResponse response = new ReservationResponse();
        response.setReservationId(1L);
        response.setReservationStatus(ReservationResponse.ReservationStatusEnum.PENDING_PAYMENT);

        when(mapper.toEntity(any(ReservationRequest.class))).thenReturn(new RoomReservation());
        when(service.createReservation(any())).thenThrow(
                new InvalidReservationException("A room cannot be reserved for more than 30 days")
        );
        when(mapper.toResponse(any(RoomReservation.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static @NonNull ReservationRequest createReservationRequest() {
        ReservationRequest request = new ReservationRequest();
        request.setNameOfCustomer("Test User");
        request.setRoomNumber("101");
        request.setReservationStartDate(LocalDate.of(2026, 6, 1));
        request.setReservationEndDate(LocalDate.of(2026, 6, 5));
        request.setRoomSegment(ReservationRequest.RoomSegmentEnum.MEDIUM);
        request.setModeOfPayment(ReservationRequest.ModeOfPaymentEnum.BANK_TRANSFER);
        return request;
    }
}
