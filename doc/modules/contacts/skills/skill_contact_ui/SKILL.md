---
name: contacts-ui
description: Generate front-end UI components for the contacts module (list, create, detail/edit pages)
---

# Contacts module — UI generation skill

You are an expert front-end developer tasked with generating UI components for the IoTower
contacts management module. This skill is self-contained: every business rule, API endpoint,
request/response shape, and UI requirement is described below. Do not look elsewhere for context.

---

## Business context

A **contact** is a lightweight, encrypted identity record for people who do not have an account
in the system (third-party technicians, recipients of alerts, etc.). Contacts are created by
authenticated users, stored encrypted at rest, and shared through a group-based visibility model.

### Roles

| Role | What the user can do |
|---|---|
| `ROLE_CONTACT_ADMIN` | Create contacts in any group they administer; update group assignments; remove contacts from administered groups. |
| `ROLE_CONTACT_WRITE` | Create personal contacts (visible only in their virtual private group); update and delete contacts they own. |
| `ROLE_CONTACT_USER` | Read-only: list and view contacts visible through their groups. Cannot create or edit. |

All roles require `ROLE_LOGIN_COMPLETE` (fully authenticated session).

### Visibility

A contact is visible to a user when the contact's `groups` list intersects with the full set of
groups the user belongs to: direct group memberships + virtual personal group + all sub-groups
derived from each. The back-end handles this transparently; the front-end only needs to call the
list/detail endpoints.

### Ownership and editability

Every contact has an `ownerLogin` (server-side only, never exposed in list/detail responses).
The `editable` boolean returned by both the list and detail endpoints is the single source of
truth: when `true`, the authenticated user owns that contact **and** holds `ROLE_CONTACT_ADMIN`
or `ROLE_CONTACT_WRITE`. Only show Edit / Delete actions when `editable === true`.

---

## API reference — base URL `/contacts/1.0`

All endpoints require an `Authorization: Bearer <token>` header.

---

### 1. List visible contacts — `GET /contacts/1.0/`

**Required role:** `ROLE_CONTACT_USER` or `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Query parameters:**

| Name | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | 0-based page index |
| `size` | int | `25` | Results per page (server clamps to [1, 100]) |

**Response 200** (`PrivContactListResponseItf`):
```json
{
  "total": 142,
  "page": 0,
  "size": 15,
  "contacts": [
    {
      "contactId": "64a1f3c2e4b0d9f3a2b1c0d7",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "+33612345678",
      "pushAddress": "fcm://token-xyz",
      "groups": ["abc123"],
      "creationDate": 1700000000000,
      "modificationDate": 1700010000000,
      "editable": true
    }
  ]
}
```

When no contacts are visible for this user (render empty state), the response is:
```json
{ "total": 0, "page": 0, "size": 0, "contacts": [] }
```

---

### 2. Get full contact detail — `GET /contacts/1.0/{contactId}`

**Required role:** `ROLE_CONTACT_USER` or `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Path parameter:** `contactId`

**Response 200** (`PrivContactDetailResponseItf`):
```json
{
  "contactId": "64a1f3c2e4b0d9f3a2b1c0d7",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+33612345678",
  "pushAddress": "fcm://token-xyz",
  "gender": "Mr",
  "language": "fr",
  "timeZone": "CET",
  "companyName": "Acme Corp",
  "address": "12 Rue de la Paix",
  "city": "Paris",
  "zipCode": "75001",
  "countryCode": "FR",
  "vatNumber": "FR12345678901",
  "customFields": [
    { "name": "badge_id", "value": "B-4821" }
  ],
  "creationDate": 1700000000000,
  "modificationDate": 1700010000000,
  "editable": true
}
```

**Response 404:** contact not found or not visible to requester.

---

### 3. Create a contact — `POST /contacts/1.0/`

**Required role:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Request body** (`PrivContactCreationBody`):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+33612345678",
  "pushAddress": "",
  "gender": "Mr",
  "language": "fr",
  "timeZone": "CET",
  "companyName": "Acme Corp",
  "address": "12 Rue de la Paix",
  "city": "Paris",
  "zipCode": "75001",
  "countryCode": "FR",
  "vatNumber": "",
  "groups": ["abc123"],
  "customFields": [
    { "name": "badge_id", "value": "B-4821" }
  ]
}
```

Business constraints:
- At least one of `firstName` or `lastName` must be non-empty.
- At least one of `email`, `phoneNumber`, or `pushAddress` must be non-empty.
- `email` syntax is validated server-side; validate on the client too before submitting.
- `groups`: only sent when the user has `ROLE_CONTACT_ADMIN`. For `ROLE_CONTACT_WRITE` users,
  send an empty array `[]` or omit the field — the server assigns the user's personal virtual
  group automatically.
- The server always adds the owner's virtual group; the front-end never needs to manage it.

**Response 201** (`PrivContactCreationResponseItf`):
```json
{ "contactId": "64a1f3c2e4b0d9f3a2b1c0d7" }
```

**Response 400:** validation error (missing name, missing contact mean, invalid email).
**Response 403:** unauthorized group assignment.

---

### 4. Update a contact — `PUT /contacts/1.0/{contactId}`

**Required role:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE` (must be owner)

**Path parameter:** `contactId`

**Request body** (`PrivContactUpdateBody`): same fields as creation.

Semantics: `null` = no change; `""` (empty string) = clear the field.
Only send fields you intend to change. `groups`, when provided, fully replaces the existing list
(server always preserves the owner's virtual group). Only sent by `ROLE_CONTACT_ADMIN`.

**Response 200:** contact updated (ActionResult).
**Response 400:** validation error.
**Response 403:** not owner or unauthorized group.
**Response 404:** contact not found.

---

### 5. Delete a contact — `DELETE /contacts/1.0/{contactId}`

**Required role:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Path parameter:** `contactId`

Behavior:
- **Owner**: always hard-deletes.
- **Admin (non-owner)**: removes the contact from groups the admin administers. If only the
  owner's virtual group remains, the contact is hard-deleted. If admin has no rights on any
  group, returns 403.

**Response 200:** deleted or detached.
**Response 403:** no rights.
**Response 404:** not found.

---

### 6. Get accessible groups (for admin group picker) — `GET /users/1.0/groups`

**Required role:** `ROLE_LOGIN_COMPLETE`

Returns the hierarchy of groups accessible to the current user as a tree.

**Response 200** (array of `GroupsHierarchySimplified`):
```json
[
  {
    "shortId": "grp-root-abc",
    "name": "Technicians",
    "children": [
      {
        "shortId": "grp-child-def",
        "name": "Field team North",
        "children": []
      }
    ]
  }
]
```

Use this to build a multi-select tree picker in the admin creation/edit form. Flatten the tree
to collect all `shortId` values and let the user pick one or more. Send only the selected
`shortId` values in the `groups` array of the creation/update body.

---

## UI pages

### Page 1 — Contact list

**Route:** e.g. `{base}/contacts`
**Access:** `ROLE_CONTACT_USER` or higher

**Layout:**
```
┌────────────────────────────────────────────────────────────────────┐
│ Contacts                                    [+ New contact]        │
├──────────────┬──────────────┬────────┬────────┬──────┬──────┬──────┤
│ First name   │ Last name    │ Email  │ Phone  │ Push │ View │ Edit │
├──────────────┼──────────────┼────────┼────────┼──────┼──────┼──────┤
│ John         │ Doe          │ (...)  │ (...)  │  ✓  │ [👁] │ [✏] │
│ Jane         │ Smith        │ (...)  │ (...)  │      │ [👁] │      │
└──────────────┴──────────────┴────────┴────────┴──────┴──────┴──────┘
│ Show 15 ▾   < 1 2 3 ... >                                          │
└────────────────────────────────────────────────────────────────────┘
```

**Columns:**
- `firstName`, `lastName` — always visible.
- `email` — hidden by default; clicking the row (or a "show" button) calls
  `GET /contacts/1.0/{contactId}` and reveals the full detail in an expandable panel or modal.
  Never display email or phone directly from the list response (they are present but should be
  revealed only on demand to limit exposure). A green check icon (`✓`) can be shown in the list when `email` is non-empty, 
  but the raw value is never shown in the list until clicked.
- `phone` — same treatment as email.
- Push column: green check icon (`✓`) when `pushAddress` is non-empty in the list response
  (the list already returns `pushAddress`, so no extra call needed for the indicator). No raw
  value is ever shown in the list.
- `editable` column: show an edit icon/button only when `contact.editable === true`.
  The edit button navigates to the edit page (Page 3).

**Pagination:**
- Per-page selector: [15, 25, 50, 100], default **15**.
- Use `page` (0-based) and `size` query parameters.
- Derive total pages from `Math.ceil(total / size)`.
- Show page number controls (prev / numbers / next).
- Use the framework standard pagination component if available.

**New contact button:**
- Visible only when the user has `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`.
- Navigates to Page 2 (create form).

**Contact detail expansion (on row click):**
- Call `GET /contacts/1.0/{contactId}`.
- Show a detail panel or modal with all returned fields (firstName, lastName, email, phone,
  gender, language, company, address, city, zipCode, countryCode, vatNumber, customFields,
  alertPreferences, groups, dates).
- Do not show `pushAddress` as a raw value; the push indicator in the list suffices.
- If `editable === true` in the detail response, show an Edit button in the panel.

---

### Page 2 — Create contact

**Route:** e.g. `{base}/contacts/new`
**Access:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Form fields:**

Identity (required group — at least one of firstName or lastName):
- First name (text input)
- Last name (text input)

Contact means (required group — at least one):
- Email (text input, validate RFC 5322 syntax on client)
- Phone number (text input, E.164 format hint: +33612345678)
- Push address (text input)

Additional (all optional):
- Gender (text input)
- Language (text input or select, e.g. `fr`, `en`)
- Timezone (text input, IANA name e.g. `CET`, `Europe/Paris`)
- Company name
- Address / City / Zip code / Country code (ISO alpha-2)
- VAT number

Custom fields (dynamic list):
- Key/value pairs; user can add or remove rows.
- Names starting with `clear_` or `cbasic_` are stored in clear; all others are encrypted.

**Group assignment (ROLE_CONTACT_ADMIN only):**
- Call `GET /users/1.0/groups` on page load to get the accessible group tree.
- Render a tree or flat list with checkboxes.
- The user's personal virtual group is always added automatically by the server; do not display
  or send it — it is transparent to the user.
- For `ROLE_CONTACT_WRITE` users: hide the group section entirely.

**Validation before submit:**
- At least one of firstName / lastName is non-empty.
- At least one of email / phoneNumber / pushAddress is non-empty.
- Email matches basic RFC 5322 pattern if filled.

**Submit:** `POST /contacts/1.0/` → on 201, navigate to the list page.

**Error handling:**
- 400: display the i18n slug as a user-friendly message.
- 403: display a permission error.

---

### Page 3 — Edit / view contact

**Route:** e.g. `/contacts/{contactId}/edit`
**Access:** only reachable when `editable === true` (guard the route)

**Pre-fill:** call `GET /contacts/1.0/{contactId}` on page load to populate all fields.

**Form:** identical to Page 2, with these differences:
- All fields pre-filled with the values from the detail response.
- Empty string `""` means the field has no value (display empty input, not null).
- Clearing a field and saving will send `""` (empty string) for that field, which the server
  interprets as "clear this field".
- For fields the user does not touch, send `null` so the server makes no change.
  Alternatively, always send all fields with their current values — the server is idempotent.
- Group picker (admin only): pre-select the groups returned by the detail response.
  The personal virtual group is hidden from display (filtered by checking if the shortId matches
  the virtual group pattern — virtual groups are prefixed with `user_`).

**Custom fields:**
- Render existing custom fields pre-filled.
- Adding a new row: sends a new key/value on save.
- Setting a value to empty string for an existing key: the server treats `null` as delete.
  Implement a "remove" button per row that sets the value to `null` in the patch body.

**Delete button:**
- Visible only when `editable === true`.
- Show a confirmation dialog before calling `DELETE /contacts/1.0/{contactId}`.
- On 200: navigate back to the contact list.
- On 403: display a permission error (may happen for admin partial-deletion scenarios).

**Submit update:** `PUT /contacts/1.0/{contactId}` → on 200, navigate to list or show success.

---

## Error display

All API errors return:
```json
{ "status": "string", "message": "slug-key" }
```

Map slugs to human-readable messages using the i18n table below. Display errors inline near
the relevant field when possible, or as a toast/banner for general errors.

### i18n slug → message mapping

**Creation errors:**
```
contacts-creation-body-empty            → "The contact creation request body cannot be empty"
contacts-creation-name-required         → "At least a first name or a last name is required"
contacts-creation-contact-mean-required → "At least one contact means (email, phone or push) is required"
contacts-creation-email-invalid         → "The provided email address is not valid"
contacts-creation-requester-not-found   → "Your account could not be found"
contacts-creation-no-rights             → "You do not have the required role to create a contact"
contacts-creation-group-not-allowed     → "You do not have access to one or more of the selected groups"
```

**Update errors:**
```
contacts-update-body-empty              → "The update request body cannot be empty"
contacts-update-contact-not-found       → "The contact to update was not found"
contacts-update-requester-not-found     → "Your account could not be found"
contacts-update-not-owner               → "Only the contact owner can perform this update"
contacts-update-email-invalid           → "The provided email address is not valid"
contacts-update-group-not-allowed       → "You do not have access to one or more of the selected groups"
contacts-update-failed                  → "An unexpected error occurred while updating the contact"
```

**Deletion errors:**
```
contacts-delete-contact-not-found       → "The contact was not found"
contacts-delete-requester-not-found     → "Your account could not be found"
contacts-delete-not-owner               → "Only the contact owner or an administrator can delete a contact"
contacts-delete-no-group-rights         → "You do not have administrator rights on any group this contact belongs to"
contacts-delete-failed                  → "An unexpected error occurred while deleting the contact"
```

**Detail / get errors:**
```
contacts-get-contact-not-found          → "The contact was not found or is not visible to you"
contacts-get-requester-not-found        → "Your account could not be found"
contacts-get-failed                     → "An unexpected error occurred while fetching the contact"
```

**List errors:**
```
contacts-list-requester-not-found       → "Your account could not be found"
contacts-list-none-found                → "No contacts found"
contacts-list-failed                    → "An unexpected error occurred while listing contacts"
```

---

## Implementation notes

- The list endpoint already returns decrypted `email`, `phoneNumber`, and `pushAddress` values.
  **Do not display them in the table rows.** Reveal them only after the user explicitly clicks a
  row and the detail call confirms visibility. This reduces inadvertent exposure in screen shares
  or shoulder-surfing.
- The `pushAddress` is a technical token (FCM device token, etc.); display it as a green check
  icon in the list and in the detail panel as a greyed-out non-copyable indicator (or omit it
  from the detail view entirely — it has no user-readable value).
- Virtual groups (group shortId starting with `user_`) should never be shown to the user in the
  group picker or in the groups column of the list/detail. Filter them out before rendering.
- Date fields (`creationDate`, `modificationDate`) are Unix timestamps in milliseconds. Format
  them using the locale-aware date formatter (e.g. `new Date(ms).toLocaleDateString()`).
- The back-end clamps `size` to [1, 100]. The front-end default of **15** is a UI choice;
  always send the user-selected value in the query parameter.