organization := "com.typesafe"

name := "slick-scalaexchange2012"

version := "1.0"

scalaVersion := "2.10.0-RC2"

scalacOptions += "-deprecation"

libraryDependencies ++= List(
  "com.typesafe" % "slick_2.10.0-RC2" % "0.11.3",
  "com.h2database" % "h2" % "1.3.166",
  "org.xerial" % "sqlite-jdbc" % "3.6.20",
  "org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
/*
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "ch.qos.logback" % "logback-classic" % "0.9.28"
*/
)
