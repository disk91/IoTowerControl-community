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

## User interface and API

