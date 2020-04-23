# scala2ts-core

**Note: There is no current stable version in Maven yet. This is a WIP. You can pull and publish
locally if you want to try it out. Documentation will be updated accordingly.**

---

> Transform your Scala classes, objects and traits into Typescript interfaces and classes

This repository contains the parser, transpiler and renderer for the scala2ts
project. It's primary purpose is to act as a [Scala compiler plugin](https://docs.scala-lang.org/overviews/plugins/index.html) that, on compile, will
produce equivalent Typescript typings for your desired Scala classes, objects and traits.

## SBT

It is highly recommended that if you use SBT for your project, to use the `scala2ts-sbt` SBT plugin
instead of this project directly. It streamlines adding the compiler plugin and configuring it for you.

You can find the project [here](https://github.com/scala2ts/scala2ts-sbt). If you dont use SBT or have a 
more complex build configuration, proceed below.

## Usage

```sbt
// build.sbt

lazy val yourProject = (project in file("."))
  .settings(
    autoCompilerPlugins := true,
    addCompilerPlugin("com.github.scala2ts" % "scala2ts-core" % "latestVersion"),
    scalacOptions := Seq(
      // See below for configuration options
    ) 
  )
```

Adding `autoCompilerPlugins` tells SBT to automatically include the `-Xplugin` scalac flag with the
appropriate path to found compiler plugins.

## Configuration

There are many configuration options in how and what Typescript is emitted from your Scala code. The defaults
of which can be found in the [`com.github.scala2ts.configuration.Configuration`](https://github.com/scala2ts/scala2ts-core/blob/master/src/main/scala/com/github/scala2ts/configuration/Configuration.scala#L3) case class.
For ease of use, here is the current list of configuration options and their defaults:

|**Option**|**Flag**|**Multi**|**Type**|**Default**|**Description**|
|---|---|---|---|---|---|
|fileIncludes|-P:scala2ts:file:includes|yes|Regex| |File path to include in compilation|
|fileExcludes|-P:scala2ts:file:excludes|yes|Regex| |File path to exclude in compilation|
|typeIncludes|-P:scala2ts:type:includes|yes|Regex| |Type name to include in compilation|
|typeExcludes|-P:scala2ts:type:excludes|yes|Regex| |Type name to exclude in compilation|
