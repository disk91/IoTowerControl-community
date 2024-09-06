/*
 * Copyright (c) - Paul Pinault (aka disk91) - 2024.
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

package com.disk91.users.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"file:configuration/users.properties"}, ignoreResourceNotFound = true)
public class UsersConfig {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // ----------------------------------------------
    // Common setup
    // ----------------------------------------------

    // Service Id used for multi-instance environment, must be different for every instances
    @Value("${common.service.id:83DZqvwbXzmtllVq}")
    private String commonServiceId;
    public String getCommonServiceId() {
        return (commonServiceId.isEmpty())?"83DZqvwbXzmtllVq":commonServiceId;
    }

    // Select the medium to be used for the intracom service communication
    @Value("${users.intracom.medium:db}")
    private String usersIntracomMedium;
    public String getUsersIntracomMedium() {
        return usersIntracomMedium;
    }
}
