name := "wall-emoji"

version := "0.1"

scalaVersion := "2.12.7"

assemblyJarName in assembly := "wall-emoji.jar"
mainClass in assembly := Some("fr.totetmatt.wallemoji.WebServer")

val akka_http_version = "10.1.5"
val twitter4j_version = "4.0.7"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akka_http_version
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akka_http_version

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.17"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % twitter4j_version
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % twitter4j_version

libraryDependencies += "com.vdurmont" % "emoji-java" % "4.0.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"