/*
 * Copyright (c) - Paul Pinault (aka disk91) - 2026.
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 *    and associated documentation files (the "Software"), to deal in the Software without restriction,
 *    including without limitation the rights to use, copy, modify, merge, publish, distribute,
 *    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all copies or
 *    substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *    OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 *    IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.disk91.common.tools.drivers;

import com.disk91.common.config.CommonConfig;
import com.disk91.common.tools.Tools;
import com.disk91.common.tools.drivers.esendex.EsendexCreditsResponse;
import com.disk91.common.tools.drivers.esendex.EsendexMessageDispatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is a dummy implementation of the AbstractShortMessagesDriver for Esendex (esendex.com)
 * It simulates sending short messages without actually connecting to any service.
 */
public class EsendexShortMessagesDriver extends AbstractShortMessagesDriver {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected CommonConfig commonConfig;

    private boolean credentialsOk = false;
    private boolean credentialsVerified = false;

    @Override
    public ShortMessageResponse connect() {
        if ( credentialsVerified ) {
            return credentialsOk ? ShortMessageResponse.SMS_CONNECT_OK : ShortMessageResponse.SMS_CONNECT_KO;
        }
        credentialsVerified = true;

        // verify the credential existence
        if (       commonConfig.getShortMessagesEsendexEmail().isEmpty()
                || commonConfig.getShortMessagesEsendexPassword().isEmpty()
                || commonConfig.getShortMessagesEsendexAccount().isEmpty()
        ) {
            log.error(Tools.inRed("[common] EsendexShortMessagesDriver credentials are missing in the env file"));
            return ShortMessageResponse.SMS_CONNECT_KO;
        }
        credentialsOk = true;
        return  ShortMessageResponse.SMS_CONNECT_OK;
    }

    @Override
    public ShortMessageResponse sendShortMessage(
            String to,
            String text,
            String from
    ) {
        if (connect() != ShortMessageResponse.SMS_CONNECT_OK) {
            return ShortMessageResponse.SMS_SENT_KO_BAD_CREDENTIALS;
        }

        try {
            EsendexMessageDispatchRequest body = new EsendexMessageDispatchRequest();
            body.setAccountReference(commonConfig.getShortMessagesEsendexAccount());

            EsendexMessageDispatchRequest.EsendexMessageItem message = new EsendexMessageDispatchRequest.EsendexMessageItem();
            message.setTo(to);
            message.setBody(text);
            body.setMessages(java.util.List.of(message));

            org.springframework.web.reactive.function.client.WebClient webClient = org.springframework.web.reactive.function.client.WebClient.builder()
                    .baseUrl("https://api.esendex.com")
                    .defaultHeader(
                            org.springframework.http.HttpHeaders.AUTHORIZATION,
                            "Basic " + java.util.Base64.getEncoder().encodeToString(
                                    (commonConfig.getShortMessagesEsendexEmail() + ":" + commonConfig.getShortMessagesEsendexPassword())
                                            .getBytes(java.nio.charset.StandardCharsets.UTF_8)
                            )
                    )
                    .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(org.springframework.http.HttpHeaders.ACCEPT, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Integer statusCode = webClient.post()
                    .uri("/v1.0/messagedispatcher")
                    .bodyValue(body)
                    .exchangeToMono(response -> {
                        int status = response.statusCode().value();
                        if (status == 200) {
                            return reactor.core.publisher.Mono.just(status);
                        }
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errorBody -> {
                                    log.error("[common] Esendex SMS sending failed with HTTP status {} body={}", status, errorBody);
                                    return reactor.core.publisher.Mono.error(new RuntimeException("esendex-sms-http-" + status));
                                });
                    })
                    .block();
            if (statusCode == null || statusCode != 200) {
                return ShortMessageResponse.SMS_SENT_KO;
            }
            return ShortMessageResponse.SMS_SENT_OK;
        } catch (Exception e) {
            log.error("[common] Failed to send Esendex SMS to {}: {}", to, e.getMessage(), e);
            return ShortMessageResponse.SMS_SENT_KO;
        }
    }


    @Override
    public int getCreditsLeft() {
        if (connect() != ShortMessageResponse.SMS_CONNECT_OK) {
            return -1;
        }

        try {
            EsendexCreditsResponse response = callEsendexApi(
                    org.springframework.http.HttpMethod.GET,
                    "/v1.0/credits",
                    EsendexCreditsResponse.class
            );

            if (response == null || response.getPrepayCredits() == null) {
                log.debug("[common] Esendex credits response is empty");
                return -1;
            }

            String account = commonConfig.getShortMessagesEsendexAccount();

            // Search the bucket matching the configured account
            for (EsendexCreditsResponse.PrepayCreditItem credit : response.getPrepayCredits()) {
                if (account.equals(credit.getAllocatedTo())) {
                    log.debug("[common] Esendex credits found for account {}: {}", account, credit.getTotal());
                    return credit.getTotal();
                }
            }

            log.debug("[common] No Esendex credits bucket found for account {}", account);
            return -1;
        } catch (Exception e) {
            log.error("[common] Failed to retrieve Esendex credits", e);
            return -1;
        }
    }
    // ==================================================================
    // HELPERS
    // ==================================================================

    /**
     * Execute a generic Esendex API call with Basic authentication.
     * @param method - HTTP method to use
     * @param path - API path starting with /v1.0/...
     * @param responseType - Expected response class
     * @return The parsed response body
     * @throws RuntimeException when the HTTP call fails or the response cannot be parsed */
    protected <T> T callEsendexApi(
            org.springframework.http.HttpMethod method,
            String path,
            Class<T> responseType
    ) {
        // Build Basic authentication value from Esendex credentials
        String basicAuth = java.util.Base64.getEncoder().encodeToString(
                (commonConfig.getShortMessagesEsendexEmail() + ":" + commonConfig.getShortMessagesEsendexPassword())
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        // Create a compact WebClient for Esendex API calls
        org.springframework.web.reactive.function.client.WebClient webClient = org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl("https://admin.api.esendex.com")
                .defaultHeader(org.springframework.http.HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .defaultHeader(org.springframework.http.HttpHeaders.ACCEPT, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            return webClient.method(method)
                    .uri(path)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (Exception e) {
            log.error("[common] Failed Esendex API call {} {}", method, path, e);
            throw e;
        }
    }

}
