## Project packages standard organization
The project is organized in a modular way. Each module is a package and has its own but common structure
- module name (like users)
  - mdb (mongo database)
    - entities (data structure)
      - sub (sub entities) 
    - repositories (data access)
      - _EntitiesRepository.java_ (ex UsersRepository.java)
  - pdb (postgres database)
    - entities (data structure)
    - repositories (data access)
    - migrations (database schema migration)
  - services (business logic)
    - _ModNameInit.java_ (module initialization ex UsersInit.java)
    - _ModNameEntityCache.java_ (entity cache ex UsersRolesCache.java)
  - api (rest api)
    - interfaces (api definition)
  - config (configuration)

## Rules
- A service can only refer a Repository belonging to the same module
- 

## Project naming conventions
The collection and databases are prefixed by the module name. As an example, the table containing the list of 
users for the module users is named `users_users`. The Object definition like `User` is stored in `users_users`. 

## Logs
Logs contains the module name at the beginning of the line to get this like in the logs:
```
2020-01-01 12:00:00.000 INFO  [users] User created
```
The logging level respects the following rules:
- `debug`: information level following the program flow.
- `info`: report important information corresponding to a normal use of the application.
- `warn`: report a situation that is not normal, but the software can handle it.
- `error`: report a situation that is not normal, the software can't handle it, as a consequence 
it impacts the execution.
