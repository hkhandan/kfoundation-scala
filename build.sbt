// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

// --- Project Information --- //

name := "KFoundation"
ThisBuild / version := "0.3"
ThisBuild / homepage := Some(url("https://mscp.co/kfoundation/about.html"))
ThisBuild / organization := "net.kfoundation"
ThisBuild / organizationName := "KFoundation Project"
ThisBuild / organizationHomepage := Some(url("http://www.kfoundation.net/"))
ThisBuild / licenses := List("KnoRBA License" ->
  new URL("https://www.mscp.co/resouces/article/license.html"))
ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/hkhandan/kfoundation-scala"),
  "scm:git@github.com:hkhandan/kfoundation-scala"))



// --- SBT Settings --- //

useCoursier := false
pomIncludeRepository := { _ => false }
publishMavenStyle := true
isSnapshot := version.value.endsWith("-SNAPSHOT")
publishConfiguration := publishConfiguration.value.withOverwrite(isSnapshot.value)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(isSnapshot.value)

val scalaGenDirectory = settingKey[File]("Generated sources")

val generateReadWritersImpl = Def.task {
  val targetDir = scalaGenDirectory.value
  Seq(
    ReadWriterGenerator._generateReadWriters(targetDir),
    ReadWriterGenerator._generateWriters(targetDir),
    ReadWriterGenerator._generateReaders(targetDir))
}



// --- Project Settings --- //

val latestScala = "2.13.3"
val latestScalaForSbt = "2.12.10"
val scalaVersions = List(latestScala, latestScalaForSbt)

scalacOptions += "-deprecation"

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.mavenCentral)

ThisBuild / libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"

ThisBuild / scalaVersion := latestScala


val shared = project.in(file("shared"))
  .settings(
    scalaGenDirectory := (Compile / sourceManaged).value / "net" / "kfoundation" / "scala" / "serialization",
    Compile / sourceGenerators += generateReadWritersImpl,
    publishArtifact := false)

val jsApi = project.in(file("js-api"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared)
  .settings(
    moduleName := "kfoundation-js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0",
    (Compile / unmanagedSourceDirectories) += (shared / Compile / scalaSource).value,
    (Compile / unmanagedSourceDirectories) ++= (shared / Compile / managedSourceDirectories).value,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })

val scalaApi = project.in(file("scala-api"))
  .dependsOn(shared)
  .settings(
    moduleName := "kfoundation-scala",
    (Compile / unmanagedSourceDirectories) += (shared / Compile / scalaSource).value,
    (Compile / unmanagedSourceDirectories) ++= (shared / Compile / managedSourceDirectories).value)

val javaApi = project.in(file("java-api"))
  .dependsOn(shared)
  .settings(
    moduleName := "kfoundation-java",
    crossPaths := false,
    Compile / doc / javacOptions ++= Seq("-Xdoclint:none", "-quiet"))

val kfoundation = project.in(file("."))
  .aggregate(shared, scalaApi, javaApi, jsApi)



// --- Deployment --- //

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}