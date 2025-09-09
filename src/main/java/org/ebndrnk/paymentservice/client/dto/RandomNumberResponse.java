package org.ebndrnk.paymentservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RandomNumberResponse {
    private String status;
    private long min;
    private long max;

    @JsonProperty("random")
    private long randomNumber;
}
