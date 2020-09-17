// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;

import java.io.OutputStream;


/**
 * XML obejct serializer.
 */
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
