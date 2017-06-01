# Scala REST Service

REST service for testing Akka HTTP and Slick
with PostgreSQL DB and in-memory H2 DB for tests.

The service manages simple on-line shop database.

Domain includes 3 main entity types: `Customer`, `Product` and `Order`.

`Order` consists of `OrderItem`s and has enum property `OrderStatus` 
which can accept following values: `Created`, `Confirmed`, `Suspended`,
`Delivered` and `Aborted`

`Product` has properties of types `ProductCategory` and `ProductVendor`
 
#### Usage:
To create and pre-populate database tables run `sbt liquibase-update`

To drop database tables run `sbt liquibase-rollback-count 2`

To start web service on `localhost:8080` run `sbt run`

#### Api:
Check `http://localhost:8080/swagger`