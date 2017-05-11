
name := "scala-rest-service"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.1.7")

libraryDependencies ++= Seq("com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0")

libraryDependencies += "com.h2database" % "h2" % "1.4.194"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"