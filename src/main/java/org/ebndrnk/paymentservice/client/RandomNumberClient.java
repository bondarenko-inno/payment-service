package org.ebndrnk.paymentservice.client;

import org.ebndrnk.paymentservice.client.dto.RandomNumberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "randomNumberClient", url = "${payment.gateway.url}")
public interface RandomNumberClient {

    @GetMapping("/random-number")
    List<RandomNumberResponse> getRandomNumber();
}
