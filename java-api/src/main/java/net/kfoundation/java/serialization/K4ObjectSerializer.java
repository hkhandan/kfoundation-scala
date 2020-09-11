package net.kfoundation.java.serialization;

import java.io.OutputStream;

public class K4ObjectSerializer extends WrappedScalaObjectSerializer {

    public K4ObjectSerializer(OutputStream input, int indentSize) {
        super(net.kfoundation.scala.serialization.K4ObjectSerializer
            .FACTORY().of(input, indentSize));
    }

    public K4ObjectSerializer(OutputStream input) {
        super(net.kfoundation.scala.serialization.K4ObjectSerializer
            .FACTORY().of(input));
    }

}
