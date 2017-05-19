# Scala REST Service

REST service for testing Akka HTTP and Slick
with in-memory H2 database.

The service manages simple messenger database.

Domain includes 3 entity types: User, Message and Comment.

User can create, update and delete Message and Comment for Message.
 
#### Usage:
To start web service on localhost:8080 => `sbt run`
#### Api:
To get all users => `GET http://localhost:8080/webapi/users`

To get the first user => `GET http://localhost:8080/webapi/users/1`

To get all messages of the first user => `GET http://localhost:8080/webapi/users/1/messages`

To get the first message of the first user => `GET http://localhost:8080/webapi/users/1/messages/1`

To get all comments to the first message of the first user => `GET http://localhost:8080/webapi/users/1/messages/1/comments`

To get all messages => `GET http://localhost:8080/webapi/messages`

To get the first message => `GET http://localhost:8080/webapi/messages/1`

To get all comments to the first message => `GET http://localhost:8080/webapi/messages/1/comments`

To get all comments => `GET http://localhost:8080/webapi/comments`

To add new user => `POST http://localhost:8080/webapi/users` with `{"name":"UserName","id":0}` in the request body

To add new message of the first user => `POST http://localhost:8080/webapi/messages` with `{"text":"Message text","authorId":1,"id":0}` in the request body

To add new comment to the first message of the first user => `POST http://localhost:8080/webapi/comments` with `{"text":"Comment text","messageId":1,"authorId":1,"id":0}` in the request body

To update the first user => `PUT http://localhost:8080/webapi/users` with `{"name":"UserName","id":1}` in the request body

To update the first message of the first user => `PUT http://localhost:8080/webapi/messages` with `{"text":"Message text","authorId":1,"id":1}` in the request body

To update the first comment of the first user to the first message => `PUT http://localhost:8080/webapi/comments` with `{"text":"Comment text","messageId":1,"authorId":1,"id":1}` in the request body

To delete the first user => `DELETE http://localhost:8080/webapi/users/1`

To delete the first message => `DELETE http://localhost:8080/webapi/messages/1`

To delete the first message => `DELETE http://localhost:8080/webapi/comments/1`