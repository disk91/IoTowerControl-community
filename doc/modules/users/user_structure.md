## User data structure
The user data structure is defined as follows:

```json
{
  "id": "string",                 // technical uniq identifier
  "version": "number",            // user structure version
  "login": "string",              // user name for login, this is a hash of the email
  "password": "string",           // password hash
  "email": "string",              // user email [encrypted]
  "roles": [
    { "role": "string" }          // user role collection, role are predefined
  ],
  "acls": [
    { "group": "string" }         // user acl collection, acl are based on groups and dynamic
  ],
  "salt": [ "numbers" ],          // encryption salt 
  "secret" :  "string",           // session signature salt for token repudiation
  "userSecret" : "string",        // secret key computed from password allowing to deactivate user data without removing user
  "lastLogin": "date",            // last login date in MS since epoch
  "countLogin": "number",         // login count
  "registrationDate": "date",     // user creation date in MS since epoch
  "registrationIP": "string",     // IP address of the registration [encrypted]

  "modificationDate": "date",     // last user modification date in MS since epoch
  "validationId": "string",       // randomly generated secret key for validation
  "passwordResetId" : "string",   // randomly generated secret key for password reset
  "passwordResetExp" : "number",  // Expiration for password reset in MS since epoch
  
  "active": "boolean",            // user active flag
  "locked": "boolean",            // user locked flag
  "expiredPassword": "boolean",   // flag to force password change
  "apiAccount" : "boolean",       // flag to indicate if user is an API account
  "apiAccountOwner" : "string",   // user login of the owner of the API account
  
  "language": "string",           // user language (ISO country)
  
  "alert preference": {
    "email": "boolean",           // user email alert preference
    "sms": "boolean",             // user sms alert preference
    "push": "boolean"             // user push alert preference 
  },
  
  // Profile information
  "profile": {
        "gender": "string",           // user gender to be used
        "firstname": "string",        // user first name [encrypted]
        "lastname": "string",         // user last name [encrypted]
        "phone": "string",            // user phone number [encrypted]
        "address": "string",          // user address [encrypted]
        "city": "string",             // user city [encrypted]
        "zip": "string",              // user zip code [encrypted]
        "country": "string",          // user country [encrypted]
        "customFields": [{            // user custom fields
           "name": "string",          // custom field key [clear]
           "value": "string"          // custom field value [encrypted] 
        }]
    },
    "billingProfile": {
        "gender": "string",           // user gender to be used
        "firstname": "string",        // user first name [encrypted]
        "lastname": "string",         // user last name [encrypted]
        "companyName": "string",     // user company name [encrypted]
        "phone": "string",            // user phone number [encrypted]
        "address": "string",          // user address [encrypted]
        "city": "string",             // user city [encrypted]
        "zip": "string",              // user zip code [encrypted]
        "country": "string",          // user country [encrypted]
        "countryCode": "string",      // user 2 digit standard country code
        "vatNumber": "string",        // user VAT number [encrypted]
        "customFields": [{            // user custom fields
          "name": "string",           // custom field key [clear]
          "value": "string"           // custom field value [encrypted] 
         }]
    },
    "conditionValidation": "boolean",   // user condition validation flag
    "conditionValidationDate": "date",  // user condition validation date
    "conditionValidationVer": "string", // user condition version
 
    "lastComMessageSeen": "number"      // last communication message seen
}
```

### Role definition
Roles are associated to every user and gives user access to particular API endpoints. Roles are stored in the 
session token to avoid verifications on every call. As a consequence, the role refresh requires a session refresh.

- `ROLE_GOD_ADMIN`: this role is the super admin role, it does not give specific permissions but influence the other role to give full access on not owned artifacts
- `ROLE_USER_ADMIN`: this role is the user admin role, it has the right to manage users
- `ROLE_GROUP_ADMIN`: this role manage the group creation, configuration and association of user & devices to groups
- `ROLE_GROUP_LADMIN`: this role allows a user to create and configure an owned group and self assign to it
- `ROLE_DEVICE_ADMLIN`: this role allows to register devices
- `ROLE_DEVICE_READ`: this role gives access to owned device data.
- `ROLE_DEVICE_WRITE`: this role gives access to device description metadata modifications.
- `ROLE_DEVICE_CONFIG`: this role allows to modify the device technical configuration
- `ROLE_DEVICE_ALERTING`: this role allows to receive device alerts
- `ROLE_BACKEND_CAPTURE`: this role is dedicated to technical account allowed to report device data to the platform


- `ROLE_PENDING_USER`: this role is assigned to a user not yet registered into the system
- `ROLE_REGISTERED_USER`: this role is assigned to a user registered into the system (confirmed email)
- `ROLE_LOGIN_1FA`: this role is assigned on login with 1st factor to allow access 2nd Factor API
- `ROLE_LOGIN_2FA` : this role is assigned on 2nd factor login to indicate user has 2FA. Certain api can expect 2FA and user can apply 2FA as a second step.

- Role can be added dynamically by some other modules to cover certain authorizations specific for them, see [dynamic role](dynrole_structure.md)

### User life cycle
A user can be created manually by an administrator or after a self registration. User creation have different steps depending on the selected path:
- self registration:
    - User is registering with an email, until the email has been confirmed, the user does not exist in the system.
    - A confirmation email is sent to the user with a secret to validate and create the user. 
    - Following the confirmation email will create the user structure and assign the `ROLE_PENDING_USER` role.
    - User can be redirected to profile configuration or will do it later on its own base on boolean parameter `user.pending.forceprofile`
    - The user can then be validated by an administrator to assign the `ROLE_REGISTERED_USER` role or automatically moved to that status depending on the configuration boolean parameter `user.pending.autovalidation`
- Administrator registration:
    - User is created by an administrator with a `ROLE_PENDING_USER` role.
    - On first login, user will have to change its password.
    - User can be redirected to profile configuration or will do it later on its own base on boolean parameter `user.pending.forceprofile`
    - The user can then be validated by an administrator to assign the `ROLE_REGISTERED_USER` role or automatically moved to that status depending on the configuration boolean parameter `user.pending.autovalidation` 

User can delete his account, also after a given period of inactivity, the user account will be frozen by removing the `userSecret` value. This value is used for encrypting user information,
as a consequence, without a new login of the user with the right password, the data will stay encrypted and not accessible, even for the platform administrator.  The frozen period is decided by the `user.max.inactivity` parameter.

* User signout updates the `secret` value for token repudiation.
* API accounts have a long life JWT token, the expiration is set to 1 year by default, but can be configured by the `user.api.token.exp` parameter.
  ROLE_USER_ADMIN can create apiAccount and generate JWT for them with an API endpoint. 

### Data encryption
Data are AES encrypted, the encryption key is composed by
- a Server key from parameter `user.server.key` from configuration file, randomly generated
- an Application key from parameter `user.application.key` from Jar file, randomly generated
- a User key from the `userSecret` field, generated from user password (field `password` is just a hash)

### JWT signature
JWT signature depends on
- a Server key from parameter `user.server.key` from configuration file, randomly generated
- a User key from the `secret` field, ramdomly generated at user creation and modified after every user signout.

### Password change
On password change, the data encryption need to be recreated as the `userSecret` will be different.

### API
