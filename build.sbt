name          := "Muta"

version       := "0.4.1"

organization  := "de.sciss"

scalaVersion  := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

description   := "Genetic Algorithms"

homepage      := Some(url("https://github.com/Sciss/" + name.value))

licenses      := Seq("LGPL v3+" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt"))

resolvers     += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "de.sciss" %% "fileutil"         % "1.1.1",    // Easy file representation
  "de.sciss" %% "desktop"          % "0.5.3",    // Application framework
  "de.sciss" %% "treetable-scala"  % "1.3.7",    // Treetable widget
  "de.sciss" %% "guiflitz"         % "0.3.2",    // Automatic configuration GUIs
  "de.sciss" %% "processor"        % "0.3.0",    // Asynchronous iteration
  "de.sciss" %% "rating-scala"     % "0.1.1",    // Rating widget
  "de.sciss" %% "play-json-sealed" % "0.2.0"     // JSON serialization
)

// retrieveManaged := true

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture")

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (version.value endsWith "-SNAPSHOT")
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

