import com.jsuereth.sbtpgp.PgpKeys.publishSigned

// --- Project Information --- //

ThisBuild / name := "KFoundation-Scala"
ThisBuild / version := "0.2-SNAPSHOT"
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
  val targetDir = scalaGenDirectory.value / "net" / "kfoundation" / "serialization"
  Seq(
    ReadWriterGenerator.generateReaders(targetDir),
    ReadWriterGenerator.generateWriters(targetDir),
    ReadWriterGenerator.generateReadWriters(targetDir))
}



// --- Project Settings --- //



val latestScala = "2.13.3"
val latestScalaForSbt = "2.12.10"

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.mavenCentral)

ThisBuild / libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"
ThisBuild / libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.0.0-M1"

ThisBuild / scalaVersion := latestScalaForSbt
ThisBuild / crossScalaVersions := List(latestScala, latestScalaForSbt)
ThisBuild / scalaGenDirectory := (Compile / sourceManaged).value

Compile / sourceGenerators += generateReadWritersImpl

lazy val root = project.in(file("."))
  .aggregate(kfoundation.js, kfoundation.jvm)
  .settings(
    publish := {},
    publishLocal := {},
    publishSigned := {},
    publishM2 := {})

lazy val kfoundation = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    //name := (ThisBuild / name).value
    Compile / unmanagedSourceDirectories += scalaGenDirectory.value
  )



// --- Deployment --- //

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}