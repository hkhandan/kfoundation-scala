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
 * YAML object serializer.
 */
public class YamlObjectSerializer extends WrappedScalaObjectSerializer {

    public YamlObjectSerializer(OutputStream output, int indentSize) {
        super(net.kfoundation.scala.serialization.YamlObjectSerializer
            .FACTORY().of(output, indentSize, false));
    }

    public YamlObjectSerializer(OutputStream output) {
        super(net.kfoundation.scala.serialization.YamlObjectSerializer
            .FACTORY().of(output, 2, false));
    }

}
