// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.i18n

import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.util.WQName
import net.kfoundation.scala.parse.lex.CodeWalker
import net.kfoundation.scala.{UChar, UString}


object LocaleManager {
  private val AT = U"@"
  private val CBRACES_OPEN: UString = "{{"
  private val CBRACES_CLOSE: UString = "}}"
  private val CBRACE_CODE: Int = '{'


  private class LocalizerImpl(dict: MultiDictionary, lang: Dialect)
    extends Localizer
  {
    override def dialect: Dialect = lang

    private def read(walker: CodeWalker, values: Map[UString, UString]): UString = {
      val builder = UString.builder
      while(walker.hasMore) {
        walker.readAll(_ != CBRACE_CODE)
        builder.append(walker.getCurrentSelection)
        walker.commit()
        tryReadSymbol(walker).foreach(s => builder.append(
          values.get(s).filter(!_.isEmpty).map(apply).getOrElse(U"")))
      }
      builder.build
    }

    private def tryReadSymbol(walker: CodeWalker): Option[UString] =
      if(walker.hasMore && walker.tryRead(CBRACES_OPEN)) {
        walker.commit()
        walker.readAll(ch => UChar.isAlphabet(ch))
        val name = walker.getCurrentSelection
        walker.commit()
        if(name.isEmpty) {
          walker.lexicalErrorAtCurrentLocation("Missing symbol name")
        }
        if(!walker.tryRead(CBRACES_CLOSE)) {
          walker.lexicalErrorAtCurrentLocation("}} expected")
        }
        walker.commit()
        Some(name)
      } else {
        None
      }

    override def apply(key: UString): UString = {
      val wqn = WQName(key)
      if(wqn.parts.size > 1 && wqn.parts.head.equals(AT)) {
        dict.lookup(WQName(wqn.last), lang)
      } else {
        dict.lookup(wqn, lang)
      }
    }

    override def apply(message: LMessage): UString = {
      val template = apply(message.key)
      read(CodeWalker.of(template), message.params)
    }

    override def apply(key: UString, values: (UString, UString)*): UString = {
      val template = apply(key)
      read(CodeWalker.of(template), values.toMap)
    }
  }
}


class LocaleManager(dict: MultiDictionary) {
  import LocaleManager._

  def localizerOf(dialect: Dialect): Localizer =
    new LocalizerImpl(dict, dialect)
}