package org.ebndrnk.paymentservice.service.pay;

import lombok.RequiredArgsConstructor;
import org.ebndrnk.paymentservice.client.RandomNumberClient;
import org.ebndrnk.paymentservice.exception.dto.ExternalApiException;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RandomNumberPaymentGateway implements PaymentGateway {

    private final RandomNumberClient randomNumberClient;


    @Override
    public PaymentStatus processPayment() {
        var responseList = randomNumberClient.getRandomNumber();

        if (responseList.isEmpty()) {
            throw new ExternalApiException("No random number received from external API");
        }

        long randomNumber = responseList.getFirst().getRandomNumber();
        return (randomNumber % 2 == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }
}
