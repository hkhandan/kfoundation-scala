package net.kfoundation.java.serialization;

import java.io.OutputStream;



public class XmlObjectSerializer extends WrappedScalaObjectSerializer {

    public XmlObjectSerializer(OutputStream output, int indentSize) {
        super(net.kfoundation.scala.serialization.XmlObjectSerializer
            .FACTORY().of(output, indentSize));
    }

    public XmlObjectSerializer(OutputStream output) {
        super(net.kfoundation.scala.serialization.XmlObjectSerializer
            .FACTORY().of(output));
    }

}
