// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.db.postgres

import net.kfoundation.scala.db.{ConnectionPool, DatabaseClient}


class PostgreSQLClient(cp: ConnectionPool) extends
  DatabaseClient(cp, PostgresDSLInterpreter)
{
  def databaseExists(name: String): Boolean = withTransaction(s =>
    s.executeQuery(
      s"SELECT 1 FROM pg_database WHERE datname='$name'").next())

  def createDatabase(name: String): Unit = withoutTransaction(
      _.executeUpdate(s"create database $name"))
}
