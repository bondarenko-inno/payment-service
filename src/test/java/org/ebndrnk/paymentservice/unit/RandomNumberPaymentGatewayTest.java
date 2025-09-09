package org.ebndrnk.paymentservice.unit;

import org.ebndrnk.paymentservice.client.RandomNumberClient;
import org.ebndrnk.paymentservice.client.dto.RandomNumberResponse;
import org.ebndrnk.paymentservice.exception.dto.ExternalApiException;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.ebndrnk.paymentservice.service.pay.RandomNumberPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class RandomNumberPaymentGatewayTest {

    @Mock
    private RandomNumberClient randomNumberClient;

    @InjectMocks
    private RandomNumberPaymentGateway gateway;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processPayment_shouldReturnSuccess_whenRandomNumberIsEven() {
        RandomNumberResponse response = new RandomNumberResponse();
        response.setRandomNumber(42);
        when(randomNumberClient.getRandomNumber()).thenReturn(List.of(response));

        PaymentStatus status = gateway.processPayment();

        assertThat(status).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_shouldReturnFailed_whenRandomNumberIsOdd() {
        RandomNumberResponse response = new RandomNumberResponse();
        response.setRandomNumber(7);
        when(randomNumberClient.getRandomNumber()).thenReturn(List.of(response));

        PaymentStatus status = gateway.processPayment();

        assertThat(status).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void processPayment_shouldThrow_whenResponseEmpty() {
        when(randomNumberClient.getRandomNumber()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> gateway.processPayment())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("No random number received from external API");
    }
}
