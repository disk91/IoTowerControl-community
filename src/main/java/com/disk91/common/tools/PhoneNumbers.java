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

import java.util.Arrays;
import java.util.Comparator;

/**
 * Tools for phone number manipulation and validation.
 * Provides a complete E164 country code table and static parsing functions.
 */
public class PhoneNumbers {

    /**
     * E164 country code entry mapping an international calling prefix to a country.
     * @param prefix    - calling code digits only (e.g. "33" for France)
     * @param prefixLen - number of digits in the calling code (e.g. 2)
     * @param iso       - ISO 3166-1 alpha-2 country code (e.g. "FR")
     * @param name      - country name in English
     * @param trunk     - national trunk prefix prepended when building the national format (e.g. "0"),
     *                    empty string when the country does not use a trunk prefix
     * @param format    - national format template; '#' is replaced by successive digits of (trunk + localNumber);
     *                    literal characters (spaces, dashes, parentheses) are kept as-is;
     *                    null triggers a generic grouping heuristic
     */
    public record E164Country(String prefix, int prefixLen, String iso, String name, String trunk, String format) {}

    /**
     * Result of parsing a complete E164 phone number.
     * @param internationalPrefix - e.g. "+33"
     * @param localNumber         - subscriber digits after the prefix (e.g. "612345678")
     * @param nationalFormat      - conventional national representation (e.g. "06 12 34 56 78")
     * @param iso                 - ISO 3166-1 alpha-2 country code (e.g. "FR")
     */
    public record PhoneParseResult(String internationalPrefix, String localNumber, String nationalFormat, String iso) {}

    /**
     * E164 country code table, sorted by prefix length descending to ensure
     * longest-match parsing (e.g. "212" matches before "2").
     * format=null means the generic grouping heuristic applies.
     */
    public static final E164Country[] COUNTRIES;

    static {
        E164Country[] raw = {

            // ---- 1-digit prefixes ----
            new E164Country("1", 1, "US", "United States / NANP", "",  "(###) ###-####"),
            new E164Country("7", 1, "RU", "Russia",               "8", "### ###-##-##"),

            // ---- 2-digit prefixes ----
            new E164Country("20", 2, "EG", "Egypt",               "0",  "### #### ###"),
            new E164Country("27", 2, "ZA", "South Africa",        "0",  "## ### ####"),
            new E164Country("30", 2, "GR", "Greece",              "",   "### ### ####"),
            new E164Country("31", 2, "NL", "Netherlands",         "0",  null),
            new E164Country("32", 2, "BE", "Belgium",             "0",  "### ## ## ##"),
            new E164Country("33", 2, "FR", "France",              "0",  "## ## ## ## ##"),
            new E164Country("34", 2, "ES", "Spain",               "",   "### ### ###"),
            new E164Country("36", 2, "HU", "Hungary",             "06", "## ### ####"),
            new E164Country("39", 2, "IT", "Italy",               "",   null),
            new E164Country("40", 2, "RO", "Romania",             "0",  "### ### ###"),
            new E164Country("41", 2, "CH", "Switzerland",         "0",  "## ### ## ##"),
            new E164Country("43", 2, "AT", "Austria",             "0",  null),
            new E164Country("44", 2, "GB", "United Kingdom",      "0",  "##### ######"),
            new E164Country("45", 2, "DK", "Denmark",             "",   "## ## ## ##"),
            new E164Country("46", 2, "SE", "Sweden",              "0",  null),
            new E164Country("47", 2, "NO", "Norway",              "",   "### ## ###"),
            new E164Country("48", 2, "PL", "Poland",              "",   "### ### ###"),
            new E164Country("49", 2, "DE", "Germany",             "0",  null),
            new E164Country("51", 2, "PE", "Peru",                "0",  "### ### ###"),
            new E164Country("52", 2, "MX", "Mexico",              "",   "## #### ####"),
            new E164Country("53", 2, "CU", "Cuba",                "0",  "# ### ####"),
            new E164Country("54", 2, "AR", "Argentina",           "0",  "### ####-####"),
            new E164Country("55", 2, "BR", "Brazil",              "0",  "## #####-####"),
            new E164Country("56", 2, "CL", "Chile",               "0",  "# #### ####"),
            new E164Country("57", 2, "CO", "Colombia",            "0",  "### ### ####"),
            new E164Country("58", 2, "VE", "Venezuela",           "0",  "### ### ####"),
            new E164Country("60", 2, "MY", "Malaysia",            "0",  null),
            new E164Country("61", 2, "AU", "Australia",           "0",  "### ### ###"),
            new E164Country("62", 2, "ID", "Indonesia",           "0",  "### #### ####"),
            new E164Country("63", 2, "PH", "Philippines",         "0",  "### ### ####"),
            new E164Country("64", 2, "NZ", "New Zealand",         "0",  "## ### ####"),
            new E164Country("65", 2, "SG", "Singapore",           "",   "#### ####"),
            new E164Country("66", 2, "TH", "Thailand",            "0",  "## ### ####"),
            new E164Country("81", 2, "JP", "Japan",               "0",  null),
            new E164Country("82", 2, "KR", "South Korea",         "0",  null),
            new E164Country("84", 2, "VN", "Vietnam",             "0",  "### ### ####"),
            new E164Country("86", 2, "CN", "China",               "0",  "### #### ####"),
            new E164Country("90", 2, "TR", "Turkey",              "0",  "### ### ## ##"),
            new E164Country("91", 2, "IN", "India",               "",   "##### #####"),
            new E164Country("92", 2, "PK", "Pakistan",            "0",  "### #######"),
            new E164Country("93", 2, "AF", "Afghanistan",         "0",  "## ### ####"),
            new E164Country("94", 2, "LK", "Sri Lanka",           "0",  "## ### ####"),
            new E164Country("95", 2, "MM", "Myanmar",             "0",  "## ### ####"),
            new E164Country("98", 2, "IR", "Iran",                "0",  "### ### ####"),

            // ---- 3-digit prefixes — Africa ----
            new E164Country("212", 3, "MA", "Morocco",                          "0",  "### ## ## ##"),
            new E164Country("213", 3, "DZ", "Algeria",                          "0",  "### ## ## ##"),
            new E164Country("216", 3, "TN", "Tunisia",                          "",   "## ### ###"),
            new E164Country("218", 3, "LY", "Libya",                            "0",  "## ### ####"),
            new E164Country("220", 3, "GM", "Gambia",                           "",   "### ####"),
            new E164Country("221", 3, "SN", "Senegal",                          "",   "## ### ## ##"),
            new E164Country("222", 3, "MR", "Mauritania",                       "",   "## ## ## ##"),
            new E164Country("223", 3, "ML", "Mali",                             "",   "## ## ## ##"),
            new E164Country("224", 3, "GN", "Guinea",                           "",   "### ## ## ##"),
            new E164Country("225", 3, "CI", "Côte d'Ivoire",                    "",   "## ## ## ## ##"),
            new E164Country("226", 3, "BF", "Burkina Faso",                     "",   "## ## ## ##"),
            new E164Country("227", 3, "NE", "Niger",                            "",   "## ## ## ##"),
            new E164Country("228", 3, "TG", "Togo",                             "",   "## ## ## ##"),
            new E164Country("229", 3, "BJ", "Benin",                            "",   "## ## ## ##"),
            new E164Country("230", 3, "MU", "Mauritius",                        "",   "#### ####"),
            new E164Country("231", 3, "LR", "Liberia",                          "",   "### ### ####"),
            new E164Country("232", 3, "SL", "Sierra Leone",                     "",   "## ######"),
            new E164Country("233", 3, "GH", "Ghana",                            "0",  "## ### ####"),
            new E164Country("234", 3, "NG", "Nigeria",                          "0",  "### ### ####"),
            new E164Country("235", 3, "TD", "Chad",                             "",   "## ## ## ##"),
            new E164Country("236", 3, "CF", "Central African Republic",         "",   "## ## ## ##"),
            new E164Country("237", 3, "CM", "Cameroon",                         "",   "## ## ## ##"),
            new E164Country("238", 3, "CV", "Cape Verde",                       "",   "### ## ##"),
            new E164Country("239", 3, "ST", "São Tomé and Príncipe",            "",   "### ####"),
            new E164Country("240", 3, "GQ", "Equatorial Guinea",                "",   "### ### ###"),
            new E164Country("241", 3, "GA", "Gabon",                            "",   "## ## ## ##"),
            new E164Country("242", 3, "CG", "Republic of the Congo",            "",   "## ### ####"),
            new E164Country("243", 3, "CD", "Democratic Republic of the Congo", "0",  "## ### ####"),
            new E164Country("244", 3, "AO", "Angola",                           "",   "### ### ###"),
            new E164Country("245", 3, "GW", "Guinea-Bissau",                    "",   "### ## ##"),
            new E164Country("246", 3, "IO", "British Indian Ocean Territory",   "",   "### ####"),
            new E164Country("247", 3, "SH", "Ascension Island",                 "",   "#####"),
            new E164Country("248", 3, "SC", "Seychelles",                       "",   "# ## ## ##"),
            new E164Country("249", 3, "SD", "Sudan",                            "0",  "## ### ####"),
            new E164Country("250", 3, "RW", "Rwanda",                           "0",  "### ### ###"),
            new E164Country("251", 3, "ET", "Ethiopia",                         "0",  "## ### ####"),
            new E164Country("252", 3, "SO", "Somalia",                          "",   "## ### ###"),
            new E164Country("253", 3, "DJ", "Djibouti",                         "",   "## ## ## ##"),
            new E164Country("254", 3, "KE", "Kenya",                            "0",  "### ######"),
            new E164Country("255", 3, "TZ", "Tanzania",                         "0",  "### ### ###"),
            new E164Country("256", 3, "UG", "Uganda",                           "0",  "### ######"),
            new E164Country("257", 3, "BI", "Burundi",                          "",   "## ## ## ##"),
            new E164Country("258", 3, "MZ", "Mozambique",                       "",   "## ### ####"),
            new E164Country("260", 3, "ZM", "Zambia",                           "0",  "## ### ####"),
            new E164Country("261", 3, "MG", "Madagascar",                       "0",  "## ## ### ##"),
            new E164Country("262", 3, "RE", "Réunion / Mayotte",                "0",  "### ## ## ##"),
            new E164Country("263", 3, "ZW", "Zimbabwe",                         "0",  "## ### ####"),
            new E164Country("264", 3, "NA", "Namibia",                          "0",  "## ### ####"),
            new E164Country("265", 3, "MW", "Malawi",                           "0",  "### ## ## ##"),
            new E164Country("266", 3, "LS", "Lesotho",                          "",   "## ## ## ##"),
            new E164Country("267", 3, "BW", "Botswana",                         "",   "## ### ###"),
            new E164Country("268", 3, "SZ", "Eswatini",                         "",   "## ## ## ##"),
            new E164Country("269", 3, "KM", "Comoros",                          "",   "### ## ##"),
            new E164Country("290", 3, "SH", "Saint Helena",                     "",   "####"),
            new E164Country("291", 3, "ER", "Eritrea",                          "0",  "## ### ###"),
            new E164Country("297", 3, "AW", "Aruba",                            "",   "### ####"),
            new E164Country("298", 3, "FO", "Faroe Islands",                    "",   "### ###"),
            new E164Country("299", 3, "GL", "Greenland",                        "",   "## ## ##"),

            // ---- 3-digit prefixes — Europe ----
            new E164Country("350", 3, "GI", "Gibraltar",              "",  "### #####"),
            new E164Country("351", 3, "PT", "Portugal",               "",  "### ### ###"),
            new E164Country("352", 3, "LU", "Luxembourg",             "",  "### ### ###"),
            new E164Country("353", 3, "IE", "Ireland",                "0", "## ### ####"),
            new E164Country("354", 3, "IS", "Iceland",                "",  "### ####"),
            new E164Country("355", 3, "AL", "Albania",                "0", "## ### ####"),
            new E164Country("356", 3, "MT", "Malta",                  "",  "#### ####"),
            new E164Country("357", 3, "CY", "Cyprus",                 "",  "## ### ###"),
            new E164Country("358", 3, "FI", "Finland",                "0", "## ### ####"),
            new E164Country("359", 3, "BG", "Bulgaria",               "0", "## ### ####"),
            new E164Country("370", 3, "LT", "Lithuania",              "0", "### ## ###"),
            new E164Country("371", 3, "LV", "Latvia",                 "",  "## ### ###"),
            new E164Country("372", 3, "EE", "Estonia",                "",  "### ####"),
            new E164Country("373", 3, "MD", "Moldova",                "0", "### ## ###"),
            new E164Country("374", 3, "AM", "Armenia",                "0", "## ### ###"),
            new E164Country("375", 3, "BY", "Belarus",                "0", "## ### ## ##"),
            new E164Country("376", 3, "AD", "Andorra",                "",  "### ###"),
            new E164Country("377", 3, "MC", "Monaco",                 "",  "## ## ## ##"),
            new E164Country("378", 3, "SM", "San Marino",             "",  "## ## ## ##"),
            new E164Country("380", 3, "UA", "Ukraine",                "0", "## ### ## ##"),
            new E164Country("381", 3, "RS", "Serbia",                 "0", "## ### ####"),
            new E164Country("382", 3, "ME", "Montenegro",             "0", "## ### ###"),
            new E164Country("385", 3, "HR", "Croatia",                "0", "## ### ###"),
            new E164Country("386", 3, "SI", "Slovenia",               "0", "## ### ###"),
            new E164Country("387", 3, "BA", "Bosnia and Herzegovina", "0", "## ### ###"),
            new E164Country("389", 3, "MK", "North Macedonia",        "0", "## ### ###"),
            new E164Country("420", 3, "CZ", "Czech Republic",         "0", "### ### ###"),
            new E164Country("421", 3, "SK", "Slovakia",               "0", "### ### ###"),
            new E164Country("423", 3, "LI", "Liechtenstein",          "",  "### ####"),

            // ---- 3-digit prefixes — Americas ----
            new E164Country("500", 3, "FK", "Falkland Islands",             "",  "#####"),
            new E164Country("501", 3, "BZ", "Belize",                       "",  "### ####"),
            new E164Country("502", 3, "GT", "Guatemala",                    "",  "#### ####"),
            new E164Country("503", 3, "SV", "El Salvador",                  "",  "#### ####"),
            new E164Country("504", 3, "HN", "Honduras",                     "",  "#### ####"),
            new E164Country("505", 3, "NI", "Nicaragua",                    "",  "#### ####"),
            new E164Country("506", 3, "CR", "Costa Rica",                   "",  "#### ####"),
            new E164Country("507", 3, "PA", "Panama",                       "",  "#### ####"),
            new E164Country("508", 3, "PM", "Saint Pierre and Miquelon",    "0", "## ## ##"),
            new E164Country("509", 3, "HT", "Haiti",                        "",  "#### ####"),
            new E164Country("590", 3, "GP", "Guadeloupe",                   "0", "### ## ## ##"),
            new E164Country("591", 3, "BO", "Bolivia",                      "0", "### ## ## ##"),
            new E164Country("592", 3, "GY", "Guyana",                       "",  "### ####"),
            new E164Country("593", 3, "EC", "Ecuador",                      "0", "## ### ####"),
            new E164Country("594", 3, "GF", "French Guiana",                "0", "### ## ## ##"),
            new E164Country("595", 3, "PY", "Paraguay",                     "0", "### ### ###"),
            new E164Country("596", 3, "MQ", "Martinique",                   "0", "### ## ## ##"),
            new E164Country("597", 3, "SR", "Suriname",                     "",  "### ####"),
            new E164Country("598", 3, "UY", "Uruguay",                      "0", "### ### ###"),
            new E164Country("599", 3, "CW", "Curaçao / Netherlands Antilles","", "### ####"),

            // ---- 3-digit prefixes — Asia / Pacific ----
            new E164Country("670", 3, "TL", "East Timor",           "",  "### ####"),
            new E164Country("672", 3, "NF", "Norfolk Island",       "",  "### ###"),
            new E164Country("673", 3, "BN", "Brunei",               "",  "### ####"),
            new E164Country("674", 3, "NR", "Nauru",                "",  "### ####"),
            new E164Country("675", 3, "PG", "Papua New Guinea",     "",  "### ####"),
            new E164Country("676", 3, "TO", "Tonga",                "",  "#####"),
            new E164Country("677", 3, "SB", "Solomon Islands",      "",  "#####"),
            new E164Country("678", 3, "VU", "Vanuatu",              "",  "#####"),
            new E164Country("679", 3, "FJ", "Fiji",                 "",  "### ####"),
            new E164Country("680", 3, "PW", "Palau",                "",  "### ####"),
            new E164Country("681", 3, "WF", "Wallis and Futuna",    "",  "## ## ##"),
            new E164Country("682", 3, "CK", "Cook Islands",         "",  "#####"),
            new E164Country("683", 3, "NU", "Niue",                 "",  "####"),
            new E164Country("685", 3, "WS", "Samoa",                "",  "## ####"),
            new E164Country("686", 3, "KI", "Kiribati",             "",  "#####"),
            new E164Country("687", 3, "NC", "New Caledonia",        "",  "## ## ##"),
            new E164Country("688", 3, "TV", "Tuvalu",               "",  "#####"),
            new E164Country("689", 3, "PF", "French Polynesia",     "",  "## ## ## ##"),
            new E164Country("690", 3, "TK", "Tokelau",              "",  "####"),
            new E164Country("691", 3, "FM", "Micronesia",           "",  "### ####"),
            new E164Country("692", 3, "MH", "Marshall Islands",     "",  "### ####"),
            new E164Country("850", 3, "KP", "North Korea",          "0", "## ### ####"),
            new E164Country("852", 3, "HK", "Hong Kong",            "",  "#### ####"),
            new E164Country("853", 3, "MO", "Macau",                "",  "#### ####"),
            new E164Country("855", 3, "KH", "Cambodia",             "0", "## ### ###"),
            new E164Country("856", 3, "LA", "Laos",                 "0", "## ### ###"),
            new E164Country("880", 3, "BD", "Bangladesh",           "0", "### ### ####"),
            new E164Country("886", 3, "TW", "Taiwan",               "0", "## #### ####"),

            // ---- 3-digit prefixes — Middle East / Central Asia ----
            new E164Country("960", 3, "MV", "Maldives",             "",  "### ####"),
            new E164Country("961", 3, "LB", "Lebanon",              "0", "## ### ###"),
            new E164Country("962", 3, "JO", "Jordan",               "0", "# #### ####"),
            new E164Country("963", 3, "SY", "Syria",                "0", "### ### ###"),
            new E164Country("964", 3, "IQ", "Iraq",                 "0", "### ### ####"),
            new E164Country("965", 3, "KW", "Kuwait",               "",  "#### ####"),
            new E164Country("966", 3, "SA", "Saudi Arabia",         "0", "### ### ####"),
            new E164Country("967", 3, "YE", "Yemen",                "0", "### ### ###"),
            new E164Country("968", 3, "OM", "Oman",                 "",  "#### ####"),
            new E164Country("970", 3, "PS", "Palestinian Territory","0", "### ### ###"),
            new E164Country("971", 3, "AE", "United Arab Emirates", "0", "## ### ####"),
            new E164Country("972", 3, "IL", "Israel",               "0", "##-### ####"),
            new E164Country("973", 3, "BH", "Bahrain",              "",  "#### ####"),
            new E164Country("974", 3, "QA", "Qatar",                "",  "#### ####"),
            new E164Country("975", 3, "BT", "Bhutan",               "",  "## ### ###"),
            new E164Country("976", 3, "MN", "Mongolia",             "0", "#### ####"),
            new E164Country("977", 3, "NP", "Nepal",                "0", "##-### ####"),
            new E164Country("992", 3, "TJ", "Tajikistan",           "0", "### ## ## ##"),
            new E164Country("993", 3, "TM", "Turkmenistan",         "0", "## ## ## ##"),
            new E164Country("994", 3, "AZ", "Azerbaijan",           "0", "## ### ## ##"),
            new E164Country("995", 3, "GE", "Georgia",              "0", "### ## ## ##"),
            new E164Country("996", 3, "KG", "Kyrgyzstan",           "0", "### ## ## ##"),
            new E164Country("998", 3, "UZ", "Uzbekistan",           "0", "## ### ## ##"),
        };

        // Sort by prefix length descending so that longer prefixes are matched first
        COUNTRIES = Arrays.stream(raw)
                .sorted(Comparator.comparingInt(E164Country::prefixLen).reversed())
                .toArray(E164Country[]::new);
    }

    // =====================================================================================================
    // Public API
    // =====================================================================================================

    /**
     * Parse a phone number in international E164 format and decompose it into its components.
     * Accepts numbers starting with '+', with '00' as the international dialing prefix, or with
     * bare country code digits. National format numbers (starting with a trunk prefix such as '0')
     * cannot be reliably parsed and will return null.
     * Non-digit characters (spaces, dashes, dots, parentheses) are stripped before parsing.
     *
     * @param phone - phone number string (e.g. "+33612345678", "0033612345678", "+1 (212) 555-1234")
     * @return PhoneParseResult with internationalPrefix, localNumber, nationalFormat and iso;
     *         or null when no matching country code is found
     */
    public static PhoneParseResult parse(String phone) {
        if (phone == null || phone.isBlank()) return null;

        // Strip all non-digit characters (spaces, dashes, dots, parentheses, leading '+')
        String digits = phone.trim().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;

        // "00" used as international dialing prefix is equivalent to "+"
        if (digits.startsWith("00")) digits = digits.substring(2);

        // Longest-match: the array is already sorted by prefixLen descending
        for (E164Country country : COUNTRIES) {
            if (digits.startsWith(country.prefix())) {
                String local = digits.substring(country.prefix().length());
                String national = applyFormat(country.trunk() + local, country.format());
                return new PhoneParseResult("+" + country.prefix(), local, national, country.iso());
            }
        }
        return null;
    }

    // =====================================================================================================
    // Private helpers
    // =====================================================================================================

    /**
     * Apply a national format template to a digit string.
     * '#' in the template is replaced by successive digits; all other characters are kept as literals.
     * Digits remaining after the template is exhausted are appended at the end without separator.
     * When format is null or empty, a generic space-separated grouping heuristic is applied.
     *
     * @param digits - full national digit string (trunk prefix prepended to local number)
     * @param format - format template (may be null for generic grouping)
     * @return formatted phone number string
     */
    private static String applyFormat(String digits, String format) {
        if (digits == null || digits.isEmpty()) return "";
        if (format == null || format.isEmpty()) return genericFormat(digits);

        int di = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : format.toCharArray()) {
            if (c == '#') {
                if (di < digits.length()) sb.append(digits.charAt(di++));
            } else {
                sb.append(c);
            }
        }
        // Append any remaining digits not covered by the format template
        while (di < digits.length()) sb.append(digits.charAt(di++));
        return sb.toString();
    }

    /**
     * Generic digit grouping heuristic for countries without a specific format template.
     * Groups digits left-to-right into blocks of 3, with the last block allowed to be 3 or 4.
     *
     * @param digits - national digit string (trunk + local)
     * @return space-separated grouped representation
     */
    private static String genericFormat(String digits) {
        int len = digits.length();
        if (len <= 4) return digits;

        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (pos < len) {
            int remaining = len - pos;
            // Use block of 3 unless only 1 or 2 remain (merge into previous block)
            int blockSize = (remaining > 4) ? 3 : remaining;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(digits, pos, pos + blockSize);
            pos += blockSize;
        }
        return sb.toString();
    }

}
