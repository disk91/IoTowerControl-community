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

package com.disk91.common.tools;

import com.disk91.common.config.CommonConfig;
import com.disk91.common.tools.drivers.AbstractShortMessagesDriver;
import com.disk91.common.tools.drivers.ShortMessageResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
public class ShortMessagesTools {

    @Autowired(required = false)
    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    protected CommonConfig commonConfig;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private AbstractShortMessagesDriver driver;

    @PostConstruct
    public void init() {
        log.info("[common] ShortMessagesTools init");
        this.initShorMessageToolsMetrics();
        try {
            String driverClassName = commonConfig.getShortMessagesDriver();
            if ( driverClassName == null || driverClassName.isEmpty()) {
                log.warn("[common] No ShortMessagesDriver configured, using DummyShortMessagesDriver");
                driverClassName = "com.disk91.common.tools.drivers.DummyShortMessagesDriver";
            }
            Class<?> driverClass = Class.forName(driverClassName);
            driver = (AbstractShortMessagesDriver) beanFactory.createBean(driverClass);
            driver.getClass()
                    .getMethod("connect")
                    .invoke(driver);
        } catch (Exception e) {
            log.error("[common] Failed to initialize ShortMessagesTools", e);
        }
    }


    @Async
    public CompletableFuture<ShortMessageResponse> send(String to, String text, String from) {
        return CompletableFuture.supplyAsync(() -> sendSync(to, text, from));
    }


    public ShortMessageResponse sendSync(String to, String text, String from) {
        this.incTotalSms();

        if (driver == null) {
            this.incTotalSmsFailure();
            return ShortMessageResponse.SMS_DRIVER_NOT_READY;
        }

        // Check if the phone number is valid
        if (!to.matches("\\+?[1-9]\\d{1,14}")) {
            this.incTotalSmsFailure();
            return ShortMessageResponse.SMS_SENT_KO_BAD_NUMBER;
        }

        // Send the message
        try {

            ShortMessageResponse r = (ShortMessageResponse) driver.getClass()
                    .getMethod("connect")
                    .invoke(driver);

            if ( r != ShortMessageResponse.SMS_CONNECT_OK) {
                this.incTotalSmsFailure();
                return ShortMessageResponse.SMS_CONNECT_KO;
            }

            r = (ShortMessageResponse) driver.getClass()
                    .getMethod("sendShortMessage", String.class, String.class, String.class)
                    .invoke(driver, to, text, from);

            this.incTotalSmsSuccess();
            return r;
        } catch (Exception e) {
            log.error("[common] Failed to send short message ({})", e.getMessage());
            this.incTotalSmsFailure();
            return ShortMessageResponse.SMS_SENT_KO;
        }
    }

    public int getCreditsLeft() {
        if (driver == null) {
            return -1;
        }
        try {
            Integer credits = (Integer) driver.getClass()
                    .getMethod("getCreditsLeft")
                    .invoke(driver);
            return credits;
        } catch (Exception e) {
            log.error("[common] Failed to get credits left ({})", e.getMessage());
            return -1;
        }
    }

    // ================================================================================================================
    // Register Prometheus gauges for email tools metrics at startup.
    // ================================================================================================================

    @Autowired
    protected MeterRegistry meterRegistry;

    private volatile long statTotalSms = 0;
    protected synchronized void incTotalSms() {
        statTotalSms++;
    }
    protected Supplier<Number> getTotalSms() {
        return ()->statTotalSms;
    }

    private volatile long statTotalSmsSuccess = 0;
    protected  synchronized void incTotalSmsSuccess() {
        statTotalSmsSuccess++;
    }
    protected Supplier<Number> getTotalSmsSuccess() {
        return ()->statTotalSmsSuccess;
    }

    private volatile long statTotalSmsFailure = 0;
    protected synchronized void incTotalSmsFailure() {
        statTotalSmsFailure++;
    }
    protected Supplier<Number> getTotalSmsFailure() {
        return ()->statTotalSmsFailure;
    }

    private void initShorMessageToolsMetrics() {
        log.info("[common] Firebase short messages tools metrics initialized");
        Gauge.builder("common_sms_tools_total_sent", this.getTotalSms())
                .description("Total number of sms sent request")
                .register(meterRegistry);
        Gauge.builder("common_sms_tools_total_sent_success", this.getTotalSmsSuccess())
                .description("Total number of sms sent successfully")
                .register(meterRegistry);
        Gauge.builder("common_sms_tools_total_sent_failure", this.getTotalSmsFailure())
                .description("Total number of sms sent failure")
                .register(meterRegistry);
    }

}
