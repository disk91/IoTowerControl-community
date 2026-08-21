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
package com.disk91.users.api.interfaces;

import com.disk91.common.tools.CustomField;
import com.disk91.users.mdb.entities.sub.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Update User Detailed Profile", description = "User profile with detailed information for profile update")
public class UserDetailedProfileBody {


    @Schema(
            description = "User login (hash) when not the requester itself.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    protected String login;

    @Schema(
            description = "User language",
            example = "en",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected String language;


    @Schema(
            description = "List of profile custom fields as a key, decrypted",
            example = "[ { name : xxx, value : xxxx }, ...  ]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    protected List<CustomField> customFields;

    @Schema(
            description = "Alert preferences",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected UserAlertPreference alertPreference;

    @Schema(
            description = "User profile",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected UserProfile profile;

    @Schema(
            description = "Billing profile",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected UserBillingProfile billingProfile;


    // ==========================
    // Getters & Setters


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<CustomField> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<CustomField> customFields) {
        this.customFields = customFields;
    }

    public UserAlertPreference getAlertPreference() {
        return alertPreference;
    }

    public void setAlertPreference(UserAlertPreference alertPreference) {
        this.alertPreference = alertPreference;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public UserBillingProfile getBillingProfile() {
        return billingProfile;
    }

    public void setBillingProfile(UserBillingProfile billingProfile) {
        this.billingProfile = billingProfile;
    }
}
