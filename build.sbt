ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "lec7",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.14",
      "io.d11" %% "zhttp" % "1.0.0.0-RC27"
    )
  )
