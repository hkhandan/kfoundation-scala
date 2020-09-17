// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;



/**
 * Represents an error occurred during deserialization
 */
public class DeserializationError extends RuntimeException {
    public DeserializationError(String message) {
        super(message);
    }
    public DeserializationError(String message, Throwable cause) {
        super(message, cause);
    }
}
