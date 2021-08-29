import Dependencies.Libraries._

name := "akka-http-products"

version := "0.1"

scalaVersion := "2.13.6"

lazy val settings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-feature",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-deprecation",
    "-Xfatal-warnings",
    "-encoding",
    "UTF-8",
    "-Ywarn-dead-code",
  ),
  exportJars := true,
)

lazy val rootProject = project
  .in(file("."))
  .settings(
    name := "slick-poc"
  )
  .aggregate(common, api)

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= basicDeps ++ circe ++ Seq(catsCore) ++ unitTests,
  )

lazy val api = project
  .settings(
    name := "api",
    settings,
    libraryDependencies ++= basicDeps ++ akka ++ circe ++ unitTests,
  )
  .dependsOn(common)
