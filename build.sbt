import com.jsuereth.sbtpgp.PgpKeys.publishSigned

// --- Project Information --- //

ThisBuild / name := "KFoundation-Scala"
ThisBuild / version := "0.1-SNAPSHOT"
ThisBuild / licenses := List("KnoRBA License" -> new URL("https://www.mscp.co/resouces/article/license.html"))
ThisBuild / homepage := Some(url("https://mscp.co/kfoundation/about.html"))
ThisBuild / organization := "net.kfoundation"
ThisBuild / organizationName := "KFoundation Project"
ThisBuild / organizationHomepage := Some(url("http://www.kfoundation.net/"))
ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/hkhandan/kfoundation-scala"),
  "scm:git@github.com:hkhandan/kfoundation-scala"))



// --- Dependency Management --- //

lazy val kfoundation = crossProject(JVMPlatform, JSPlatform).in(file("."))

lazy val root = project.in(file("."))
  .aggregate(kfoundation.js, kfoundation.jvm)
  .settings(
    publish := {},
    publishLocal := {},
    publishSigned := {},
    publishM2 := {}
  )

val latestScala = "2.13.3"
val latestScalaForSbt = "2.12.10"

scalaVersion := latestScalaForSbt
crossScalaVersions := List(latestScala, latestScalaForSbt)

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.mavenCentral)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"



// --- SBT Settings --- //

useCoursier := false
pomIncludeRepository := { _ => false }
publishMavenStyle := true
isSnapshot := version.value.endsWith("-SNAPSHOT")
publishConfiguration := publishConfiguration.value.withOverwrite(isSnapshot.value)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(isSnapshot.value)



// --- Deployment --- //

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}