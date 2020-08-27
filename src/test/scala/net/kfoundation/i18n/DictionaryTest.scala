package net.kfoundation.i18n

import java.io.ByteArrayInputStream

class DictionaryTest extends org.scalatest.flatspec.AnyFlatSpec {

  private val xml = """
    <?xml version="1.0" encoding="utf-8" ?>
    <Localization>
      <Scope name="net.kfoundation.io">
        <Key name="path-contains-slash-error">
          <Value lang="en">Path segment should not contain. Provided: s</Value>
        </Key>
      </Scope>
    </Localization>
    """

  "Dictionary" should "be able to load from XML" in {

  }

}
