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
 * K4 object deserializer
 */
public class K4ObjectDeserializer extends WrappedScalaObjectDeserializer {
    public K4ObjectDeserializer(InputStream input) {
        super(net.kfoundation.scala.serialization.K4ObjectDeserializer
            .FACTORY().of(input));
    }
}