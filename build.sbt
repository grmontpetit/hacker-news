name := "hacker-news"

version := "0.0.1"

organization := "sniggel"

scalaVersion  := "2.11.4"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

fork := true

libraryDependencies ++= {
  val akkaV = "2.5.8"
  Seq(
    "com.typesafe.akka"    %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"    %%  "akka-testkit"   % akkaV   % "test",
    "com.typesafe.akka"    %% "akka-http" % "10.0.11",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0"
  )
}

