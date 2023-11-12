/**
 * -----------------------------------------------------------------------------
 * This file is part of the Kreyzon Stripe open-source project.
 *
 * Kreyzon Stripe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kreyzon Stripe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with [Your Project Name]. If not, see <https://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * Author: Lorenzo Orlando
 * Created on: 2023-11-12
 *
 * -----------------------------------------------------------------------------
 */


package org.kreyzon.stripe.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCaptureParams;
import org.kreyzon.stripe.dto.PaymentResponse;
import org.kreyzon.stripe.dto.PaymentRequest;
import org.kreyzon.stripe.utils.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.Map;

/**
 * Service class for Stripe API calls.
 *
 * @author Lorenzo Orlando
 */
@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    /**
     * Creates a new payment intent.
     *
     * @param paymentRequest Payment request object
     * @return Payment response object
     */
    public PaymentResponse createPaymentIntent(PaymentRequest paymentRequest) {
        // Set your secret key. Remember to switch to your live secret key in production!
        Stripe.apiKey = secretKey;

        // Create a PaymentIntent with the order amount and currency
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(paymentRequest.getName())
                        .build();

        // Create new line item with the above product data and associated price
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(paymentRequest.getCurrency())
                        .setUnitAmount(paymentRequest.getAmount())
                        .setProductData(productData)
                        .build();

        // Create new line item with the above price data
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams
                        .LineItem.builder()
                        .setQuantity(paymentRequest.getQuantity())
                        .setPriceData(priceData)
                        .build();

        // Create new session with the line items
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(paymentRequest.getSuccessUrl())
                        .setCancelUrl(paymentRequest.getCancelUrl())
                        .addLineItem(lineItem)
                        .build();

        // Create new session
        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            e.printStackTrace();
            return PaymentResponse
                    .builder()
                    .status(Constant.SUCCESS)
                    .message("Payment session creation failed")
                    .httpStatus(400)
                    .data(null)
                    .build();
        }

        Map<String, String> responseData = session.getMetadata();
        responseData.put("sessionId", session.getId());
        responseData.put("sessionUrl", session.getUrl());

        return PaymentResponse
                .builder()
                .status(Constant.FAILURE)
                .message("Payment session created successfully")
                .httpStatus(200)
                .data(responseData)
                .build();
    }

    public PaymentResponse capturePayment(String paymentIntentId) {
        Stripe.apiKey = secretKey;

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder().build();
            paymentIntent.capture(params);
            return PaymentResponse
                    .builder()
                    .status(Constant.SUCCESS)
                    .message("Payment successfully captured.")
                    .httpStatus(200)
                    .data(paymentIntent)
                    .build();
        } catch (StripeException e) {
            // Handle capture failure, log the error, and return false
            e.printStackTrace();
            return PaymentResponse
                    .builder()
                    .status(Constant.FAILURE)
                    .message("Payment capture failed due to a server error.")
                    .httpStatus(500)
                    .data(null)
                    .build();
        }
    }
}
