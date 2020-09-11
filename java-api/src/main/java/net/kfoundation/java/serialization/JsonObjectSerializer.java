package net.kfoundation.java.serialization;

import java.io.OutputStream;


public class JsonObjectSerializer extends WrappedScalaObjectSerializer {

    public JsonObjectSerializer(OutputStream output, int indentSize) {
        super(net.kfoundation.scala.serialization.JsonObjectSerializer
            .FACTORY().of(output, indentSize));
    }

    public JsonObjectSerializer(OutputStream output) {
        super(net.kfoundation.scala.serialization.JsonObjectSerializer
            .FACTORY().of(output));
    }

}
