// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java;

import java.io.OutputStream;



/**
 * Represents a Unicode character. Internally, it maintains both codepoint
 * and UTF-8 representations.
 */
public class UChar {

    private final net.kfoundation.scala.UChar impl;


    private UChar(net.kfoundation.scala.UChar impl) {
        this.impl = impl;
    }


    /**
     * Creates a UChar from a unicode codepoint.
     */
    public UChar(int codePoint) {
        this(new net.kfoundation.scala.UChar(codePoint));
    }


    /**
     * Creates a UChar from a raw UTF-8 encoded array of bytes.
     */
    public UChar(byte[] utf8) {
        this(new net.kfoundation.scala.UChar(utf8));
    }


    /**
     * Converts a Scala UChar to Java.
     */
    public static UChar of(net.kfoundation.scala.UChar scalaUChar) {
        return new UChar(scalaUChar);
    }


    /**
     * Converts a Java UChar to Scala.
     */
    public net.kfoundation.scala.UChar asScala() {
        return impl;
    }



    // --- Delegates --- //

    /**
     * Computes the number of octets used for UTF-8 representation based on the
     * first octet of a UTF-8 sequence.
     */
    public static byte getUtf8SizeWithFirstOctet(int firstOctet) {
        return net.kfoundation.scala.UChar.getUtf8SizeWithFirstOctet(firstOctet);
    }


    /**
     * Computes the number of octets needed for UTF-8 representation of the
     * given codepoint.
     */
    public static byte getUtf8SizeWithCodePoint(int codePoint) {
        return net.kfoundation.scala.UChar.getUtf8SizeWithCodePoint(codePoint);
    }


    /**
     * Encodes the given character (codepoint) to UTF-8.
     */
    public static byte[] encodeUtf8(int ch) {
        return net.kfoundation.scala.UChar.encodeUtf8(ch);
    }


    /**
     * Encodes the given character (codepoint) to UTF-8 and writes the result
     * to the given stream.
     */
    public static void encodeUtf8(int ch, OutputStream output) {
        net.kfoundation.scala.UChar.encodeUtf8(ch, output);
    }


    /**
     * Decodes a UTF-8 encoded character into its corresponding codepoint.
     */
    public static int decodeUtf8(byte[] input) {
        return net.kfoundation.scala.UChar.decodeUtf8(input);
    }


    /**
     * Encodes the given character (codepoint) to UTF-16.
     */
    public static char[] encodeUtf16(int codePoint) {
        return net.kfoundation.scala.UChar.encodeUtf16(codePoint);
    }


    /**
     * Decodes a UTF-8 character producing a UChar.
     */
    public static UChar valueOfUtf8(byte[] utf8) {
        return new UChar(net.kfoundation.scala.UChar.valueOfUtf8(utf8));
    }


    /**
     * Decodes a UTF-16 encoded character producing a UChar.
     */
    public static UChar valueOfUtf16(char w1, char w2) {
        return new UChar(net.kfoundation.scala.UChar.valueOfUtf16(w1, w2));
    }


    /**
     * Converts a native character into UChar.
     */
    public static UChar of(char ch) {
        return new UChar(net.kfoundation.scala.UChar.of(ch));
    }


    /**
     * Returns the codepoint for this character.
     */
    public int codePoint() {
        return impl.codePoint();
    }


    /**
     * Tests if this character is lower-case.
     */
    public boolean isLowerCase() {
        return impl.isLowerCase();
    }


    /**
     * Tests if this character is upper-case.
     */
    public boolean isUpperCase() {
        return impl.isUpperCase();
    }


    /**
     * Tests if this character is numeric.
     */
    public boolean isNumeric() {
        return impl.isNumeric();
    }


    /**
     * Tests if this character is alphabetic.
     */
    public boolean isAlphabet() {
        return impl.isAlphabet();
    }


    /**
     * Tests if this character is alphanumeric.
     */
    public boolean isAlphanumeric() {
        return impl.isAlphanumeric();
    }


    /**
     * Test if this character is a white space.
     */
    public boolean isWhiteSpace() {
        return impl.isWhiteSpace();
    }


    /**
     * Converts this character to lowercase.
     */
    public net.kfoundation.scala.UChar toLowerCase() {
        return impl.toLowerCase();
    }


    /**
     * Converts this character to upper case.
     */
    public net.kfoundation.scala.UChar toUpperCase() {
        return impl.toUpperCase();
    }


    /**
     * Returns the number of octets needed to store this character in UTF-8 format.
     */
    public int getUtf8Length() {
        return impl.getUtf8Length();
    }


    /**
     * Returns this character as encoded in UTF-8.
     */
    public byte[] toUtf8() {
        return impl.toUtf8();
    }


    /**
     * Returns this character as encoded in UTF-16.
     */
    public char[] toUtf16() {
        return impl.toUtf16();
    }


    /**
     * Writes the UTF-8 representation of this character to the given stream.
     */
    public void printToStream(OutputStream os) {
        impl.writeToStream(os);
    }


    /**
     * Appends this character to given builder (after conversion to native
     * character).
     */
    public void appendTo(StringBuilder builder) {
        builder.append(impl.toString());
    }


    @Override
    public int hashCode() {
        return impl.hashCode();
    }


    @Override
    public String toString() {
        return impl.toString();
    }


    @Override
    public boolean equals(Object other) {
        if(other == null) {
            return false;
        }
        if(other.getClass() != UChar.class) {
            return false;
        }
        return impl.equals(other);
    }

}
