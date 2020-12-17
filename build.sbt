import Dependencies._

ThisBuild / scalaVersion     := "2.13.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val circeVersion = "0.12.3"
val http4sVersion = "0.21.6"

lazy val root = (project in file("."))
  .settings(
    name := "diversity-score",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.typelevel" %% "cats-core" % "2.0.0-RC1",
      "org.typelevel" %% "cats-effect" % "2.1.3",
      "com.amazonaws" % "aws-java-sdk-comprehend" % "1.11.653",
      "com.softwaremill.sttp.client" %% "core" % "2.0.0-M7",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ),
    mainClass in assembly := Some("lib.Main"),
    assemblyOutputPath in assembly := file("dist/diversity-score.jar")
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
