## Intracom Service

The intracom service allow to broadcast a communication from one instance of a service to
the other instances of that service. The purpose is as an exemple to force the instances resynchronizatoin
from the database.

The implementation of this service can be made with the MongoDB shared database or with a message queue
broker like mosquitto. The selection is indicated by the following configuration lines in the 
configuration file:

```properties

```


In the current implementation only database is implemented.
