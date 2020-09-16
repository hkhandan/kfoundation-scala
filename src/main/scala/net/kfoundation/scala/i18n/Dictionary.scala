package net.kfoundation.scala.i18n



object Dictionary {
  class Value(val dialect: LanguageLike, val text: String)

  class Key(val name: String, values: Set[Value]) {
    private val valueMap = values.map(v => v.dialect -> v.text).toMap
    def get(lang: LanguageLike): Option[String] = valueMap.get(lang)
  }

  class Scope(val name: String, keys: Set[Key]) {
    private val keyMap = keys.map(k => (k.name, k)).toMap
    def get(keyName: String): Option[Key] = keyMap.get(keyName)
  }

  private val ANY = new Dialect(new Language("*", "any"), Region.WORLD)

  val DEFAULT_FALLBACKS = Map(
    ANY -> Language.EN,
    Language.EN -> Dialect.EN_US)

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
