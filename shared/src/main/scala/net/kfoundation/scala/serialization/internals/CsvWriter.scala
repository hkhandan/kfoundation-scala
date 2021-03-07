// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization.internals

import net.kfoundation.scala.UString

import java.io.{OutputStream, PrintWriter}
import java.text.{DateFormat, DecimalFormat, SimpleDateFormat}
import java.util.Date

object CsvWriter {
  val DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy")
  val DEFAULT_NUMBER_FORMAT = new DecimalFormat("#.0######")
  val DEFAULT_SEPARATOR: UString = ", "
}

class CsvWriter(output: OutputStream,
  separator: UString = CsvWriter.DEFAULT_SEPARATOR,
  dateFormat: DateFormat = CsvWriter.DEFAULT_DATE_FORMAT,
  decimalFormat: DecimalFormat = CsvWriter.DEFAULT_NUMBER_FORMAT)
  extends AutoCloseable
{
  // --- FIELDS --- //

  private var nCols = -1
  private var colCounter = 0
  private val writer = new PrintWriter(output)


  // --- MEtHODS --- //

  private def newCol(): Unit = {
    if (colCounter != 0) writer.print(separator)
    colCounter += 1
  }


  /**
   * Writes a new integer cell.
   *
   * @param value - The value to write.
   * @return Reference to this object for method chaining.
   */
  def write(value: Integer): CsvWriter = {
    newCol()
    writer.print(value)
    this
  }


  /**
   * Writes a new long cell.
   *
   * @param value - The value to write.
   * @return Reference to this object for method chaining.
   */
  def write(value: Long): CsvWriter = {
    newCol()
    writer.print(value)
    this
  }


  /**
   * Writes a new double cell.
   *
   * @param value - The value to write.
   * @return Reference to this object for method chaining.
   */
  def write(value: Double): CsvWriter = {
    newCol()
    writer.print(decimalFormat.format(value))
    this
  }


  /**
   * Writes a new Date cell.
   *
   * @param value - The value to write.
   * @return Reference to this object for method chaining.
   */
  def write(value: Date): CsvWriter = {
    newCol()
    writer.print(dateFormat.format(value))
    this
  }


  /**
   * Writes a new String cell.
   *
   * @param value - The value to write.
   * @return Reference to this object for method chaining.
   */
  def write(value: String): CsvWriter = {
    newCol()
    if (value == null) return this
    writer.print('"')
    writer.print(value)
    writer.print('"')
    this
  }


  /**
   * Gets ready to write a new row.
   */
  def newRow(): Unit = {
    writer.print('\n')
    writer.flush()
    if (nCols == -1) {
      nCols = colCounter
    } else if (nCols != colCounter) {
      throw new Error("Column count of the last row does not match the header.")
    }
    colCounter = 0
  }


  def close(): Unit = {
    writer.close()
  }
}
