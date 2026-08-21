# Skill — User Profile Display

This document describes how to consume the user profile APIs and how the returned data
should be represented in a frontend. Two APIs are involved: a **basic** profile for
lightweight global display, and a **detailed** profile for full editing.

---

## 1. Basic Profile API

### Endpoint

```
GET /users/1.0/profile/basic
Authorization: Bearer <token>   (ROLE_LOGIN_COMPLETE required, API sessions excluded)
```

### Response — `UserBasicProfileResponse`

```json
{
  "email":               "john.doe@example.com",
  "login":               "a3f9…",
  "firstName":           "John",
  "lastName":            "Doe",
  "mobileNumber":        "+33601020304",
  "isoCountryCode":      "FR",
  "language":            "en",
  "passwordExpirationMs": 1900000000000,
  "lastComMessageSeen":  1720000000000,
  "twoFAConfig":         "NONE",
  "roles": [
    "ROLE_LOGIN_COMPLETE",
    "ROLE_REGISTERED_USER"
  ],
  "acls": [
    { "group": "my-group", "localName": "My Group", "roles": ["ROLE_DEVICE_READ"] }
  ],
  "customFields": [
    { "name": "basic_avatar_url", "value": "https://…" },
    { "name": "cbasic_theme",     "value": "dark" }
  ]
}
```

### Field notes

| Field | Description |
|---|---|
| `email` | Decrypted email address. May be empty before profile completion. |
| `login` | SHA-256 hash of the email, used as stable identifier across calls. |
| `firstName` / `lastName` | May be empty until the user fills in their profile. |
| `mobileNumber` | E.164 format (e.g. `+33601020304`). Empty string if not set. |
| `isoCountryCode` | ISO 3166-1 alpha-2 code linked to the phone number. |
| `language` | 2-letter language code (e.g. `en`, `fr`). |
| `passwordExpirationMs` | Epoch ms. Show a warning banner when < `Date.now()`. |
| `lastComMessageSeen` | Epoch ms. Compare against the newest communication timestamp to show an unread badge. |
| `twoFAConfig` | One of `NONE`, `EMAIL`, `SMS`, `AUTHENTICATOR`. |
| `roles` | Flat list of all roles. Drive feature flags and menu visibility from this list. |
| `acls` | Group-scoped roles. Each entry has `group`, `localName`, and `roles[]`. |
| `customFields` | **Only** fields whose name starts with `basic_` (encrypted) or `cbasic_` (clear). |

### Custom field name conventions (all APIs)

| Prefix | Encrypted | Visibility |
|---|---|---|
| `basic_` | yes | Basic profile + detailed profile (all users) |
| `cbasic_` | no | Basic profile + detailed profile (all users) |
| `hide_` | yes | Detailed profile — **admin only** |
| `chide_` | no | Detailed profile — **admin only** |
| *(other)* | yes | Detailed profile (all users) |

This convention applies equally at the root level, inside `profile.customFields`,
and inside `billingProfile.customFields`.

### Frontend representation

The basic profile response is used to populate persistent UI elements that are
present on every page after login:

- **Header / avatar area**: `firstName` + `lastName` (or `email` as fallback),
  `isoCountryCode` flag icon.
- **Notification badge**: compare `lastComMessageSeen` with the latest communication
  to show an unread count.
- **Password expiry banner**: display a dismissible alert when `passwordExpirationMs`
  is in the past or within 7 days.
- **Menu & feature flags**: derive from `roles` and `acls`.
- **2FA indicator**: small status chip (shield icon) driven by `twoFAConfig`.
- **App-specific fields**: iterate `customFields` (prefixed `basic_` / `cbasic_`)
  for any product-specific lightweight data (avatar URL, theme preference, etc.).

---

## 2. Detailed Profile API

### Endpoints

```
# Self
GET /users/1.0/profile/detailed
Authorization: Bearer <token>   (ROLE_LOGIN_COMPLETE, API sessions excluded)

# By admin (target user identified in path)
GET /users/1.0/profile/detailed/{login}
Authorization: Bearer <token>   (ROLE_LOGIN_COMPLETE + admin role checked server-side)
Path: login — the hashed login of the target user
```

Both endpoints return the same `UserDetailedProfileResponse` structure.
When the requester is an administrator, additional fields are populated (see below).

### Response — `UserDetailedProfileResponse`

```json
{
  "email":            "john.doe@example.com",
  "login":            "a3f9…",
  "language":         "en",
  "twoFAConfig":      "AUTHENTICATOR",
  "lastLoginMs":      1720000000000,
  "loginCount":       42,
  "registrationDate": 1600000000000,

  "alertPreference": {
    "emailAlert": true,
    "smsAlert":   false,
    "pushAlert":  false
  },

  "profile": {
    "firstName":   "John",
    "lastName":    "Doe",
    "gender":      "Mr",
    "phoneNumber": "+33601020304",
    "address":     "123 Main St",
    "city":        "Paris",
    "zipCode":     "75001",
    "country":     "FR",
    "timezone":    "Europe/Paris",
    "customFields": [
      { "name": "profile_nickname", "value": "Johnny" }
    ]
  },

  "billingProfile": {
    "firstName":   "John",
    "lastName":    "Doe",
    "gender":      "Mr",
    "phoneNumber": "+33601020304",
    "address":     "123 Main St",
    "city":        "Paris",
    "zipCode":     "75001",
    "country":     "FR",
    "timezone":    "Europe/Paris",
    "companyName": "Acme Inc.",
    "countryCode": "FR",
    "vatNumber":   "FR12345678901",
    "customFields": []
  },

  "customFields": [
    { "name": "basic_avatar_url", "value": "https://…" },
    { "name": "some_app_field",   "value": "…" }
  ],

  "registrationIp":            null,
  "passwordExpired":            null,
  "active":                     null,
  "locked":                     null,
  "conditionValidation":        null,
  "conditionValidationDate":    null,
  "conditionValidationVersion": null,
  "apiKeys":                    null
}
```

Fields that are `null` in the self-request are populated when the requester is an admin
(see [Admin-only fields](#admin-only-fields) below).

### Field notes — common

| Field | Description |
|---|---|
| `language` | 2-letter language code. Editable. |
| `twoFAConfig` | One of `NONE`, `EMAIL`, `SMS`, `AUTHENTICATOR`. Editable via `/profile/2fa`. |
| `lastLoginMs` | Read-only. Epoch ms of last successful login. |
| `loginCount` | Read-only. Total successful logins since account creation. |
| `registrationDate` | Read-only. Epoch ms of account creation. |
| `alertPreference` | Three booleans: `emailAlert`, `smsAlert`, `pushAlert`. Editable. |
| `profile` | Personal identity and address. See [Profile tab](#tab-1--personal-profile). |
| `billingProfile` | Billing identity, address and company info. Extends `profile` fields. See [Billing tab](#tab-2--billing-information). |
| `customFields` | Root-level custom fields. Filtered by prefix rules. See [Custom fields tab](#tab-4--custom-fields). |

### Admin-only fields

These fields are `null` in the JSON (and must not be rendered) when the logged-in user
is not an administrator. When the requester is an admin they are populated:

| Field | Type | Description |
|---|---|---|
| `registrationIp` | string | IP address used at registration. |
| `passwordExpired` | boolean | True when the password reset deadline has passed. |
| `active` | boolean | Whether the account is active. |
| `locked` | boolean | Whether the account is locked (too many failed logins). |
| `conditionValidation` | boolean | Whether the user has accepted the current ToS. |
| `conditionValidationDate` | epoch ms | Date of last ToS acceptance. |
| `conditionValidationVersion` | string | Version of the ToS that was accepted. |
| `apiKeys` | array | List of API key metadata (secrets are never returned). |

---

## 3. Detailed Profile — Frontend page layout

The detailed profile page is organised in tabs. The same layout is used whether the
viewer is the user themselves or an administrator — admin-only sections are simply
hidden when the fields are null.

### Tab 1 — Personal Profile

Editable personal identity and contact information.

**Identity section**
| UI label | JSON path | Input type |
|---|---|---|
| First name | `profile.firstName` | text |
| Last name | `profile.lastName` | text |
| Gender / Title | `profile.gender` | text or select |
| Language | `language` | select (ISO codes) |

**Contact section**
| UI label | JSON path | Input type |
|---|---|---|
| Email | `email` | email (read-only for self, editable for admin) |
| Mobile phone | `profile.phoneNumber` | tel (E.164) |
| Country code | `profile.country` | select (ISO 2-char) |
| Timezone | `profile.timezone` | select (IANA tz) |

**Address section**
| UI label | JSON path | Input type |
|---|---|---|
| Address | `profile.address` | text |
| City | `profile.city` | text |
| Zip code | `profile.zipCode` | text |
| Country | `profile.country` | select |

**Preferences section**
| UI label | JSON path | Input type |
|---|---|---|
| Email alerts | `alertPreference.emailAlert` | toggle |
| SMS alerts | `alertPreference.smsAlert` | toggle |
| Push alerts | `alertPreference.pushAlert` | toggle |
| 2FA method | `twoFAConfig` | select / wizard |

**Profile custom fields** (at the bottom of the tab)

Render `profile.customFields` as a list of label/value pairs. Apply visibility
rules: hide entries prefixed `hide_` or `chide_` when the viewer is not an admin.

**"Copy to billing" button**

A single click copies the following fields from `profile` into `billingProfile`:
`firstName`, `lastName`, `gender`, `phoneNumber`, `address`, `city`, `zipCode`,
`country`, `timezone`. The copy is local only; the user must save the billing tab
to persist it.

---

### Tab 2 — Billing Information

Editable billing identity, company details and billing address. This tab mirrors
Tab 1 but operates on `billingProfile` fields, with the addition of company data.

**Company section**
| UI label | JSON path | Input type |
|---|---|---|
| Company name | `billingProfile.companyName` | text |
| Country code | `billingProfile.countryCode` | select (ISO 2-char) |
| VAT / Tax number | `billingProfile.vatNumber` | text |

**Identity section** — same fields as Tab 1 but from `billingProfile.*`

**Address section** — same fields as Tab 1 but from `billingProfile.*`

**Billing custom fields** (at the bottom of the tab)

Render `billingProfile.customFields` with the same visibility rules as above.

---

### Tab 3 — Account Information

Read-only summary available to all users. Admin section is shown only when the
admin-only fields are non-null.

**Activity** (all users)
| UI label | JSON path |
|---|---|
| Login identifier | `login` |
| Registration date | `registrationDate` (formatted date) |
| Last login | `lastLoginMs` (formatted date) |
| Login count | `loginCount` |

**Security** (all users)
| UI label | JSON path |
|---|---|
| 2FA method | `twoFAConfig` |

**Account status** (admin only — render only when fields are non-null)
| UI label | JSON path |
|---|---|
| Active | `active` (boolean badge) |
| Locked | `locked` (boolean badge) |
| Password expired | `passwordExpired` (boolean badge) |
| Registration IP | `registrationIp` |
| ToS accepted | `conditionValidation` (boolean) |
| ToS acceptance date | `conditionValidationDate` (formatted date) |
| ToS version | `conditionValidationVersion` |

---

### Tab 4 — Custom Fields

Displays the root-level `customFields` array (not `profile.customFields` nor
`billingProfile.customFields` — those are shown in their respective tabs).

These fields are application-specific extensions. Render them as a generic
key/value list. Apply visibility rules:

- Skip entries whose name starts with `hide_` or `chide_` when the viewer is not
  an administrator.
- The `basic_` and `cbasic_` entries are visible to all; label them accordingly
  (they also appear in the basic profile).

---

### Tab 5 — API Keys (admin only)

Shown only when `apiKeys` is non-null (i.e. the viewer is an administrator).

Display the list of `apiKeys` as a table with metadata columns (name, creation date,
last-used date, expiry). Secret values are never returned by the API and must never
be displayed.

---

## 4. Update endpoints

| Action | Endpoint | Body |
|---|---|---|
| Update basic info (name, phone, language) | `PUT /users/1.0/profile/basic` | `UserBasicProfileBody` |
| Update detailed profile (profile + billing + alerts + custom fields) | `PUT /users/1.0/profile/detailed` | `UserDetailedProfileBody` |
| Upsert a root custom field | `PUT /users/1.0/profile/customfield` | `{ name, value }` |
| Change password | `PUT /users/1.0/profile/password/change` | `{ password }` |
| Configure 2FA | `PUT /users/1.0/profile/2fa` | `UserTwoFaBody` |

`PUT /users/1.0/profile/basic` body fields: `login` (target, optional for self),
`firstName`, `lastName`, `mobileNumber`, `isoCountryCode`, `language`, `customFields`
(only `basic_` / `cbasic_` entries are accepted here).

---

## 5. Updating the Detailed Profile — `PUT /users/1.0/profile/detailed`

### Authorization

```
PUT /users/1.0/profile/detailed
Authorization: Bearer <token>   (ROLE_LOGIN_COMPLETE, API sessions excluded)
```

A user updates their own profile. An administrator can update any user's profile by
including the optional `login` field in the body.

### Request body — `UserDetailedProfileBody`

```json
{
  "login":    "a3f9…",

  "language": "en",

  "alertPreference": {
    "emailAlert": true,
    "smsAlert":   false,
    "pushAlert":  false
  },

  "profile": {
    "firstName":   "John",
    "lastName":    "Doe",
    "gender":      "Mr",
    "phoneNumber": "+33601020304",
    "address":     "123 Main St",
    "city":        "Paris",
    "zipCode":     "75001",
    "country":     "FR",
    "timezone":    "Europe/Paris",
    "customFields": [
      { "name": "profile_nickname", "value": "Johnny" }
    ]
  },

  "billingProfile": {
    "firstName":   "John",
    "lastName":    "Doe",
    "gender":      "Mr",
    "phoneNumber": "+33601020304",
    "address":     "123 Main St",
    "city":        "Paris",
    "zipCode":     "75001",
    "country":     "FR",
    "timezone":    "Europe/Paris",
    "companyName": "Acme Inc.",
    "countryCode": "FR",
    "vatNumber":   "FR12345678901",
    "customFields": []
  },

  "customFields": [
    { "name": "basic_avatar_url", "value": "https://…" },
    { "name": "some_app_field",   "value": "…" }
  ]
}
```

### Body field reference

| Field | Required | Description |
|---|---|---|
| `login` | No | Target user login hash. Omit for self. **Admin only** — ignored when the caller is not an admin. |
| `language` | **Yes** | 2-letter language code (`en`, `fr`, …). |
| `alertPreference` | **Yes** | Three booleans controlling notification channels. |
| `profile` | **Yes** | Personal identity and address block (see `UserProfile` fields below). |
| `billingProfile` | **Yes** | Billing identity, address and company block (extends `profile`). |
| `customFields` | No | Root-level custom fields (key/value list). Existing fields not listed are left unchanged. |

#### `profile` / `billingProfile` shared fields

| Field | Type | Description |
|---|---|---|
| `firstName` | string | First name (encrypted at rest). |
| `lastName` | string | Last name (encrypted at rest). |
| `gender` | string | Free text title / salutation (e.g. `Mr`, `Ms`). |
| `phoneNumber` | string | E.164 phone number (e.g. `+33601020304`). |
| `address` | string | Street address. |
| `city` | string | City. |
| `zipCode` | string | Postal code. |
| `country` | string | ISO 3166-1 alpha-2 country code. |
| `timezone` | string | IANA timezone (e.g. `Europe/Paris`). |
| `customFields` | list | Custom fields scoped to this profile block. |

#### `billingProfile` additional fields

| Field | Type | Required | Description |
|---|---|---|---|
| `companyName` | string | **Yes** | Legal company name. |
| `countryCode` | string | **Yes** | ISO 2-char country code for billing jurisdiction. |
| `vatNumber` | string | No | VAT / tax registration number. |

### Response

Returns `UserDetailedProfileResponse` (same structure as the GET endpoints).
HTTP 200 on success, 400 on parse error, 403 on insufficient rights.

### Read-only fields (never sent in the body)

The following fields from `UserDetailedProfileResponse` cannot be updated via this
endpoint and must not be included in the form submission: `email`, `login` (root),
`twoFAConfig`, `lastLoginMs`, `loginCount`, `registrationDate`, `registrationIp`,
`passwordExpired`, `active`, `locked`, `conditionValidation`, `conditionValidationDate`,
`conditionValidationVersion`, `apiKeys`.

Use the dedicated endpoints listed in [Section 4](#4-update-endpoints) for 2FA and
password changes.

### Frontend save flow

1. Load the detailed profile via `GET /users/1.0/profile/detailed` (or the `/{login}`
   admin variant) and populate all form fields.
2. The user edits one or more tabs.
3. On "Save", collect all form values — even unchanged ones — and POST the full body to
   `PUT /users/1.0/profile/detailed`. The server replaces all editable fields.
4. On success (HTTP 200), reload the profile and refresh the basic profile cache so the
   header reflects any name/language changes immediately.
5. On HTTP 400, surface the server `message` slug in an i18n-translated error banner.

---

## 6. Key implementation notes for the frontend

- **Always load the detailed profile before opening the page.** Do not reuse the
  basic profile data — it only carries a subset of the fields and custom fields.
- **Null-guard every admin-only field.** Check `!= null` before rendering any of
  `registrationIp`, `passwordExpired`, `active`, `locked`, `conditionValidation`,
  `conditionValidationDate`, `conditionValidationVersion`, `apiKeys`.
- **The "copy to billing" action is client-side only** until the user explicitly
  saves Tab 2. Avoid auto-saving.
- **Custom field labels** are not provided by the API; the frontend is responsible
  for mapping `name` slugs to human-readable labels via an i18n table or a
  configuration file.
- **Prefix filtering** must be applied client-side as a safety measure, even though
  the server already filters `hide_` / `chide_` fields for non-admin users.
  This prevents accidental display of fields added in future API versions.
- **Phone numbers** are stored and returned in E.164 format. Use a phone-number
  formatting library for display; submit in E.164 on save.
- **Epoch milliseconds** must be converted to human-readable dates using the user's
  `timezone` and `language` for formatting.
