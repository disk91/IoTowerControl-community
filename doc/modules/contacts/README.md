# Third Party Contact Management (NCE)
Contacts are, in a way, information related to users, but they represent users who cannot log in to the 
application. A contact can be used, for example, to send alerts to third parties who do not need an account 
on the system.

A contact can also define people who are associated with devices or processes without being directly involved 
in the solution itself. For example, this could be a repair technician, an installer, or a doctor-type contact 
for an individual. These people will not have access to the solution, but their contact information must be 
stored, such as email addresses or phone numbers, so that information can be sent to them.

A contact is therefore a lightweight version of a user, while still maintaining a certain level of 
confidentiality for the data stored in it. There is no expiration date for the data, and encryption is 
performed using a platform key.

It must also be possible to retrieve a contact easily. To do this, a hash of the contact’s phone number or email 
address will be stored, so that the contact can later be searched for. The goal is, for example, when creating 
a user account, to check whether that person already exists as a contact, so that they can be associated 
differently if needed, with more precise elements and better-protected data.

## Contact life cycle
A new contact is created by a user. It can either be public, in which case it becomes part of a shared contact 
directory, or private to that user.

In most cases, only a `ROLE_CONTACT_ADMIN` can create public contacts and therefore accessible to users with `ROLE_CONTACT_USER`. 
A user with `ROLE_CONTACT_WRITE` can create private contacts, which are only accessible to them.
A contact exists independently, but the contacts owned by a user can be purged when that user is purged. 

A user can only edit contacts they have created, even if they can access the list of public contacts. 

## Contact structure

```json
{
  "id": "string",                 // technical unique identifier
  "version": "number",            // user structure version
  "emailHash": "string",          // user email hash 
  "phoneHash": "string",          // user phone number hash 
  "ownerLogin": "string",         // user login of the contact owner (only able to edit the contact)

  "email": "string",              // user email [Base64(encrypted)]
  "pushAddress": "string",        // Id for smartphone push (encrypted)
  "phoneNumber": "string",        // user phone number e164 format [Base64(encrypted)]
  "groups" : [                    // group having access to this contact (when public, Read Only), private contains the user_LoginHash
    "strings"
  ],
  "salt": [ "numbers" ],          // encryption salt 
  
  "language": "string",           // contact language (ISO country)
  
  "alertPreference": {
    "email": "boolean",           // contact email alert preference
    "sms": "boolean",             // contact sms alert preference
    "push": "boolean"             // contact push alert preference 
  },
  
  "gender": "string",             // contact gender to be used [Base64(encrypted)]
  "firstname": "string",          // contact first name [Base64(encrypted)]
  "lastname": "string",           // contact last name [Base64(encrypted)]
  "companyName": "string",        // contact company name [Base64(encrypted)]
  "address": "string",            // contact address [Base64(encrypted)]
  "city": "string",               // contact city [Base64(encrypted)]
  "zipCode": "string",            // contact zip code [Base64(encrypted)]
  "countryCode": "string",        // contact country ISO code [Base64(encrypted)]
  "vatNumber": "string",          // contact VAT number [Base64(encrypted)]

  "customFields": [{              // contact custom fields
        "name": "string",            // custom field key [clear], the one starting with `basic_` or `cbasic_` are returned in the basic API
        "value": "string"            // custom field value [Base64(encrypted)] value will be clear for seach when name start with `clear_` or `cbasic_`
  }],

  "creationDate": "date",         // contact creation date in MS since epoch
  "modificationDate": "date"     // last user modification date in MS since epoch

}
```

## Business rules

### Roles

| Role | Description |
|---|---|
| `ROLE_CONTACT_ADMIN` | Can create contacts in any group they have access to, update the group list of a contact they own, and remove a contact from groups they administer. |
| `ROLE_CONTACT_WRITE` | Can create private contacts (visible only in their virtual group) and update or delete contacts they own. |
| `ROLE_CONTACT_USER` | Read-only access; can list contacts visible to their groups. |

### Creation rules
- At least one of `firstName` or `lastName` must be provided.
- At least one contact mean (`email`, `phoneNumber`, or `pushAddress`) must be provided.
- When an email is provided its syntax is validated.
- `ROLE_CONTACT_WRITE` users can only create contacts in their own virtual group. The `groups` field must be empty or absent.
- `ROLE_CONTACT_ADMIN` users can assign additional groups, subject to having membership in each requested group. The owner virtual group is always added automatically.
- Alert preferences (`emailAlert`, `smsAlert`, `pushAlert`) are set automatically based on which contact means are provided.
- All personal data (names, email, phone, push address, address fields, VAT, gender) is encrypted at rest using a per-contact random salt combined with the server and application keys.
- A SHA-256 hash of the email and phone number is stored in clear to allow future indexed searches without decrypting.

### Update rules
- Only the **owner** of a contact can update it.
- All fields are optional in the update body: a `null` value means "no change", an empty string clears the field.
- After any update to a contact mean, the alert preferences are automatically recomputed to match the new state.
- Group list updates (`groups` field) are restricted to `ROLE_CONTACT_ADMIN` owners. When provided, the new list fully replaces the existing one; the owner virtual group is always preserved.
- Custom fields are upserted individually; a custom field with a `null` value is removed.

### Deletion rules
- **Owner delete**: always performs a hard delete of the contact, regardless of which groups it belongs to.
- **Admin delete** (`ROLE_CONTACT_ADMIN`, non-owner): the system iterates over the contact's groups and removes those for which the admin has `ROLE_CONTACT_ADMIN` rights.
  - If only the owner's virtual group remains after removal → hard delete (the contact is no longer shared with anyone).
  - If some groups are removed but others remain (not administered by the requester) → the contact is saved with the reduced group list.
  - If the admin has no rights on any of the contact's groups → `403 Forbidden`.

### Encryption model
Each contact has a random 16-byte `salt` stored in the document. The effective encryption key is derived from `salt` and other keys.


---

## API endpoints

All endpoints are under the base path `/contacts/1.0/` and require `ROLE_LOGIN_COMPLETE`.

---

### POST `/contacts/1.0/` — Create a contact

**Roles required:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Request body** (`PrivContactCreationBody`):

| Field | Type | Required | Description |
|---|---|---|---|
| `firstName` | string | conditional | Contact first name. Required if `lastName` is absent. |
| `lastName` | string | conditional | Contact last name. Required if `firstName` is absent. |
| `email` | string | conditional | Email address (syntax validated). Required if `phoneNumber` and `pushAddress` are absent. |
| `phoneNumber` | string | conditional | Phone in E.164 format. Required if `email` and `pushAddress` are absent. |
| `pushAddress` | string | conditional | Push notification address. Required if `email` and `phoneNumber` are absent. |
| `gender` | string | no | Free-text gender. |
| `language` | string | no | Language preference (e.g. `fr-fr`). |
| `companyName` | string | no | Company name. |
| `address` | string | no | Street address. |
| `city` | string | no | City. |
| `zipCode` | string | no | Zip / postal code. |
| `countryCode` | string | no | ISO 3166-1 alpha-2 country code. |
| `vatNumber` | string | no | VAT number. |
| `groups` | string[] | no | Additional group shortIds. Admin only; ignored for `ROLE_CONTACT_WRITE`. |
| `customFields` | CustomField[] | no | Key/value pairs attached to the contact. |

**Responses:**

| Code | Description |
|---|---|
| 201 | Contact created. Body: `PrivContactCreationResponseItf` with `contactId`. |
| 400 | Parse error (missing name, missing contact mean, invalid email, etc.). |
| 403 | Insufficient rights or unauthorized group assignment. |

---

### PUT `/contacts/1.0/{contactId}` — Update a contact

**Roles required:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE` (must be the contact owner)

**Path parameter:** `contactId` — the contact identifier returned at creation.

**Request body** (`PrivContactUpdateBody`): same fields as creation, all optional. `null` = no change, `""` = clear the field.

| Field | Type | Description |
|---|---|---|
| `firstName` | string | Null = no change, empty = clear. |
| `lastName` | string | Null = no change, empty = clear. |
| `email` | string | Null = no change, empty = clear. Syntax validated if non-empty. |
| `phoneNumber` | string | Null = no change, empty = clear. |
| `pushAddress` | string | Null = no change, empty = clear. |
| `gender` | string | Null = no change, empty = clear. |
| `language` | string | Null = no change, empty = clear. |
| `companyName` | string | Null = no change, empty = clear. |
| `address` | string | Null = no change, empty = clear. |
| `city` | string | Null = no change, empty = clear. |
| `zipCode` | string | Null = no change, empty = clear. |
| `countryCode` | string | Null = no change, empty = clear. |
| `vatNumber` | string | Null = no change, empty = clear. |
| `groups` | string[] | Admin only. Null = no change. Provided list fully replaces existing groups. |
| `customFields` | CustomField[] | Upserted individually. A null value on a field removes it. |

**Responses:**

| Code | Description |
|---|---|
| 200 | Contact updated. |
| 400 | Parse error (invalid email, etc.). |
| 403 | Requester is not the owner, or unauthorized group assignment. |
| 404 | Contact not found. |

---

### DELETE `/contacts/1.0/{contactId}` — Delete a contact

**Roles required:** `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`

**Path parameter:** `contactId` — the contact identifier.

**Behavior:**
- **Owner**: hard delete regardless of groups.
- **Admin (non-owner)**: removes the contact from groups the admin administers. If only the owner's virtual group remains, the contact is hard-deleted. If the admin has no rights on any group, returns 403.

**Responses:**

| Code | Description |
|---|---|
| 200 | Contact deleted (or detached from administered groups). |
| 403 | Requester has no rights over this contact or any of its groups. |
| 404 | Contact not found. |

---

### GET `/contacts/1.0/{contactId}` — Get full contact details

**Roles required:** `ROLE_CONTACT_ADMIN`, `ROLE_CONTACT_WRITE`, or `ROLE_CONTACT_USER`

**Path parameter:** `contactId` — the contact identifier returned at creation.

**Visibility rule:** the contact must belong to at least one group the requester can access
(same rule as the list endpoint). Contacts not visible to the requester return 404.

**Response body** (`PrivContactDetailResponseItf`):

| Field | Type | Description |
|---|---|---|
| `contactId` | string | Contact identifier. |
| `firstName` | string | Decrypted first name. |
| `lastName` | string | Decrypted last name. |
| `email` | string | Decrypted email address. |
| `phoneNumber` | string | Decrypted phone number. |
| `pushAddress` | string | Decrypted push address. |
| `gender` | string | Decrypted gender (free text). |
| `language` | string | Language preference (2x2 ISO code). |
| `companyName` | string | Decrypted company name. |
| `address` | string | Decrypted street address. |
| `city` | string | Decrypted city. |
| `zipCode` | string | Decrypted zip/postal code. |
| `countryCode` | string | Decrypted ISO country code. |
| `vatNumber` | string | Decrypted VAT number. |
| `groups` | string[] | Groups the contact belongs to. |
| `emailAlert` | boolean | Email alert preference. |
| `smsAlert` | boolean | SMS alert preference. |
| `pushAlert` | boolean | Push alert preference. |
| `customFields` | object[] | All custom fields, fully decrypted (`{name, value}`). |
| `creationDate` | long | Creation timestamp in ms since epoch. |
| `modificationDate` | long | Last modification timestamp in ms since epoch. |
| `editable` | boolean | `true` when the requester owns this contact **and** holds `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`. |

**Responses:**

| Code | Description |
|---|---|
| 200 | Full contact detail. |
| 403 | Insufficient rights. |
| 404 | Contact not found or not visible to the requester. |

---

### GET `/contacts/1.0/` — List visible contacts (paginated)

**Roles required:** `ROLE_CONTACT_ADMIN`, `ROLE_CONTACT_WRITE`, or `ROLE_CONTACT_USER`

Returns a paginated list of contacts visible to the authenticated user, i.e. all contacts belonging
to at least one group the user is a member of (including their virtual group and all sub-groups).
Personal data is decrypted and returned in full. Results are sorted by modification date descending.

**Visibility rule:** a contact is visible to a user when the contact's `groups` list intersects with
the complete set of groups the user belongs to: direct group memberships, ACL-based memberships,
the virtual group, and all sub-groups derived from each of the above.

**Query parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | 0-based page index. Clamped to `>= 0`. |
| `size` | int | `25` | Number of results per page. Clamped to `[1, 100]`. |

**Response body** (`PrivContactListResponseItf`):

| Field | Type | Description |
|---|---|---|
| `total` | long | Total number of contacts visible to the user across all pages. |
| `page` | int | Current page index (0-based). |
| `size` | int | Effective page size used. |
| `contacts` | object[] | Contacts on this page (see below). |

Each element of `contacts` (`PrivContactAbstractResponseItf`):

| Field | Type | Description |
|---|---|---|
| `contactId` | string | Contact identifier. |
| `firstName` | string | Decrypted first name. |
| `lastName` | string | Decrypted last name. |
| `email` | string | Decrypted email address. |
| `phoneNumber` | string | Decrypted phone number. |
| `pushAddress` | string | Decrypted push address. |
| `groups` | string[] | Groups the contact belongs to. |
| `creationDate` | long | Creation timestamp in ms since epoch. |
| `modificationDate` | long | Last modification timestamp in ms since epoch. |
| `editable` | boolean | `true` when the requester owns this contact **and** holds `ROLE_CONTACT_ADMIN` or `ROLE_CONTACT_WRITE`. Indicates the contact can be updated or deleted by this user. |

**Responses:**

| Code | Description |
|---|---|
| 200 | Paginated list of contacts. |
| 204 | No contacts visible for this user. |
| 403 | Insufficient rights. |

