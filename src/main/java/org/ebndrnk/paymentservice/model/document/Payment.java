package org.ebndrnk.paymentservice.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "payments")
public class Payment {

    @Id
    private UUID id;

    private String orderId;
    private String userId;

    private PaymentStatus status;

    private Instant timestamp;

    private BigDecimal paymentAmount;
}
