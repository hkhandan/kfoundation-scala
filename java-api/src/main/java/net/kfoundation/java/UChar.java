package net.kfoundation.java;

import scala.collection.mutable.StringBuilder;

import java.io.OutputStream;



public class UChar {

    private final net.kfoundation.scala.UChar impl;

    private UChar(net.kfoundation.scala.UChar impl) {
        this.impl = impl;
    }

    public UChar(int codePoint) {
        this(new net.kfoundation.scala.UChar(codePoint));
    }

    public UChar(byte[] utf8) {
        this(new net.kfoundation.scala.UChar(utf8));
    }

    public static UChar of(net.kfoundation.scala.UChar scalaUChar) {
        return new UChar(scalaUChar);
    }

    public net.kfoundation.scala.UChar asScala() {
        return impl;
    }

    // --- Delegates --- //

    public static byte getUtf8SizeWithFirstOctet(int firstOctet) {
        return net.kfoundation.scala.UChar.getUtf8SizeWithFirstOctet(firstOctet);
    }

    public static byte getUtf8SizeWithCodePoint(int codePoint) {
        return net.kfoundation.scala.UChar.getUtf8SizeWithCodePoint(codePoint);
    }

    public static byte[] encodeUtf8(int ch) {
        return net.kfoundation.scala.UChar.encodeUtf8(ch);
    }

    public static void encodeUtf8(int ch, OutputStream output) {
        net.kfoundation.scala.UChar.encodeUtf8(ch, output);
    }

    public static int decodeUtf8(byte[] input) {
        return net.kfoundation.scala.UChar.decodeUtf8(input);
    }

    public static char[] encodeUtf16(int codePoint) {
        return net.kfoundation.scala.UChar.encodeUtf16(codePoint);
    }

    public static UChar valueOfUtf8(byte[] utf8) {
        return new UChar(net.kfoundation.scala.UChar.valueOfUtf8(utf8));
    }

    public static UChar valueOfUtf16(char w1, char w2) {
        return new UChar(net.kfoundation.scala.UChar.valueOfUtf16(w1, w2));
    }

    public static UChar of(char ch) {
        return new UChar(net.kfoundation.scala.UChar.of(ch));
    }

    public int codePoint() {
        return impl.codePoint();
    }

    public boolean isLowerCase() {
        return impl.isLowerCase();
    }

    public boolean isUpperCase() {
        return impl.isUpperCase();
    }

    public boolean isNumeric() {
        return impl.isNumeric();
    }

    public boolean isAlphabet() {
        return impl.isAlphabet();
    }

    public boolean isAlphanumeric() {
        return impl.isAlphanumeric();
    }

    public boolean isWhiteSpace() {
        return impl.isWhiteSpace();
    }

    public net.kfoundation.scala.UChar toLowerCase() {
        return impl.toLowerCase();
    }

    public net.kfoundation.scala.UChar toUpperCase() {
        return impl.toUpperCase();
    }

    public int getUtf8Length() {
        return impl.getUtf8Length();
    }

    public byte[] toUtf8() {
        return impl.toUtf8();
    }

    public char[] toUtf16() {
        return impl.toUtf16();
    }

    public void printToStream(OutputStream os) {
        impl.writeToStream(os);
    }

    public void appendTo(StringBuilder builder) {
        impl.appendTo(builder);
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
