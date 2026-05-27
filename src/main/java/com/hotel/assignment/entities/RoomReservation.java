package com.hotel.assignment.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OptimisticLocking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@OptimisticLocking
@ToString
public class RoomReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reservationId;

    private String nameOfCustomer;

    private String roomNumber;

    private LocalDate reservationStartDate;

    private LocalDate reservationEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomSegment roomSegment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode modeOfPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    private String paymentReference;

    @Version
    private Long version;
}
