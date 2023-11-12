package org.kreyzon.stripe.controller;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.kreyzon.stripe.dto.PaymentRequest;
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
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) {
        try {
            String clientSecret = stripeService.createPaymentIntent(paymentRequest);
            return ResponseEntity.ok(clientSecret);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment intent creation failed");
        }
    }

    @PostMapping("/capture-payment")
    public ResponseEntity<String> capturePayment(@RequestParam String paymentIntentId) {
        try {
            boolean paymentCaptured = stripeService.capturePayment(paymentIntentId);
            if (paymentCaptured) {
                return ResponseEntity.ok("Payment successfully captured.");
            } else {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Payment capture failed.");
            }
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment capture failed due to a server error.");
        }
    }
}

