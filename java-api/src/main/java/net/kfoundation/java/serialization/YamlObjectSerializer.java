package net.kfoundation.java.serialization;

import java.io.OutputStream;



public class YamlObjectSerializer extends WrappedScalaObjectSerializer {

    public YamlObjectSerializer(OutputStream output, int indentSize) {
        super(net.kfoundation.scala.serialization.YamlObjectSerializer
            .FACTORY().of(output, indentSize));
    }

    public YamlObjectSerializer(OutputStream output) {
        super(net.kfoundation.scala.serialization.YamlObjectSerializer
            .FACTORY().of(output));
    }

}
