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



/**
 * Standard framework for an object serializer.
 */
public interface ObjectSerializer {

    /**
     * Writes a property name (property definition sequence) to output.
     */
    ObjectSerializer writePropertyName(UString name);


    /**
     * Writes a string literal to the output.
     */
    ObjectSerializer writeLiteral(UString value);


    /**
     * Writes an integer literal to the output.
     */
    ObjectSerializer writeLiteral(long value);


    /**
     * Writes a possible fractional decimal number to the ouptut.
     */
    ObjectSerializer writeLiteral(double value);


    /**
     * Writes a boolean literal to the output.
     */
    ObjectSerializer writeLiteral(boolean value);


    /**
     * Writes the symbol representing null to the output.
     */
    ObjectSerializer writeNull();


    /**
     * Writes a sequence representing beginning of an object with the given name
     * to the output.
     */
    ObjectSerializer writeObjectBegin(UString name);


    /**
     * Writes a sequence representing the end of an object to the output.
     */
    ObjectSerializer writeObjectEnd();


    /**
     * Writes the sequence representing the beginning of a collection to the
     * output.
     */
    ObjectSerializer writeCollectionBegin();


    /**
     * Writes the sequence representing the end of a collection to the output.
     */
    ObjectSerializer writeCollectionEnd();


    /**
     * Signals the end of stream.
     */
    void writeStreamEnd();

}
