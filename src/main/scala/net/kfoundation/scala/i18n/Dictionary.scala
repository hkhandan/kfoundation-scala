package net.kfoundation.scala.i18n

import java.io.InputStream
import java.net.URL

import scala.xml.{Elem, XML}



object Dictionary {
  private class Value(val dialect: LanguageLike, val text: String)

  private class Key(val name: String, values: Set[Value]) {
    private val valueMap = values.map(v => v.dialect -> v.text).toMap
    def get(lang: LanguageLike): Option[String] = valueMap.get(lang)
  }

  private class Scope(val name: String, keys: Set[Key]) {
    private val keyMap = keys.map(k => (k.name, k)).toMap
    def get(keyName: String): Option[Key] = keyMap.get(keyName)
  }

  private val ANY = new Dialect(new Language("*", "any"), Region.WORLD)
  private val DEFAULT_FALLBACKS = Map(
    ANY -> Language.EN,
    Language.EN -> Dialect.EN_US)

  def load(url: URL): Dictionary = readScopes(XML.load(url))
  def load(stream: InputStream): Dictionary = readScopes(XML.load(stream))

  private def readScopes(e: Elem): Dictionary = new Dictionary(
    (e \ "Scope").map(n => readScope(n.asInstanceOf[Elem])).toSet,
    DEFAULT_FALLBACKS)

  private def readScope(e: Elem): Scope = new Scope(
    e \@ "name",
    (e \ "Key").map(n => readKey(n.asInstanceOf[Elem])).toSet)

  private def readKey(e: Elem): Key = new Key(
    e \@ "name",
    (e \ "Value").map(n => readValue(n.asInstanceOf[Elem])).toSet)

  private def readValue(e: Elem): Value = new Value(
    Dialect.of(e \@ "lang"), e.text)
}



class Dictionary private(
  scopes: Set[Dictionary.Scope],
  fallbackMap: Map[LanguageLike, LanguageLike])
{
  import Dictionary._


  private val scopeMap = scopes.map(s => (s.name, s)).toMap


  def get(keyPath: String, language: LanguageLike,
    defaultScope: Option[String]): Option[String] =
  {
    val i = keyPath.lastIndexOf(".")

    val scope: Option[Scope] = if(i == -1) {
      defaultScope.flatMap(scopeMap.get)
    } else {
      val scopeName = keyPath.substring(0, i-1)
      if(scopeName.isEmpty) None else scopeMap.get(scopeName)
    }

    val key: Option[Key] = scope.flatMap(s => {
      val keyName = keyPath.substring(i + 1)
      if(keyName.isEmpty) None else s.get(keyName)
    })

    key.flatMap(k => get(k, language))
  }


  private def get(key: Key, language: LanguageLike): Option[String] =
    key.get(language)
      .orElse(fallbackMap.get(language)
        .flatMap(l => get(key, l)))
}
