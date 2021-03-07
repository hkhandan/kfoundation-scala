// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import net.kfoundation.scala.UString
import net.kfoundation.scala.util.Flow


/** Implement this class to have renderer process your content.  */
abstract class Document {
  val size: Flow.Inlet[Size] = Flow.inlet
  def apply(mediaTraits: MediaTraits, storedState: Flow.Inlet[UString]): Flow[Content]
}
