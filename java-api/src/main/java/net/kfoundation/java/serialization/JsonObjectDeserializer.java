// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;

import java.io.InputStream;


/**
 * JSON object deserializer
 */
public class JsonObjectDeserializer extends WrappedScalaObjectDeserializer {
    public JsonObjectDeserializer(InputStream input) {
        super(net.kfoundation.scala.serialization.JsonObjectDeserializer
            .FACTORY().of(input));
    }
}
