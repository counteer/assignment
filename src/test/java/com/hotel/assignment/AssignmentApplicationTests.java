package com.hotel.assignment;

import com.hotel.assignment.dto.PaymentUpdateEvent;
import com.hotel.assignment.model.server.ReservationRequest;
import com.hotel.assignment.model.server.ReservationResponse;
import com.hotel.assignment.repository.RoomReservationRepository;
import com.yourcompany.app.api.client.DefaultApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssignmentApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private RoomReservationRepository repository;

	@MockitoBean
	private DefaultApi paymentStatusApi;

	@MockitoBean
	private KafkaTemplate<String, PaymentUpdateEvent> kafkaTemplate;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@Test
	void testEndToEnd_CreateReservation_ShouldSaveToDatabase() {
		ReservationRequest request = new ReservationRequest();
		request.setNameOfCustomer("Integration Tester");
		request.setRoomNumber("999");
		request.setReservationStartDate(LocalDate.now().plusDays(1));
		request.setReservationEndDate(LocalDate.now().plusDays(5));
		request.setRoomSegment(ReservationRequest.RoomSegmentEnum.MEDIUM);
		request.setModeOfPayment(ReservationRequest.ModeOfPaymentEnum.BANK_TRANSFER);

		ResponseEntity<ReservationResponse> responseEntity = restTemplate.postForEntity(
				"/api/reservations",
				request,
				ReservationResponse.class
		);

		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		assertNotNull(responseEntity.getBody());
		assertEquals(ReservationResponse.ReservationStatusEnum.PENDING_PAYMENT, responseEntity.getBody().getReservationStatus());
		assertEquals(1, repository.count());
		var savedReservation = repository.findAll().getFirst();
		assertEquals("Integration Tester", savedReservation.getNameOfCustomer());
		assertEquals(400.0, savedReservation.getTotalAmount());
	}

	@Test
	void testEndToEnd_CreateReservationWithInvalidInterval_ShouldNotSaveToDatabase() {
		ReservationRequest request = new ReservationRequest();
		request.setNameOfCustomer("Integration Tester");
		request.setRoomNumber("999");
		request.setReservationStartDate(LocalDate.now().plusDays(1));
		request.setReservationEndDate(LocalDate.now().plusDays(33));
		request.setRoomSegment(ReservationRequest.RoomSegmentEnum.MEDIUM);
		request.setModeOfPayment(ReservationRequest.ModeOfPaymentEnum.BANK_TRANSFER);

		ResponseEntity<ReservationResponse> responseEntity = restTemplate.postForEntity(
				"/api/reservations",
				request,
				ReservationResponse.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertNotNull(responseEntity.getBody());
		assertEquals(0, repository.count());
	}
}
