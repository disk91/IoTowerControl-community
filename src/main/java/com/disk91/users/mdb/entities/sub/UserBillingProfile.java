package com.disk91.users.mdb.entities.sub;

import io.swagger.v3.oas.annotations.media.Schema;

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
        u.setCompanyName(this.companyName);
        u.setCountryCode(this.countryCode);
        u.setVatNumber(this.vatNumber);
        return u;
    }

}
