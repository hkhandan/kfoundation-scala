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
 * JSON object serializer
 */
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