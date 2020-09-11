package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

import java.io.InputStream;



public class K4ObjectDeserializer extends WrappedScalaObjectDeserializer {

    public K4ObjectDeserializer(UString str) {
        super(net.kfoundation.scala.serialization.K4ObjectDeserializer
            .FACTORY().of(str.asScala()));
    }

    public K4ObjectDeserializer(InputStream input) {
        super(net.kfoundation.scala.serialization.K4ObjectDeserializer
            .FACTORY().of(input));
    }

}