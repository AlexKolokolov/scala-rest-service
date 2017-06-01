
name := "scala-rest-service"

version := "1.2-SNAPSHOT"

scalaVersion := "2.11.8"

parallelExecution in Test := false

val akkaHttpVersion = "10.0.6"

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.1.7")

libraryDependencies ++= Seq("com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0")

libraryDependencies += "com.h2database" % "h2" % "1.4.194" % "test"

libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion

libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"

libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.9.1"

libraryDependencies += "co.pragmati" %% "swagger-ui-akka-http" % "1.0.0"

libraryDependencies += "org.yaml" % "snakeyaml" % "1.18"

import com.github.sbtliquibase.SbtLiquibase

enablePlugins(SbtLiquibase)

liquibaseUsername := "postgres"

liquibasePassword := "q1"

liquibaseDriver   := "org.postgresql.Driver"

liquibaseUrl      := "jdbc:postgresql://localhost:5432/rest_service"

liquibaseChangelog := new java.io.File("src/main/resources/liquibase/changelog.xml")

//liquibaseUsername := ""
//
//liquibasePassword := ""
//
//liquibaseDriver   := "org.h2.Driver"
//
//liquibaseUrl      := "jdbc:h2:mem:work"