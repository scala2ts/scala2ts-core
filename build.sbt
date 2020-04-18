/**
 * scala2ts SBT build script
 */

import ReleaseTransformations._

lazy val root = project.in(file("."))
  .settings(
    name := "scala2ts",
    organization := "com.github.scala2ts",
    scalaVersion := "2.13.1",
    crossVersion := CrossVersion.binary,
    crossScalaVersions := Seq(
      "2.11.12",
      "2.12.11",
      scalaVersion.value
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang"        %   "scala-compiler"  % scalaVersion.value,
      "org.scala-lang"        %   "scala-reflect"   % scalaVersion.value,
      "org.scalatra.scalate"  %%  "scalate-core"    % "1.9.5"
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
      <url>https://www.github.com/scala2ts/scala2ts-core</url>
        <licenses>
          <license>
            <name>MIT</name>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scala2ts/scala2ts-core.git</url>
          <connection>scm:git:git@github.com:scala2ts/scala2ts-core.git</connection>
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
    resolvers ++= Seq(DefaultMavenRepository)
  )
