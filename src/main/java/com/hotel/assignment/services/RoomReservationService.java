package com.hotel.assignment.services;

import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.entities.PaymentMode;
import com.hotel.assignment.entities.ReservationStatus;
import com.hotel.assignment.entities.RoomReservation;
import com.hotel.assignment.exceptions.InvalidReservationException;
import com.hotel.assignment.repository.RoomReservationRepository;
import com.yourcompany.app.api.client.DefaultApi;
import com.yourcompany.app.model.client.PaymentStatusResponse;
import com.yourcompany.app.model.client.PaymentStatusRetrievalRequest;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class RoomReservationService {
    private static final Logger log = LoggerFactory.getLogger(RoomReservationService.class);
    private final RoomReservationRepository repository;
    private final DefaultApi paymentClientApi; // Inject the external API client
    private final PricingService pricingService;

    public RoomReservationService(RoomReservationRepository repository, DefaultApi paymentClientApi, PricingService pricingService) {
        this.repository = repository;
        this.paymentClientApi = paymentClientApi;
        this.pricingService = pricingService;
    }

    @Transactional
    public void processBankTransferUpdate(PaymentUpdateEvent event) {
        String[] descriptionParts = event.transactionDescription().split(" ");
        if (descriptionParts.length < 2) {
            log.warn("Invalid transaction description format: {}", event.transactionDescription());
            return;
        }

        String extractedReference = descriptionParts[1];

        Optional<RoomReservation> optionalReservation = repository.findByPaymentReferenceAndReservationStatus(extractedReference, ReservationStatus.PENDING_PAYMENT);

        if (optionalReservation.isEmpty()) {
            log.warn("PENDING_PAYMENT reservation for reference {} not found. It may be missing, already confirmed, or cancelled.", extractedReference);
            return;
        }

        RoomReservation reservation = optionalReservation.get();
        if (event.amountReceived() == null || event.amountReceived() <= 0) {
            log.info("Invalid amount received");
            return;
        }

        Double paidAmount = reservation.getPaidAmount() + event.amountReceived();
        reservation.setPaidAmount(paidAmount);
        if (reservation.getPaidAmount() >= reservation.getTotalAmount()) {
            reservation.setReservationStatus(ReservationStatus.CONFIRMED);
            log.info("Reservation {} fully paid and confirmed! Amount: {}", reservation.getReservationId(), event.amountReceived());
        }
        repository.save(reservation);
    }

    @Transactional
    public RoomReservation createReservation(RoomReservation reservation) {
        validateDates(reservation);
        PaymentMode paymentMode = PaymentMode.valueOf(reservation.getModeOfPayment().name());

        Double calculatedPrice = pricingService.calculateTotalAmount(reservation);
        reservation.setTotalAmount(calculatedPrice);

        ReservationStatus calculatedStatus = calculateReservationStatus(reservation, paymentMode);

        reservation.setPaidAmount(calculatePaidAmount(calculatedStatus, calculatedPrice));
        reservation.setReservationStatus(calculatedStatus);

        RoomReservation savedReservation = repository.save(reservation);
        log.info("Created reservation ID: {} with status: {}", savedReservation.getReservationId(), savedReservation.getReservationStatus());
        return savedReservation;
    }

    private static double calculatePaidAmount(ReservationStatus calculatedStatus, Double calculatedPrice) {
        return calculatedStatus == ReservationStatus.CONFIRMED ? calculatedPrice : 0.0;
    }

    private @NonNull ReservationStatus calculateReservationStatus(RoomReservation reservation, PaymentMode mode) {
        return switch (mode) {
            case CASH -> ReservationStatus.CONFIRMED;
            case BANK_TRANSFER -> ReservationStatus.PENDING_PAYMENT;
            case CREDIT_CARD -> {
                if (isCreditCardConfirmed(reservation.getPaymentReference())) {
                    yield ReservationStatus.CONFIRMED;
                }
                yield ReservationStatus.CANCELLED;
            }
        };
    }

    private static void validateDates(RoomReservation reservation) {
        long daysBetween = ChronoUnit.DAYS.between(reservation.getReservationStartDate(), reservation.getReservationEndDate());
        if (daysBetween > 30) {
            throw new InvalidReservationException("A room cannot be reserved for more than 30 days. Requested: " + daysBetween + " days.");
        }
        if (daysBetween <= 0) {
            throw new InvalidReservationException("The end date must be after the start date.");
        }
    }

    private boolean isCreditCardConfirmed(String paymentReference) {
        if (!StringUtils.hasText(paymentReference)) {
            return false;
        }
        try {
            PaymentStatusRetrievalRequest clientReq = new PaymentStatusRetrievalRequest().paymentReference(paymentReference);
            PaymentStatusResponse clientRes = paymentClientApi.paymentStatusPost(clientReq);
            return "CONFIRMED".equalsIgnoreCase(clientRes.getStatus());
        } catch (Exception e) {
            log.error("Failed to verify credit card payment ID: {}", paymentReference, e);
            return false;
        }
    }

}
