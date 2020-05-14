name := "K-Foundation"
organization := "net.kfoundation"

scalaVersion in ThisBuild := "2.13.2"

version in ThisBuild := "0.1"
resolvers in ThisBuild += Resolver.mavenLocal
resolvers in ThisBuild += Resolver.mavenCentral

libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.1.1" % "test"
