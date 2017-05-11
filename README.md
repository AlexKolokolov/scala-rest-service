# Scala REST Service

REST service for testing Akka HTTP and Slick

With in-memory H2 database
 
#### Usage:
To start web service on localhost:8080 => `sbt run`
#### Api:
To get all entities => `GET http://localhost:8080/webapi/entities`

To get first entity => `GET http://localhost:8080/webapi/entities/1`

To save new entity => `POST http://localhost:8080/webapi/entities` with `{"name":"Name","id":0}` in the request body

To delete entity => `DELETE http://localhost:8080/webapi/entities/1`

To update first entity => `PUT http://localhost:8080/webapi/entities` with `{"name":"Name","id":1}` in the request body


 

