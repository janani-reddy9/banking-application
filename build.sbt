name := """banking-application"""
organization := "com.somebank"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "5.3.1",
  "org.postgresql" % "postgresql" % "42.5.1"
)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test

Test/javaOptions ++= Seq("-Dconfig.file=conf/application-test.conf")
