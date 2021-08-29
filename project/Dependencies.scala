import Dependencies.Versions._
import sbt._

object Dependencies {

  object Versions {
    val akkaVersion          = "2.6.16"
    val akkaHttpVersion      = "10.2.6"
    val akkaHttpCirceVersion = "1.37.0"
    val circeVersion         = "0.14.1"

    val logbackVersion      = "1.2.5"
    val scalaLoggingVersion = "3.9.4"
    val pureConfigVersion   = "0.16.0"

    val scalaTestVersion = "3.2.9"

    val catsCoreVersion = "2.6.1"

  }

  object Libraries {
    // Logs
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging"   % Versions.scalaLoggingVersion
    val logback      = "ch.qos.logback"              % "logback-classic" % logbackVersion

    // Akka Framework
    val akkaActors    = "com.typesafe.akka" %% "akka-actor"        % Versions.akkaVersion
    val akkaHttp      = "com.typesafe.akka" %% "akka-http"         % Versions.akkaHttpVersion
    val akkaStream    = "com.typesafe.akka" %% "akka-stream"       % Versions.akkaVersion
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe"   % Versions.akkaHttpCirceVersion
    val akkaTestKit   = "com.typesafe.akka" %% "akka-testkit"      % Versions.akkaVersion
    val akkHttpTest   = "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttpVersion % Test

    // Circe
    val circeCore    = "io.circe" %% "circe-core"    % Versions.circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % Versions.circeVersion

    // PureConfig
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfigVersion

    // Cats
    val catsCore = "org.typelevel" %% "cats-core" % Versions.catsCoreVersion

    // ScalaTest
    val scalaTest         = "org.scalatest" %% "scalatest"                % Versions.scalaTestVersion
    val scalaTestFlatSpec = "org.scalatest" %% "scalatest-flatspec"       % Versions.scalaTestVersion % "test"
    val scalaTestMatchers = "org.scalatest" %% "scalatest-shouldmatchers" % Versions.scalaTestVersion % "test"

    val basicDeps = Seq(logback, scalaLogging, pureConfig)
    val akka      = Seq(akkaActors, akkaHttp, akkaStream, akkaHttpCirce)
    val circe     = Seq(circeCore, circeGeneric)
    val unitTests = Seq(scalaTest, akkHttpTest, akkaTestKit)
  }

}
