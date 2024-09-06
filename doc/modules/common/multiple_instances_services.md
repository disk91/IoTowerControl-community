## Multiple Service & instance of Service Concurrent management

### Shared databases
The databases are shared across services, each of the services can run its own and different database
with some advantage on scalability, but also it's impact in terms of inter-service communication latency.

Multiple instances of a given service must run on the same database, this is to avoid data inconsistency.
Access to this database requires synchronization between services.

### Database initialization
The database schema initialization is made on startup, it is preferable to run a single service first to
the others to deploy / update the database schema without getting concurrent attempt to update the schema.

### Database mutex interface
When a service need to perform an action in the database and this action can be made by multiple instances,
like a initial database setup, a mutex interface must be used to avoid data inconsistency.
We can manage multiple mutex for different actions, `MongoMutex` structure is the following:

```json
{
    "mutexId": "string",  // name of the mutex, unique for a mutex, shared between services
    "taken": "boolean",   // true when a service already taken the mutex
    "takenAt": "string",  // timestamp of the MogoMutex last reservation
    "takenBy": "string",  // identifier of the service that took the mutex
    "timeout": "number"   // timeout in milliseconds, then it can be retaken
}
```
The timeout indicates the maximum estimated duration of the critical section under
mutex protection. If the service fails to release the mutex, it will be automatically 
released after this period of time.

To use the mutex, the service will reserve it:
```java
  if ( mongoMutexService.P(
          "module-service-action",    // mutexId - unique for a mutex, shared between services
          "nodeId",                   // comes from parameter file to identify the node, ramdom string
          1000 )                      // mutex timeout in milliseconds 
  ){  
    // critical section
    ...
    // end of critical section
    mongoMutexService.V("module-service-action");
  }
```

A service may terminate the running action taking a mutex before exiting.

### Service synchronization
When an instance of a service make an action and need other instance to excuse an action 
(broadcast actions) it goes to the Intracom service. Intracom service can be implemented with
a database log or with a message queue channel. See [Intracom service](intracom_service.md)

### integration
This service manage the
service integration including between the multiple instances of a given service.
See the [integration layer](integration_layer.md) for more details.