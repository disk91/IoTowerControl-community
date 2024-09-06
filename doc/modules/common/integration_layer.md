## Service Integration Layer
The service integration layer allows communication between service and 
between instances of services. This is a common interface, integrated to all the 
services and not a specific service. It manages the communications using different 
methods to make the deployment flexible as a monolith or as independent, scalable, services.

Service integration is mostly asynchronous, synchronous call are polling the asynchronous API.

### Available services
A service can configure a list of services to be joined and to join other services.
The configuration is made in the service configuration file.

### Query API
Messages and responses are encapsulated into a common message.

```json
{
    'queryId': String,                    // Uid of the message to link with response
    'serviceNameSource' : QueryService,   // Service name for the source related to package (users)
    'serviceNameDest': QueryService,      // Service name for the destination (users)
    'type': QueryType,                    // Query type, fire & forget, broadcast, async, sync...
    'action' : number,                    // Query action, value depends on services
    'query': object,                      // Query parameters as an Object depending on oction
    'response': ActionResult,             // Result of the Query, sucess or error like Exception
    'result' : object,                    // Query response as an Object depending on action
    'state' : QueryState,                 // Query state, STATE_PENDING, STATE_DONE, STATE_ERROR
                                          // Query access request parallelism support
    'query_ts' : number,                  // Query start time ref, structure creation in ns
    'response_ts' : number,               // Response time ref, in ns
    'query_ms' : number                   // Query timestamp in ms for timeout
}
```

Integration is natively asynchronous, API will transform asynchronous call into synchronous call when preferred.
The following mechanism is used to manage the asynchronous call:
- The service sends a query message to the integration service
- The service wait for response (polling the `state` field)
- Integration service complete the query_ts timestamp, it route de message according to the configuration:
  - If the destination service is local, integration directly call the sync query function. 
    - The query is updated with the response, response_ts and state are updated
  - If the destination service is remote, integration send the message to the remote service using the proposed route MQTT or API (async)
    - Integration service stores the pending query.
    - The remote integration service process the response and call the async response handler
    - The async response handler updates the query, response_ts and state 
  
### Integration Service

Integration service stores the pending query in an hashmap based on the messageId.



#### local
The service communicates directly calling the Java Api of the service hosted locally.

