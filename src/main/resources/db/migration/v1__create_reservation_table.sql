CREATE TABLE room_reservation
(
    reservation_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_of_customer       VARCHAR(255) NOT NULL,
    room_number            VARCHAR(50)  NOT NULL,
    reservation_start_date DATE         NOT NULL,
    reservation_end_date   DATE         NOT NULL,
    room_segment           VARCHAR(50)  NOT NULL,
    mode_of_payment        VARCHAR(50)  NOT NULL,
    reservation_status     VARCHAR(50)  NOT NULL,
    payment_reference      VARCHAR(255),
    total_amount DOUBLE NOT NULL,
    paid_amount DOUBLE NOT NULL DEFAULT 0.0
);
