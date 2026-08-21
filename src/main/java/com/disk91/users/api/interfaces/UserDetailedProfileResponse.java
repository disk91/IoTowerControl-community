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
package com.disk91.users.api.interfaces;

import com.disk91.common.tools.CustomField;
import com.disk91.common.tools.Now;
import com.disk91.common.tools.exceptions.ITNotFoundException;
import com.disk91.common.tools.exceptions.ITParseException;
import com.disk91.users.mdb.entities.User;
import com.disk91.users.mdb.entities.sub.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "User Detailed Profile", description = "User profile with detailed information for front display")
public class UserDetailedProfileResponse {

    @Schema(
            description = "User Email",
            example = "john.doe@foo.bar",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    protected String email;

    @Schema(
            description = "User login (hash)",
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
            description = "User 2FA configuration",
            example = "NONE | EMAIL | SMS | AUTHENTICATOR",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    protected TwoFATypes twoFAConfig;

    @Schema(
            description = "Last login, ms since EPOC",
            example = "172545052000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected long lastLoginMs;

    @Schema(
            description = "Count login, since creation",
            example = "120",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected long loginCount;

    @Schema(
            description = "Registration date, ms since EPOC",
            example = "172545052000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected long registrationDate;

    @Schema(
            description = "Registration IP address (admin only)",
            example = "192.168.1.1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String registrationIp;

    @Schema(
            description = "True when user password is expired (admin only)",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Boolean passwordExpired;

    @Schema(
            description = "True when user is active (admin only)",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Boolean active;

    @Schema(
            description = "True when user is locked (admin only)",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Boolean locked;

    @Schema(
            description = "True when the user conditions have been validated (admin only)",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Boolean conditionValidation;

    @Schema(
            description = "Condition validation date, ms since EPOC (admin only)",
            example = "172545052000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Long conditionValidationDate;

    @Schema(
            description = "Condition validation version (admin only)",
            example = "172545052000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String conditionValidationVersion;

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

    @Schema(
            description = "Api keys (admin only)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected ArrayList<UserApiKeys> apiKeys;


    // ==========================
    // Build from User Object
    // (encryption key must be injected)
    public static UserDetailedProfileResponse of(
            User u,
            String encryptionKey,
            String applicationKey,
            boolean isRequesterAdmin
    ) throws ITParseException {
        UserDetailedProfileResponse r = new UserDetailedProfileResponse();
        try {
            u.setKeys(encryptionKey, applicationKey);
            r.setEmail(u.getEncEmail());
            r.setLogin(u.getLogin());
            r.setLanguage(u.getLanguage());
            r.setCustomFields(new ArrayList<>());
            if ( u.getCustomFields() != null) {
                for (CustomField cf : u.getCustomFields()) {
                    if ( !isRequesterAdmin && ( cf.getName().startsWith("hide_") || cf.getName().startsWith("chide_")) ) continue;
                    try {
                        r.getCustomFields().add(u.getEncCustomField(cf.getName()));
                    } catch (ITNotFoundException x) {
                        throw  new ITParseException(x.getMessage());
                    }
                }
            }
            r.setTwoFAConfig(u.getTwoFAType());
            r.setLastLoginMs(u.getLastLogin());
            r.setLoginCount(u.getCountLogin());
            r.setRegistrationDate(u.getRegistrationDate());
            r.setAlertPreference(u.getAlertPreference().clone());

            if ( isRequesterAdmin ) {
                r.setRegistrationIp(u.getEncRegistrationIP());
                r.setPasswordExpired((u.getPasswordResetExp() < Now.NowUtcMs()));
                r.setActive(u.isActive());
                r.setLocked(u.isLocked());
                r.setConditionValidation(u.isConditionValidation());
                r.setConditionValidationDate(u.getConditionValidationDate());
                r.setConditionValidationVersion(u.getConditionValidationVer());
                r.setApiKeys(new ArrayList<>());
                for ( UserApiKeys k : u.getApiKeys() ) {
                    UserApiKeys key = k.clone();
                    key.setSecret(null);
                }
            } else {
                r.setRegistrationIp(null);
                r.setPasswordExpired(null);
                r.setActive(null);
                r.setLocked(null);
                r.setConditionValidation(null);
                r.setConditionValidationDate(null);
                r.setConditionValidationVersion(null);
                r.setApiKeys(null);
            }

            // User Profile
            r.setProfile(new UserProfile());
            r.getProfile().setFirstName(u.getEncProfileFirstName());
            r.getProfile().setLastName(u.getEncProfileLastName());
            r.getProfile().setGender(u.getEncProfileGender());
            r.getProfile().setPhoneNumber(u.getEncProfilePhone());
            r.getProfile().setPhoneHash(null);
            r.getProfile().setAddress(u.getEncProfileAddress());
            r.getProfile().setCity(u.getEncProfileCity());
            r.getProfile().setZipCode(u.getEncProfileZipCode());
            r.getProfile().setCountry(u.getEncProfileCountry());
            r.getProfile().setTimezone(u.getEncProfileTimezone());
            if ( u.getProfile().getCustomFields() != null) {
                for (CustomField cf : u.getEncProfileCustomFields()) {
                    if ( !isRequesterAdmin && ( cf.getName().startsWith("hide_") || cf.getName().startsWith("chide_")) ) continue;
                    r.getProfile().getCustomFields().add(cf.clone());
                }
            }
            // Billing Profile
            r.setBillingProfile(new UserBillingProfile());
            r.getBillingProfile().setFirstName(u.getEncBillingFirstName());
            r.getBillingProfile().setLastName(u.getEncBillingLastName());
            r.getBillingProfile().setGender(u.getEncBillingGender());
            r.getBillingProfile().setPhoneNumber(u.getEncBillingPhone());
            r.getBillingProfile().setPhoneHash(null);
            r.getBillingProfile().setAddress(u.getEncBillingAddress());
            r.getBillingProfile().setCity(u.getEncBillingCity());
            r.getBillingProfile().setZipCode(u.getEncBillingZipCode());
            r.getBillingProfile().setCountry(u.getEncBillingCountry());
            r.getBillingProfile().setTimezone(u.getEncBillingTimezone());
            if ( u.getBillingProfile().getCustomFields() != null) {
                for (CustomField cf : u.getEncBillingCustomFields()) {
                    if ( !isRequesterAdmin && ( cf.getName().startsWith("hide_") || cf.getName().startsWith("chide_")) ) continue;
                    r.getBillingProfile().getCustomFields().add(cf.clone());
                }
            }
            r.getBillingProfile().setCompanyName(u.getEncBillingCompanyName());
            r.getBillingProfile().setCountryCode(u.getEncBillingCountryCode());
            r.getBillingProfile().setVatNumber(u.getEncBillingVatNumber());

        } finally {
            u.cleanKeys();
        }
        return r;
    }


    // ==========================
    // Getters & Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public TwoFATypes getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFATypes twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public long getLastLoginMs() {
        return lastLoginMs;
    }

    public void setLastLoginMs(long lastLoginMs) {
        this.lastLoginMs = lastLoginMs;
    }

    public long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(long loginCount) {
        this.loginCount = loginCount;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRegistrationIp() {
        return registrationIp;
    }

    public void setRegistrationIp(String registrationIp) {
        this.registrationIp = registrationIp;
    }

    public Boolean getPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(Boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getConditionValidation() {
        return conditionValidation;
    }

    public void setConditionValidation(Boolean conditionValidation) {
        this.conditionValidation = conditionValidation;
    }

    public Long getConditionValidationDate() {
        return conditionValidationDate;
    }

    public void setConditionValidationDate(Long conditionValidationDate) {
        this.conditionValidationDate = conditionValidationDate;
    }

    public String getConditionValidationVersion() {
        return conditionValidationVersion;
    }

    public void setConditionValidationVersion(String conditionValidationVersion) {
        this.conditionValidationVersion = conditionValidationVersion;
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

    public ArrayList<UserApiKeys> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(ArrayList<UserApiKeys> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}
