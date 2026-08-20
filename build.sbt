val http4sVersion = "0.23.34"

ThisBuild / scalaVersion := "3.8.4"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.typelevel" %% "log4cats-slf4j" % "2.8.0"
)
libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.18"
libraryDependencies += "org.http4s" %% "http4s-scalatags" % "0.25.3"
libraryDependencies += "com.google.cloud" % "google-cloud-firestore" % "3.45.0"
libraryDependencies += "is.cir" %% "ciris" % "3.15.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-explain",
  "-Wunused:all",
  "-Wvalue-discard"
  // "-Xfatal-warnings"    // turn all warnings into errors
)
