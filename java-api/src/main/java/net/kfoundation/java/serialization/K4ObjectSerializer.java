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
 * K4 object serializer
 */
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
