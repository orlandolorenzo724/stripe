package org.kreyzon.stripe.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class PaymentResponse<T> {
    private String status;
    private String message;
    private Integer httpStatus;
    private T data;

    public PaymentResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public PaymentResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public PaymentResponse(String status, String message, Integer httpStatus) {
        this.status = status;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public PaymentResponse(String status, String message, Integer httpStatus, T data) {
        this.status = status;
        this.message = message;
        this.httpStatus = httpStatus;
        this.data = data;
    }
}
