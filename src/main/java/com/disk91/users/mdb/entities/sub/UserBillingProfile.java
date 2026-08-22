package com.disk91.users.mdb.entities.sub;

import com.disk91.common.tools.CustomField;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;

public class UserBillingProfile extends UserProfile {

    // Name of the company (encrypted)
    @Schema(
            description = "Name of the company",
            example = "Acme Inc.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String companyName;

    // 2 digits country code (possible extension for more precision, if empty, use county)
    @Schema(
            description = "2 digits country code (possible extension for more precision, if empty, use county)",
            example = "FR",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String countryCode;

    // VAT number
    @Schema(
            description = "TAX number",
            example = "FR123456789",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String vatNumber;

    // === GETTER / SETTER ===

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }


    // === CLONE ===

    public UserBillingProfile clone() {
        UserBillingProfile u = new UserBillingProfile();
        u.setFirstName(this.getFirstName());
        u.setLastName(this.getLastName());
        u.setGender(this.getGender());
        u.setPhoneNumber(this.getPhoneNumber());
        u.setPhoneHash(this.getPhoneHash());
        u.setAddress(this.getAddress());
        u.setCity(this.getCity());
        u.setZipCode(this.getZipCode());
        u.setCountry(this.getCountry());
        u.setTimezone(this.getTimezone());
        if (this.getCustomFields() != null) {
            ArrayList<CustomField> cf = new ArrayList<>();
            for (CustomField c : this.getCustomFields()) {
                cf.add(c.clone());
            }
            u.setCustomFields(cf);
        }
        u.setCompanyName(this.companyName);
        u.setCountryCode(this.countryCode);
        u.setVatNumber(this.vatNumber);
        return u;
    }

}
