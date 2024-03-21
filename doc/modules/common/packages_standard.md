## Project packages standard organization
The project is organized in a modular way. Each module is a package and has its own but common structure
- module name (like users)
  - mdb (mongo database)
    - entities (data structure)
      - sub (sub entities) 
    - repositories (data access)
  - pdb (postgres database)
    - entities (data structure)
    - repositories (data access)
    - migrations (database schema migration)
  - services (business logic)
  - api (rest api)
    - interfaces (api definition)
  - config (configuration)

