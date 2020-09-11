package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

import java.io.InputStream;



public class JsonObjectDeserializer extends WrappedScalaObjectDeserializer {

    public JsonObjectDeserializer(UString str) {
        super(net.kfoundation.scala.serialization.JsonObjectDeserializer
            .FACTORY().of(str.asScala()));
    }

    public JsonObjectDeserializer(InputStream input) {
        super(net.kfoundation.scala.serialization.JsonObjectDeserializer
            .FACTORY().of(input));
    }

}
