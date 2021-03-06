name               := "Muta"
version            := "0.7.0"
organization       := "de.sciss"
scalaVersion       := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.10.6")
description        := "Genetic Algorithms"
homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))
licenses           := Seq("LGPL v3+" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt"))

resolvers     += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "de.sciss" %% "fileutil"         % "1.1.2",    // Easy file representation
  "de.sciss" %% "desktop"          % "0.7.2",    // Application framework
  "de.sciss" %% "treetable-scala"  % "1.3.8",    // Tree-table widget
  "de.sciss" %% "guiflitz"         % "0.5.0",    // Automatic configuration GUIs
  "de.sciss" %% "processor"        % "0.4.0",    // Asynchronous iteration
  "de.sciss" %% "rating-scala"     % "0.1.1",    // Rating widget
  "de.sciss" %% "play-json-sealed" % "0.4.0"     // JSON serialization
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8", "-Xlint")

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}
