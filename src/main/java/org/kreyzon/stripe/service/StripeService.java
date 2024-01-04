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
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.stripe.dto.CapturePaymentResponse;
import org.kreyzon.stripe.dto.CreatePaymentResponse;
import org.kreyzon.stripe.dto.StripeResponse;
import org.kreyzon.stripe.dto.CreatePaymentRequest;
import org.kreyzon.stripe.utils.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Service class for Stripe API calls.
 *
 * @author Lorenzo Orlando
 */
@Service
@Slf4j
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    /**
     * Creates a new payment.
     *
     * @param createPaymentRequest Payment request object
     * @return Payment response object
     */
    public StripeResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        // Set your secret key. Remember to switch to your live secret key in production!
        Stripe.apiKey = secretKey;

        // Create a PaymentIntent with the order amount and currency
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(createPaymentRequest.getName())
                        .build();

        // Create new line item with the above product data and associated price
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(createPaymentRequest.getCurrency())
                        .setUnitAmount(createPaymentRequest.getAmount())
                        .setProductData(productData)
                        .build();

        // Create new line item with the above price data
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams
                        .LineItem.builder()
                        .setQuantity(createPaymentRequest.getQuantity())
                        .setPriceData(priceData)
                        .build();

        // Create new session with the line items
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(createPaymentRequest.getSuccessUrl())
                        .setCancelUrl(createPaymentRequest.getCancelUrl())
                        .addLineItem(lineItem)
                        .build();

        // Create new session
        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            e.printStackTrace();
            return StripeResponse
                    .builder()
                    .status(Constant.FAILURE)
                    .message("Payment session creation failed")
                    .httpStatus(400)
                    .data(null)
                    .build();
        }

        CreatePaymentResponse responseData = CreatePaymentResponse
                .builder()
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();

        return StripeResponse
                .builder()
                .status(Constant.SUCCESS)
                .message("Payment session created successfully")
                .httpStatus(200)
                .data(responseData)
                .build();
    }

    public StripeResponse capturePayment(String sessionId) {
        Stripe.apiKey = secretKey;

        try {
            Session session = Session.retrieve(sessionId);
            String status = session.getStatus();

            if (status.equalsIgnoreCase(Constant.STRIPE_SESSION_STATUS_SUCCESS)) {
                // Handle logic for successful payment
                log.info("Payment successfully captured.");
            }

            CapturePaymentResponse responseData = CapturePaymentResponse
                    .builder()
                    .sessionId(sessionId)
                    .sessionStatus(status)
                    .paymentStatus(session.getPaymentStatus())
                    .build();

            return StripeResponse
                    .builder()
                    .status(Constant.SUCCESS)
                    .message("Payment successfully captured.")
                    .httpStatus(200)
                    .data(responseData)
                    .build();
        } catch (StripeException e) {
            // Handle capture failure, log the error, and return false
            e.printStackTrace();
            return StripeResponse
                    .builder()
                    .status(Constant.FAILURE)
                    .message("Payment capture failed due to a server error.")
                    .httpStatus(500)
                    .data(null)
                    .build();
        }
    }
}