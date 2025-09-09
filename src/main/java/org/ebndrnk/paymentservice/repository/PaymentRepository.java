package org.ebndrnk.paymentservice.repository;

import org.ebndrnk.paymentservice.model.document.Payment;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByOrderId(String orderId);

    List<Payment> findByUserIdOrderByTimestampDesc(String userId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query(value = "{ 'timestamp': { $gte: ?0, $lte: ?1 }, 'status': 'SUCCESS' }",
            fields = "{ 'paymentAmount': 1 }")
    List<Payment> findPaymentsInPeriod(Instant from, Instant to);
}
