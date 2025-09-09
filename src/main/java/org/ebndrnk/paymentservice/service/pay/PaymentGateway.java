package org.ebndrnk.paymentservice.service.pay;

import org.ebndrnk.paymentservice.model.document.PaymentStatus;

public interface PaymentGateway {
    PaymentStatus processPayment();
}
