package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

import java.io.InputStream;



public class XmlObjectDeserializer extends WrappedScalaObjectDeserializer {

    public XmlObjectDeserializer(UString str) {
        super(net.kfoundation.scala.serialization.XmlObjectDeserializer
            .FACTORY().of(str.asScala()));
    }

    public XmlObjectDeserializer(InputStream input) {
        super(net.kfoundation.scala.serialization.XmlObjectDeserializer
            .FACTORY().of(input));
    }

}
