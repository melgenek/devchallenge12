### Software requirements

* install java 8 (at least 1.8.0_51)
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
* docker  (docker-compose 1.8.0 - required for tests)
https://docs.docker.com/install/
https://docs.docker.com/install/linux/docker-ce/ubuntu/

### Build docker images
All further commands should be executed from the root of the project.
```
 ./xsbt.sh docker
```


### Run sample

* To start sample setup with 1 API and 1 processor.  
```
docker-compose -f docker/docker-compose.yml up
```
What is inside:
* 2 instances of application on ports 9001 and 9002
* consul for service discovery
* kafka to pass messages asynchronously
* redis instances as a store
* registrator that automatically registers api instances in consul


### Http methods

* Get all server records (`dump`):

```
curl http://localhost:9001/records
```

* Put value:

```
curl -X PUT -H "Content-Type: application/json" -d "{\"value\":\"some_value1\", \"author\":\"any\" }"  http://localhost:9001/records/name1
```
Responds with 200 code

* Get value:

```
curl http://localhost:9001/records/name1
```

Sample response:
```
{
"value":"some_value1",
"author":"any",
"id":"7bfc8b89-23e2-4975-b626-0c2e1132b142",
"createdAt":"2018-06-23T14:16:23.810Z[GMT]"
}
```

* Get history for key

```
curl http://localhost:9001/records/name1/history
```
Sample response:

```
[
{
"value":"some_value111",
"author":"any",
"id":"117522ae-fa0d-416a-8828-56dba65d4789",
"createdAt":"2018-06-23T14:18:59.808Z[GMT]"
},

{
"value":"some_value1",
"author":"any",
"id":"7bfc8b89-23e2-4975-b626-0c2e1132b142",
"createdAt":"2018-06-23T14:16:23.810Z[GMT]"
}
]
```

* Delete value:
```
curl -X DELETE http://localhost:9001/records/name1
```

* there is additional /internal endpoint to store model with assigned id and creation time.
It is not supposed to be opened to end user

### Algorithm and replication
According to discussion in https://finaldev12.slack.com/archives/CBBVCCLSY/p1529743312000022
the requirement for storing data is as follows:
* when record is created for the first time all the nodes alive need to get data synchronously
* when the record is updated it can be updated eventually

Alive nodes - the nodes registered in consul
The algo:
* get  all alive nodes from consul
* use internal http method to store data
* to update data inside applications that are not alive the data is stored in kafka

Problems and solutions:
* http call and kafka processing can handle record twice.
Solution: unique id is assigned to each event so that it is stored only 
when data was not stored earlier
* order of changes can be corrupted.
Solution: data in kafka is guaranteed to be processed one after another based on partition key.
The key for the records in the system is `name`.

### Testsuite for N-master replication 

Please, stop containers from previous sections:
```
docker-compose -f docker/docker-compose.yml down
```
or even:
```
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
```

To run tests checking replication use
```
./xsbt.sh it:test
```

There are three applications running:
* api1 and api2 are registered in consul
* api3 that is not registered in consul

The data is processed by all of the services by means of 
synchronous (http) and asynchronous processing (kafka)

###  Fault tolerance technique/replication. Onboarding/restore algorithm

The servers that were on maintenance for any reason can restore data kafka events.
Current application has listener inside it but the 
`api` is likely to be split into `api` and `listener` so that 
processing is performed while api is not working.
Each `listener group` starts processing from last event that was processed by it (offset).
Listener groups allow each application to process events independently.

Newly created application can upload dump from other servers (upload is not implemented)
and start processing kafka messages. Processing same message is idempotent (will not be stored second time or corrupt order).
Dump is required because kafka is most likely configured to remove messages in some period of time.

To check that onboarding works correctly:
* start docker first:
```
docker-compose -f docker/docker-compose.yml up
```
* perform operations on api with port 9001
* start additional api with port 9003 and another listener group
```
docker-compose -f docker/docker-compose-additional.yml up
```
Data should be replicated to 9003










