// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.logging.Logger
import net.kfoundation.scala.serialization.{ValueReader, ValueWriter}

import java.sql.{Connection, ResultSet, Statement}
import scala.util.{Failure, Success, Using}



object DatabaseClient {
  private val LOGGER = Logger(classOf[DatabaseClient])
}



/**
 * Facilitates database operations, specially in conjunction with
 * Universal Serialization.
 * @param cp A connection pool of reusable/disposable connections
 * @param interpreter Interpreter for the desired target database
 */
class DatabaseClient(cp: ConnectionPool, interpreter: DSLInterpreter) {
  import DatabaseClient._


  /**
   * Creates a connection to be used by fn, and makes it disposed
   * after being completion.
   */
  def withConnection[T](fn: Connection => T): T =
    Using(cp.newConnection)(c => fn.apply(c)) match {
      case Success(result) => result
      case Failure(e) => throw new DatabaseError(e)
    }


  /**
   * Creates a connection and a transaction with it to be used by fn,
   * disposes both after being used.
   * @param fn
   * @tparam T
   * @return The exact value returned by fn
   */
  def withTransaction[T](fn: Statement => T): T = Using.Manager(use => {
    val connection = use(cp.newConnection)
    val statement = use(connection.createStatement())
    val result = fn.apply(statement)
    connection.commit()
    result
  }) match {
    case Success(result) => result
    case Failure(e) => throw new DatabaseError(e)
  }


  /**
   * Creates a connection and a non-transactional statement with it to
   * be used by fn, disposes both after being used.
   * @param fn
   * @tparam T
   * @return The exact value returned by fn
   */
  def withoutTransaction[T](fn: Statement => T): T = Using.Manager(use => {
    val connection = use(cp.newConnection)
    connection.setAutoCommit(true)
    val statement = use(connection.createStatement())
    fn.apply(statement)
  }) match {
    case Success(result) => result
    case Failure(e) => throw new DatabaseError(e)
  }


  /**
   * Executes the given query and let's fn consume the result, then disposes
   * all used resources.
   * @param query
   * @param fn
   * @tparam T
   * @return
   */
  def query[T](query: String, fn: ResultSet => T): T = withoutTransaction(t => {
    LOGGER.info("Running: " + query)
    val rs = t.executeQuery(query)
    val result = fn.apply(rs)
    rs.close()
    result
  })

  /**
   * Executes the given update query and returns the number of updated rows.
   * @param query
   * @return
   */
  def update(query: String): Int = {
    LOGGER.info("Running: " + query)
    withTransaction(_.executeUpdate(query))
  }


  /**
   * Executes the given update query, and lets fn consume its result, then
   * closes up all used resources.
   * @param query
   * @param fn
   * @tparam T
   * @return
   */
  def update[T](query: String, fn: ResultSet => T): T = {
    LOGGER.info("Running: " + query)
    withTransaction(t => fn(t.executeQuery(query)))
  }


  /**
   * Insert the given value into the database, having table and field names
   * inferred from the given ValueWriter.
   * @param value
   * @param writer
   * @tparam T
   * @return
   */
  def insert[T](value: T)(implicit writer: ValueWriter[T]): Int =
    update(
      writer.toString(
        InsertQueryObjectSerializer.FACTORY,
        value).toString())


  /**
   * Updates all record matching with the given whereClause with the given
   * value, having table and field names inferred from the given ValueWriter.
   * @param value
   * @param whereClause
   * @param writer
   * @tparam T
   * @return
   */
  def update[T](value: T, whereClause: UString)(
      implicit writer: ValueWriter[T]): Int =
    update(
      writer.toString(
        UpdateQueryObjectSerializer.FACTORY,
        value).toString() +
      "\n    where " + whereClause)


  /**
   * Updates all records matching with the given condition, having table and
   * field names inferred from the given ValueWriter.
   * @param value
   * @param condition
   * @param writer
   * @tparam T
   * @return
   */
  def update[T](value: T, condition: DSL.Predicate)(
      implicit writer: ValueWriter[T]): Int =
    update(value, UString.builder
      .use(interpreter.append(_, condition))
      .build)


  /**
   * Deletes all records matching the given condition from the given table.
   * @param table
   * @param condition
   * @return
   */
  def delete(table: UString, condition: DSL.Predicate): Int =
    exec(DSL.delete(table, condition))


  /**
   * Executes the given select query, parsing and returning the result using
   * the given ValueReader.
   * @param q
   * @param reader
   * @tparam T
   * @return
   */
  def select[T](q: String)(implicit reader: ValueReader[T]): Seq[T] =
    query[Seq[T]](q, rs => reader.seqReader
      .read(new ResultSetObjectDeserializer(rs)))


  /**
   * Executes a select query composed using DSL.
   * @param query
   * @param reader
   * @tparam T
   * @return
   */
  def exec[T](query: DSL.Select)(implicit reader: ValueReader[T]): Seq[T] =
    select(UString.builder
      .use(interpreter.append(_, query))
      .build)

  /**
   * Executes and aggregation query composed using DSL.
   * @param query
   * @param reader
   * @tparam T
   * @return
   */
  def exec[T](query: DSL.Aggregate)(implicit reader: ValueReader[T]): T =
    select(UString.builder
      .use(interpreter.append(_, query))
      .build)
    .head


  /**
   * Executes a delete query composed using DSL.
   * @param query
   * @return
   */
  def exec(query: DSL.Delete): Int = update(UString.builder
    .use(interpreter.append(_, query))
    .build)


  /**
   * Closes the connection pool used by this object.
   */
  def close(): Unit = cp.close()
}
