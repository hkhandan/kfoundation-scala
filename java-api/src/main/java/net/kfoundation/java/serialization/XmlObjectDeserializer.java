// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

import java.io.InputStream;


/**
 * XML object deserializer.
 */
public class XmlObjectDeserializer extends WrappedScalaObjectDeserializer {

    public XmlObjectDeserializer(UString str) {
        super(net.kfoundation.scala.serialization.XmlObjectDeserializer
            .FACTORY().of(str.asScala()));
    }

    public XmlObjectDeserializer(InputStream input) {
        super(net.kfoundation.scala.serialization.XmlObjectDeserializer
            .FACTORY().of(input));
    }

}
