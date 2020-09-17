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

import java.util.Optional;



/**
 * Standard framework of an object deserializer
 */
public interface ObjectDeserializer {

    /**
     * Reads the sequence representing the beginning of an object if any,
     * otherwise throws an exception.
     * @return Type name of the object if provided by the input, otherwise,
     * None.
     */
    Optional<UString> readObjectBegin();


    /**
     * Reads the sequence representing the end of an object if any, otherwise
     * throws an exception.
     * @return Type name of the object if provided by the input, otherwise,
     * None.
     */
    Optional<UString> readObjectEnd();


    /**
     * Reads the sequence representing the beginning a collection if any,
     * otherwise throws an exception.
     */
    void readCollectionBegin();


    /**
     * Attempts to read the sequence representing the end of a collection and
     * reports if the attempt was successful.
     */
    boolean tryReadCollectionEnd();


    /**
     * Reads a string literal from the input if any, or otherwise
     * throws an exception.
     */
    UString readStringLiteral();


    /**
     * Reads an integer literal from the input if any, or otherwise throws an
     * exception.
     */
    long readIntegerLiteral();


    /**
     * Reads a possibly fractional decimal number from input if any, or
     * otherwise throws an exception.
     */
    double readDecimalLiteral();


    /**
     * Reads a boolean literal from input if any, or otherwise throws an
     * exception.
     */
    boolean readBooleanLiteral();


    /**
     * Attempts to read a property name (property definition) from input and
     * return its value. Returns empty if the attempt fails.
     */
    Optional<UString> tryReadPropertyName();

}