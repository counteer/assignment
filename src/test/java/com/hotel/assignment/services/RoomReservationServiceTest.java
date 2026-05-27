package com.hotel.assignment.services;

import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.entities.RoomSegment;
import com.hotel.assignment.exceptions.InvalidReservationException;
import com.hotel.assignment.payment.client.api.DefaultApi;
import com.hotel.assignment.payment.client.model.PaymentStatusResponse;
import com.hotel.assignment.payment.client.model.PaymentStatusRetrievalRequest;
import com.hotel.assignment.repository.RoomReservationRepository;
import org.apache.kafka.common.errors.ApiException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomReservationServiceTest {

    @Mock
    private RoomReservationRepository repository;

    @Mock
    private DefaultApi paymentClientApi;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private RoomReservationService service;

    @Test
    void testCreateReservation_CashPayment_ShouldConfirmInstantly() {
        RoomReservation requestEntity = getRoomReservation(PaymentMode.CASH);
        when(pricingService.calculateTotalAmount(any(RoomReservation.class))).thenReturn(BigDecimal.valueOf(400));
        when(repository.save(any(RoomReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomReservation savedEntity = service.createReservation(requestEntity);

        assertEquals(ReservationStatus.CONFIRMED, savedEntity.getReservationStatus());
        assertEquals(BigDecimal.valueOf(400), savedEntity.getTotalAmount());
        assertEquals(BigDecimal.valueOf(400), savedEntity.getPaidAmount());
        verify(repository, times(1)).save(any(RoomReservation.class));
    }

    @Test
    void testCreateReservation_CashPaymentBankTransfer_ShouldBePending() {
        RoomReservation requestEntity = getRoomReservation(PaymentMode.BANK_TRANSFER);
        when(pricingService.calculateTotalAmount(any(RoomReservation.class))).thenReturn(BigDecimal.valueOf(400));
        when(repository.save(any(RoomReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomReservation savedEntity = service.createReservation(requestEntity);

        assertEquals(ReservationStatus.PENDING_PAYMENT, savedEntity.getReservationStatus());
        assertEquals(BigDecimal.valueOf(400), savedEntity.getTotalAmount());
        assertEquals(BigDecimal.ZERO, savedEntity.getPaidAmount());
        verify(repository, times(1)).save(any(RoomReservation.class));
    }

    private static @NonNull RoomReservation getRoomReservation(PaymentMode paymentMode) {
        RoomReservation requestEntity = new RoomReservation();
        requestEntity.setReservationStartDate(LocalDate.of(2026, 6, 1));
        requestEntity.setReservationEndDate(LocalDate.of(2026, 6, 5));
        requestEntity.setRoomSegment(RoomSegment.MEDIUM);
        requestEntity.setModeOfPayment(paymentMode);
        return requestEntity;
    }

    @Test
    void testCreateReservation_CreditCardPaymentValid_ShouldBePaid() throws ApiException {
        RoomReservation requestEntity = getRoomReservation(PaymentMode.CREDIT_CARD);
        requestEntity.setPaymentReference("TEST");
        when(pricingService.calculateTotalAmount(any(RoomReservation.class))).thenReturn(BigDecimal.valueOf(400));
        when(paymentClientApi.paymentStatusPost(any(PaymentStatusRetrievalRequest.class))).thenReturn(new PaymentStatusResponse().status("CONFIRMED"));
        when(repository.save(any(RoomReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomReservation savedEntity = service.createReservation(requestEntity);

        assertEquals(ReservationStatus.CONFIRMED, savedEntity.getReservationStatus());
        assertEquals(BigDecimal.valueOf(400), savedEntity.getTotalAmount());
        assertEquals(BigDecimal.valueOf(400), savedEntity.getPaidAmount());
        verify(repository, times(1)).save(any(RoomReservation.class));
    }

    @Test
    void testCreateReservation_CashPaymentCreditCardInvalid_ShouldBeCancelled() throws ApiException {
        RoomReservation requestEntity = getRoomReservation(PaymentMode.CREDIT_CARD);
        when(pricingService.calculateTotalAmount(any(RoomReservation.class))).thenReturn(BigDecimal.valueOf(400));
        when(repository.save(any(RoomReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomReservation savedEntity = service.createReservation(requestEntity);

        assertEquals(ReservationStatus.CANCELLED, savedEntity.getReservationStatus());
        assertEquals(BigDecimal.valueOf(400), savedEntity.getTotalAmount());
        assertEquals(BigDecimal.ZERO, savedEntity.getPaidAmount());
        verify(repository, times(1)).save(any(RoomReservation.class));
    }

    @Test
    void testCreateReservation_Exceeds30Days_ShouldThrowException() {
        RoomReservation requestEntity = new RoomReservation();
        requestEntity.setReservationStartDate(LocalDate.of(2026, 6, 1));
        requestEntity.setReservationEndDate(LocalDate.of(2026, 7, 15));

        InvalidReservationException exception = assertThrows(
                InvalidReservationException.class,
                () -> service.createReservation(requestEntity)
        );

        assertTrue(exception.getMessage().contains("more than 30 days"));
        verify(repository, never()).save(any());
    }

    @Test
    void testConsumePaymentUpdate_ValidPartialPayment_ShouldAccumulateAndStayPending() {
        RoomReservation existingReservation = createRoomReservation();
        when(repository.findByPaymentReferenceAndReservationStatus("REF-12345", ReservationStatus.PENDING_PAYMENT))
                .thenReturn(Optional.of(existingReservation));
        when(repository.save(any(RoomReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentUpdateEvent partialPayEvent = createPaymentUpdateEvent(BigDecimal.valueOf(200));

        service.processBankTransferUpdate(partialPayEvent);

        assertEquals(BigDecimal.valueOf(200), existingReservation.getPaidAmount());
        assertEquals(ReservationStatus.PENDING_PAYMENT, existingReservation.getReservationStatus());
        verify(repository, times(1)).save(existingReservation);
    }

    @Test
    void testConsumePaymentUpdate_ValidFullPayment_ShouldAccumulateAndBecomeConfirmed() {
        RoomReservation existingReservation = createRoomReservation();
        when(repository.findByPaymentReferenceAndReservationStatus("REF-12345", ReservationStatus.PENDING_PAYMENT))
                .thenReturn(Optional.of(existingReservation));
        when(repository.save(any(RoomReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PaymentUpdateEvent event = createPaymentUpdateEvent(BigDecimal.valueOf(500));

        service.processBankTransferUpdate(event); // Or whatever you named the method!

        assertEquals(BigDecimal.valueOf(500), existingReservation.getPaidAmount());
        assertEquals(ReservationStatus.CONFIRMED, existingReservation.getReservationStatus());
        verify(repository, times(1)).save(existingReservation);
    }

    private static @NonNull PaymentUpdateEvent createPaymentUpdateEvent(BigDecimal amount) {
        return new PaymentUpdateEvent(
                "TRX-999",
                "HU1234567890",
                amount,
                "HOTEL REF-12345"
        );
    }

    private static @NonNull RoomReservation createRoomReservation() {
        RoomReservation existingReservation = new RoomReservation();
        existingReservation.setReservationId(UUID.randomUUID());
        existingReservation.setTotalAmount(BigDecimal.valueOf(500));
        existingReservation.setPaidAmount(BigDecimal.ZERO);
        existingReservation.setReservationStatus(ReservationStatus.PENDING_PAYMENT);
        return existingReservation;
    }
}
