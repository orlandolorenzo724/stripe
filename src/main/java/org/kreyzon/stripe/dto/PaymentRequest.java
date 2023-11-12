package org.kreyzon.stripe.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long amount;
    private Long quantity;
    private String currency;
    private String successUrl;
    private String cancelUrl;
}
