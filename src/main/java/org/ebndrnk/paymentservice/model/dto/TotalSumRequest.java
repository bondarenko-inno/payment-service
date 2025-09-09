package org.ebndrnk.paymentservice.model.dto;

import java.time.Instant;

public record TotalSumRequest(Instant from, Instant to) {
}
