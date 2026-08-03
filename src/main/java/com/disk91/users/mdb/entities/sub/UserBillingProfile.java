package com.disk91.users.mdb.entities.sub;

public class UserBillingProfile extends UserProfile {

    // Name of the company
    private String companyName;

    // 2 digits country code (possible extension for more precision, if empty, use county)
    private String countryCode;

    // VAT number
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
