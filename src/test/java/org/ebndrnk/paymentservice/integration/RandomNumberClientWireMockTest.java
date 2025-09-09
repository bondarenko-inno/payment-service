package org.ebndrnk.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ebndrnk.paymentservice.client.RandomNumberClient;
import org.ebndrnk.paymentservice.client.dto.RandomNumberResponse;
import org.ebndrnk.paymentservice.kafka.PaymentProcessListener;
import org.ebndrnk.paymentservice.kafka.PaymentProcessedPublisher;
import org.ebndrnk.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("wiremock")
@EnableFeignClients
@AutoConfigureWireMock(port = 8081)
class RandomNumberClientWireMockTest {

    @Autowired
    private RandomNumberClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentProcessedPublisher paymentProcessedPublisher;

    @MockitoBean
    private PaymentProcessListener paymentProcessListener;

    @Test
    void getRandomNumber_shouldReturnListOfNumbers() throws Exception {
        RandomNumberResponse mockResponse = new RandomNumberResponse();
        mockResponse.setRandomNumber(42);
        mockResponse.setStatus("OK");
        mockResponse.setMin(0);
        mockResponse.setMax(100);

        stubFor(get(urlEqualTo("/random-number"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(List.of(mockResponse)))
                        .withStatus(200)));

        List<RandomNumberResponse> result = client.getRandomNumber();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo("success");
    }

    @Test
    void getRandomNumber_shouldReturnEmptyList_whenApiReturnsEmpty() {
        stubFor(get(urlEqualTo("/random-number"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withStatus(200)));

        List<RandomNumberResponse> result = client.getRandomNumber();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo("error");
        assertThat(result.getFirst().getMin()).isEqualTo(0);
        assertThat(result.getFirst().getMax()).isEqualTo(0);
        assertThat(result.getFirst().getRandomNumber()).isEqualTo(0);
    }
}
