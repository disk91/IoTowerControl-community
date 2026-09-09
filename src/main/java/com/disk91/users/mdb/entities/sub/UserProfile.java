package com.disk91.users.mdb.entities.sub;

import com.disk91.common.tools.CloneableObject;
import com.disk91.common.tools.CustomField;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;

public class UserProfile implements CloneableObject<UserProfile> {

    // User first name (encrypted)
    @Schema(
            description = "User First name",
            example = "John",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String firstName;

    // User last name (encrypted)
    @Schema(
            description = "User Last name",
            example = "Doe",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String lastName;

    // User gender (free text, front decides) encrypted
    @Schema(
            description = "User Gender",
            example = "Mr",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String gender;

    // phone number (encrypted) - e164 format, ex: +33601020304
    @Schema(
            description = "User Phone number",
            example = "+33601020304",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phoneNumber;

    // phone number hash for search. null = not hashed yet, "" no phone number
    @Schema(
            description = "Phone number hash for search, shall not be exported in the API",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phoneHash = null;

    // address (encrypted)
    @Schema(
            description = "User Address",
            example = "123 Main St",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address;

    // city (encrypted)
    @Schema(
            description = "User City",
            example = "Paris",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String city;

    // zip code (encrypted)
    @Schema(
            description = "User Zip code",
            example = "75001",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String zipCode;

    // iso country code (encrypted) - expl : FR, US, etc.
    @Schema(
            description = "User ISO Country",
            example = "FR",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String country;

    // time zone (ex CET)
    @Schema(
            description = "User Timezone",
            example = "CET",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String timezone;

    // Custom fields, contains key / value pairs, encrypted
    @Schema(
            description = "List of profile custom fields as a key",
            example = "[ { name : xxx, value : xxxx }, ...  ]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private ArrayList<CustomField> customFields;

    // === GETTER / SETTER ===

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ArrayList<CustomField> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(ArrayList<CustomField> customFields) {
        this.customFields = customFields;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPhoneHash() {
        return phoneHash;
    }

    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }

    // === CLONE ===

    public UserProfile clone() {
        // In case of modification, we need to modify UserBillingProfile.clone() as well
        UserProfile u = new UserProfile();
        u.setFirstName(this.firstName);
        u.setLastName(this.lastName);
        u.setGender(this.gender);
        u.setPhoneNumber(this.phoneNumber);
        u.setPhoneHash(this.phoneHash);
        u.setAddress(this.address);
        u.setCity(this.city);
        u.setZipCode(this.zipCode);
        u.setCountry(this.country);
        u.setTimezone(this.timezone);
        if (this.customFields != null) {
            ArrayList<CustomField> cf = new ArrayList<>();
            for (CustomField c : this.customFields) {
                cf.add(c.clone());
            }
            u.setCustomFields(cf);
        }
        return u;
    }

}
