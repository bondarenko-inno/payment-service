package org.ebndrnk.paymentservice.mapper;

import org.ebndrnk.paymentservice.model.document.Payment;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse paymentToResponse(Payment payment);
}
