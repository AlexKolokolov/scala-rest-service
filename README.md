# Scala REST Service

REST service for testing Akka HTTP and Slick
with in-memory H2 database.

The service manages simple on-line shop database.

Domain includes 3 main entity types: `Customer`, `Product` and `Order`.

`Order` consists of `OrderItem`s and has enum property `OrderStatus` 
which can accept following values: `Created`, `Confirmed`, `Suspended`,
`Delivered` and `Aborted`

`Product` has properties of types `ProductCategory` and `ProductVendor`
 
#### Usage:
To start web service on `localhost:8080` execute `sbt run`
#### Api:
See `http://localhost:8080/swagger`