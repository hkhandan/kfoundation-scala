package net.kfoundation.i18n

import java.util.Locale

trait LanguageLike {
  def asDialect: Dialect
  def asLocale: Locale
  def getIetfTag: String
}