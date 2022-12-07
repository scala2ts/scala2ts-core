/**
 * scala2ts-core SBT build script
 */

import ReleaseTransformations._

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "scala2ts-core",
    organization := "com.github.scala2ts",
    scalaVersion := "2.13.1",
    crossVersion := CrossVersion.binary,
    crossScalaVersions := Seq(
      // TODO: "2.10.7"
      "2.11.12",
      "2.12.11",
      scalaVersion.value
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang"        %  "scala-compiler" % scalaVersion.value % "provided",
      "org.scala-lang"        %  "scala-reflect"  % scalaVersion.value % "provided",
      "com.beachape"          %% "enumeratum"     % "1.7.2",
      "com.lihaoyi"           %% "os-lib"         % (scalaBinaryVersion.value match {
        case "2.11" => "0.2.9"
        case "2.12" | "2.13" => "0.7.0"
      }),
      "com.lihaoyi"           %% "upickle"        % (scalaBinaryVersion.value match {
        case "2.11" => "0.7.4"
        case "2.12" | "2.13" => "1.1.0"
      })
    ),
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.scala2ts",
    test in assembly := {},
    assemblyOption in assembly :=
      (assemblyOption in assembly).value.copy(includeScala = false),
    /**
     * There's some classpath particularities when executing compiler plugins
     * so we need to make a fatjar
     * @see https://www.scala-lang.org/old/node/6664.html
     * @see https://github.com/sbt/sbt/issues/2255
     */
    packageBin in Compile := (assembly in Compile).value,
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
