// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala

import net.kfoundation.scala.i18n.Localizer.l

object KFException {
  private def findCause(args: Seq[Any]): Option[Throwable] =
    args.find(_.isInstanceOf[Throwable])
      .map(_.asInstanceOf[Throwable])
}

/** WIP */
class KFException(key: String, args: Any*) extends
  Exception(null, KFException.findCause(args).orNull)
{
  override def getMessage: String = l(key, args)
}
