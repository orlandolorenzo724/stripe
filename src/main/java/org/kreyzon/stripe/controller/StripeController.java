package org.kreyzon.stripe.controller;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.kreyzon.stripe.dto.PaymentRequest;
import org.kreyzon.stripe.dto.PaymentResponse;
import org.kreyzon.stripe.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kreyzon/api/v1/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = stripeService.createPaymentIntent(paymentRequest);
        return ResponseEntity
                .status(paymentResponse.getHttpStatus())
                .body(paymentResponse);
    }

    @PostMapping("/capture-payment")
    public ResponseEntity<PaymentResponse> capturePayment(@RequestParam String paymentIntentId) {
    PaymentResponse paymentResponse = stripeService.capturePayment(paymentIntentId);
          return ResponseEntity
                 .status(paymentResponse.getHttpStatus())
                 .body(paymentResponse);
     }
}

