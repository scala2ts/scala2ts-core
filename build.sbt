/**
 * scala2ts SBT build script
 */

import ReleaseTransformations._

lazy val root = project.in(file("."))
  .settings(
    name := "scala2ts",
    organization := "com.github.halfmatthalfcat",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    sbtPlugin := true,
    crossVersion := CrossVersion.binary,
    crossScalaVersions := Seq(
      scalaVersion.value,
      "2.12.10"
    ),
    libraryDependencies ++= Seq(
      "cz.habarta.typescript-generator" %  "typescript-generator-core"  % "2.21.588",
      "com.beachape"                    %% "enumeratum"                 % "1.5.15"
    ),
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
    pomExtra :=
      <url>https://www.github.com/halfmatthalfcat/scala2ts</url>
        <licenses>
          <license>
            <name>MIT</name>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:halfmatthalfcat/scala2ts.git</url>
          <connection>scm:git:git@github.com:halfmatthalfcat/scala2ts.git</connection>
        </scm>
        <developers>
          <developer>
            <id>halfmatthalfcat</id>
            <name>Matt Oliver</name>
            <url>https://www.github.com/halfmatthalfcat</url>
          </developer>
        </developers>,
    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    resolvers ++= Seq(DefaultMavenRepository),
  )
