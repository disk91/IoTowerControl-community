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

### Response — `UserDetailedProfileResponse`

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
| `basic_` | yes | Basic profile + detailed profile (all users) — **read-only in UI, cannot be deleted** |
| `cbasic_` | no | Basic profile + detailed profile (all users) — **read-only in UI, cannot be deleted** |
| `clear_` | no | Detailed profile (all users) — **read-only in UI, cannot be deleted** |
| `hide_` | yes | Detailed profile — **admin only, must be hidden from non-admin UI** |
| `chide_` | no | Detailed profile — **admin only, must be hidden from non-admin UI** |
| *(other)* | yes | Detailed profile (all users) — **fully editable, can be added and deleted** |

This convention applies equally at the root level, inside `profile.customFields`,
and inside `billingProfile.customFields`.

**Critical rule for saving**: hidden fields (`hide_`, `chide_`) are filtered from
display but **must be re-injected from the last server response into every save body**
to avoid the backend deleting them. See [Section 5 — Save flow](#5-updating-the-detailed-profile----put-users10profiledetailed).

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
    "timezone":    "CET",
    "customFields": [
      { "name": "profile_nickname", "value": "Johnny" }
    ]
  },

  "billingProfile": {
    "firstName":   "John",
    "lastName":    "Doe",
    "gender":      "Mr",
    "phoneNumber": "+33601020304",
    "address":     "123 Main St\nBuilding B",
    "city":        "Paris",
    "zipCode":     "75001",
    "country":     "FR",
    "timezone":    "CET",
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

> **Note on `timezone`**: the field stores a timezone **abbreviation** (e.g. `CET`,
> `EST`, `UTC`), not an IANA name. Use `ToolsTimezoneSelect` which is built around
> the project's `~/config/timezones.ts` abbreviation list.

Fields that are `null` in the self-request are populated when the requester is an admin
(see [Admin-only fields](#admin-only-fields) below).

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

The detailed profile page lives at `app/pages/front/private/profile/` and is split into
**separate sub-pages** (not a single component with tab state). A `UDashboardToolbar` +
`UNavigationMenu` in the parent `profile.vue` provides the tab links. Each sub-page
is a standalone Vue file:

```
app/pages/front/private/profile/
  index.vue      ← General information (name, language, mobile, basic profile)
  personal.vue   ← Personal data tab
  billing.vue    ← Billing information tab
  security.vue   ← Security / 2FA tab
  rights.vue     ← Roles and rights (read-only)
  danger.vue     ← Danger zone (account deletion)
```

### Shared data loading pattern

Both `personal.vue` and `billing.vue` share one `useAsyncData` call with the key
`'user-detailed-profile'`. This ensures a single network request regardless of which
tab the user visits first.

```ts
const { data: detailedProfile, pending, error } = useAsyncData<UserDetailedProfileResponse>(
    'user-detailed-profile',
    async () => {
        const res = await $apiBackendUsers.getUserDetailedProfile();
        if (res.success) return res.success;
        throw new Error(res.error?.message ?? 'unknownError');
    }
);
```

After a successful save, **always call `refreshNuxtData('user-detailed-profile')`** to
invalidate the cache so both tabs reload the fresh server state.

### One-time init flag pattern

Local form state (reactive objects and refs) is initialised from the server response
via a `watch`. To prevent the watch from overwriting user edits when the reactive ref
updates for other reasons, use a plain (non-reactive) boolean flag:

```ts
let personalLoaded = false;

watch(() => detailedProfile.value, (d) => {
    if (!d || personalLoaded) return;
    personalLoaded = true;
    // populate local state from d
}, { immediate: true });

// In the save function, reset before refreshNuxtData so the watch re-fires:
personalLoaded = false;
await refreshNuxtData('user-detailed-profile');
```

Use `personalLoaded` in `personal.vue` and `billingLoaded` in `billing.vue`.

---

### Tab — Personal Data (`personal.vue`)

**Save button**: Place a `UButton` (icon `i-lucide-save`) at the **top** of the page
in a `UPageCard` with `orientation="horizontal"`, matching the General Info pattern.

**Field order and input types** (all inside `UFormField` rows with
`class="flex max-sm:flex-col justify-between items-start gap-4"`):

**Profile details section**
| UI label | JSON path | Input | Notes |
|---|---|---|---|
| Title | `profile.gender` | `UInput` | Free text, editable |
| Last name | `profile.lastName` | `UInput disabled` | Read-only — not editable by user |
| First name | `profile.firstName` | `UInput disabled` | Read-only — not editable by user |
| Time zone | `profile.timezone` | `ToolsTimezoneSelect` | v-model = abbreviation string |
| Street address | `profile.address` | `UTextarea :rows="3"` | Multi-line |
| Zip / Postal code | `profile.zipCode` | `UInput` | **ZIP before city** |
| City | `profile.city` | `UInput` | |
| Country | `profile.country` | `ToolsCountrySelect` | v-model = ISO-2 code |

**Alert preferences section**
| UI label | JSON path | Input |
|---|---|---|
| Email alerts | `alertPreference.emailAlert` | `USwitch` |
| SMS alerts | `alertPreference.smsAlert` | `USwitch` |
| Push alerts | `alertPreference.pushAlert` | `USwitch` |

**Profile custom fields section** (`profile.customFields`)

Render as a list of `UFormField` rows. Filter: hide entries where name starts with
`hide_` or `chide_`. System fields (`basic_`, `cbasic_`, `clear_`) are shown
read-only (disabled `UInput`) with no delete button. All other fields are editable
with a delete button. Provide an "add new field" row at the bottom.

**Global custom fields section** (root `customFields`)

Same CRUD pattern as profile custom fields. Same visibility/editability rules.

---

### Tab — Billing Information (`billing.vue`)

**Save button**: Same top-of-page pattern as personal tab.

**"Import personal data" button**: `UButton` (icon `i-lucide-copy`, variant `soft`)
placed in the `UPageCard` header of the Billing Contact section. Clicking it copies
`profile.*` fields into the local billing reactive state **and** updates the phone
input's seed E.164 value, forcing the phone component to re-initialise via a `:key`
counter increment. This is a local-only operation; the user must click Save to persist.

**Company information section** — ALL fields are editable (not read-only):
| UI label | JSON path | Input |
|---|---|---|
| Company name | `billingProfile.companyName` | `UInput` |
| VAT / Tax number | `billingProfile.vatNumber` | `UInput` |
| Billing country | `billingProfile.countryCode` | `ToolsCountrySelect` |

**Billing contact section**
| UI label | JSON path | Input | Notes |
|---|---|---|---|
| Title | `billingProfile.gender` | `UInput` | |
| First name | `billingProfile.firstName` | `UInput` | |
| Last name | `billingProfile.lastName` | `UInput` | |
| Phone number | `billingProfile.phoneNumber` | `ToolsPhoneNumberInput` | See phone pattern below |

**Billing address section**
| UI label | JSON path | Input | Notes |
|---|---|---|---|
| Street address | `billingProfile.address` | `UTextarea :rows="3"` | Multi-line |
| Zip / Postal code | `billingProfile.zipCode` | `UInput` | **ZIP before city** |
| City | `billingProfile.city` | `UInput` | |
| Country | `billingProfile.country` | `ToolsCountrySelect` | |
| Time zone | `billingProfile.timezone` | `ToolsTimezoneSelect` | |

**Billing custom fields section** (`billingProfile.customFields`)

Same CRUD pattern as personal tab custom fields.

---

## 4. Reusable components

### `ToolsCountrySelect`

`app/components/tools/CountrySelect.vue` — country selector with flags.

- **v-model**: ISO 3166-1 alpha-2 string (e.g. `"FR"`), empty string when unset.
- Data source: `libphonenumber-js` `getCountries()` — same library used by
  `ToolsPhoneNumberInput`. Country names via `Intl.DisplayNames(['en'], { type: 'region' })`.
- Uses `useAsyncData('country-select-list', ...)` to build and cache the sorted list.
- Displays flag icon (`flagpack:xx`) + country name + ISO code in trigger and items.
- Searchable.
- i18n namespace: `countries.*` (file `i18n/en/countries.json` and `i18n/fr/countries.json`,
  registered in `nuxt.config.ts` `fileNames`).

Usage:
```html
<ToolsCountrySelect v-model="profile.country" class="w-70" />
```

> **Width note**: do NOT add `w-full` inside `CountrySelect` root div — it conflicts
> with the `w-70` applied from the parent because Tailwind places `w-full` after
> numeric width utilities in its CSS output, making it win the cascade.
> The root div uses only `min-w-0 overflow-hidden`; the `USelectMenu` inside uses
> `class="w-full"` to fill whatever width the parent sets.

### `ToolsTimezoneSelect`

`app/components/tools/TimezoneSelect.vue` — timezone selector.

- **v-model**: timezone abbreviation string (e.g. `"CET"`), empty string when unset.
- Data source: `~/config/timezones.ts`.
- Shows full label (`(UTC+1) CET — Paris, Berlin…`) truncated inside the trigger via
  the `#label` slot; same label shown untruncated in the dropdown items.
- The `#trailing` slot provides a clear (×) button (`.stop` to prevent dropdown open).
- Same width caveat as `CountrySelect` — no `w-full` on root div.
- i18n namespace: `timezones.*` (`i18n/en/timezones.json` / `i18n/fr/timezones.json`).

### `ToolsPhoneNumberInput`

`app/components/tools/PhoneNumberInput.vue` — international phone input.

- **v-model**: `PhoneNumberInputState` object (contains `e164`, `isValid`,
  `countryCode`, `nationalNumber`, `phoneNumber`, …).
- Props: `e164` (string, initial E.164 value) and `isoCountry` (string, default country).
- The component initialises **once** from props in `setup()`; it does not react to
  prop changes after mount. To force re-initialisation (e.g. after "Import personal
  data"), increment a `:key` counter bound to the component.

```ts
const billingPhoneE164 = ref('');    // seed for (re-)initialisation
const billingPhoneKey  = ref(0);     // increment to force remount
const billingPhone     = ref<PhoneNumberInputState | null>(null);  // emitted state

// In loadFromBillingProfile:
billingPhoneE164.value = d.billingProfile?.phoneNumber ?? '';
billingPhoneKey.value++;

// In importPersonalData:
billingPhoneE164.value = d.profile?.phoneNumber ?? '';
billingPhoneKey.value++;
```

```html
<ToolsPhoneNumberInput
    :key="billingPhoneKey"
    v-model="billingPhone"
    :e164="billingPhoneE164"
    class="w-70"
/>
```

When building the save body:
```ts
const phoneNumber = billingPhone.value?.isValid
    ? (billingPhone.value.e164 ?? '')
    : (billingPhone.value?.phoneNumber ?? billingPhoneE164.value);
```

---

## 5. Updating the Detailed Profile — `PUT /users/1.0/profile/detailed`

### Authorization

```
PUT /users/1.0/profile/detailed
Authorization: Bearer <token>   (ROLE_LOGIN_COMPLETE, API sessions excluded)
```

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
    "timezone":    "CET",
    "customFields": [
      { "name": "profile_nickname", "value": "Johnny" }
    ]
  },

  "billingProfile": {
    "firstName":   "John",
    "lastName":    "Doe",
    "gender":      "Mr",
    "phoneNumber": "+33601020304",
    "address":     "123 Main St\nBuilding B",
    "city":        "Paris",
    "zipCode":     "75001",
    "country":     "FR",
    "timezone":    "CET",
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

### Mandatory fields

- `login` — the user's login hash (`appStore.getUserLogin()`). **Required** even for
  self-updates.
- `language` — must always be sent.
- `alertPreference` — all three booleans required.
- `profile` — full object required, even when only billing fields changed.
- `billingProfile` — full object required, even when only personal fields changed.

When saving from the personal tab, pass `billingProfile` through from
`detailedProfile.value.billingProfile`. When saving from billing, pass `profile`
through from `detailedProfile.value.profile`.

### Hidden custom field passthrough rule

The `customFields` arrays in `profile`, `billingProfile`, and at root level use a
**replace-all** strategy on the server. Hidden fields (`hide_`, `chide_`) are never
shown in the UI but **must** be included in the save body or they will be deleted.

Always merge them back from the last server response:

```ts
// profile custom fields
customFields: [
    ...(d.profile?.customFields ?? []).filter(cf => isHiddenField(cf.name)), // re-inject hidden
    ...profileCf.value,   // local visible draft
].map(cf => ({ name: cf.name, value: cf.value ?? '' })),

// billing custom fields
customFields: [
    ...(d.billingProfile?.customFields ?? []).filter(cf => isHiddenField(cf.name)),
    ...billingCf.value,
].map(cf => ({ name: cf.name, value: cf.value ?? '' })),

// global (root) custom fields — when tabs don't edit them, pass through entirely
customFields: (d.customFields ?? []).map(cf => ({ name: cf.name, value: cf.value ?? '' })),
```

### Custom field CRUD rules

```ts
const isSystemField = (name: string) =>
    name.startsWith('basic_') || name.startsWith('cbasic_') || name.startsWith('clear_');

const isHiddenField = (name: string) =>
    name.startsWith('hide_') || name.startsWith('chide_');

// Hidden fields: never show in UI, always pass through in save body
// System fields: show read-only, no delete button, value editable only by admin
// Other fields: fully editable, delete button visible
const canDeleteCf = (name: string) => !isSystemField(name) && !isHiddenField(name);
const canAddCf    = (name: string) => name.trim().length > 0 && !isSystemField(name) && !isHiddenField(name);
```

Initialise local CF draft arrays by **excluding** hidden fields (they travel separately):

```ts
function initProfileCf(d: UserDetailedProfileResponse) {
    profileCf.value = (d.profile?.customFields ?? [])
        .filter(cf => !isHiddenField(cf.name))
        .map(cf => ({ name: cf.name, value: cf.value ?? '' }));
}
```

### Response

Returns `UserDetailedProfileResponse`. HTTP 200 on success, 400 on validation error,
403 on insufficient rights.

### Frontend save flow

1. User edits a tab and clicks **Save** (button at the top of the page).
2. Build the full `UserDetailedProfileBody`:
  - Include `login` from `appStore.getUserLogin()`.
  - Pass the **other** tab's data through from `detailedProfile.value.*` untouched.
  - Merge hidden CFs back into all three `customFields` arrays.
3. Call `$apiBackendUsers.putUserDetailedProfile(body)`.
4. On error: show inline error message.
5. On success:
  - Reset the `loaded` flag (`personalLoaded = false` / `billingLoaded = false`).
  - Call `refreshNuxtData('user-detailed-profile')` — the watcher fires and
    re-populates local state from the fresh server data.
  - Show a success toast (`useToast()`).

---

## 6. Update endpoints summary

| Action | Endpoint | Body |
|---|---|---|
| Update basic info (name, phone, language) | `PUT /users/1.0/profile/basic` | `UserBasicProfileBody` |
| Update detailed profile | `PUT /users/1.0/profile/detailed` | `UserDetailedProfileBody` |
| Change password | `PUT /users/1.0/profile/password/change` | `{ password }` |
| Configure 2FA | `PUT /users/1.0/profile/2fa` | `UserTwoFaBody` |

---

## 7. Key implementation notes

- **Always include `login`** in the `PUT /users/1.0/profile/detailed` body even for
  self-updates. Omitting it causes a backend error.
- **Send the entire body** on every save — the server replaces all editable fields.
  Do not send partial updates.
- **ZIP code before city** in all address forms.
- **Title (gender) before last name / first name** in all identity forms.
- **`lastName` and `firstName` are read-only** on the personal tab (not editable by
  the user themselves; only admins can change them via the admin panel).
- **Address fields use `UTextarea`** (not `UInput`) to allow multi-line postal addresses.
- **Country fields use `ToolsCountrySelect`** (not a raw text input). The component
  returns an ISO-2 code.
- **Timezone fields use `ToolsTimezoneSelect`**. The v-model value is an abbreviation
  (`CET`, `EST`, `UTC`…), not an IANA name.
- **Phone number on billing tab uses `ToolsPhoneNumberInput`** with the key-based
  re-init pattern. The component is already a `UButtonGroup` (country selector + input
  on the same line); no additional layout wrapper needed.
- **Null-guard every admin-only field** before rendering.
- **Epoch milliseconds** must be converted to human-readable dates using
  `Intl.DateTimeFormat` with the user's `language` locale.
- **i18n**: both `en` and `fr` translations required for every string. Country selector
  labels live in `i18n/en/countries.json` and `i18n/fr/countries.json`, registered
  in `nuxt.config.ts` under `fileNames`.
