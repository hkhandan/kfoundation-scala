// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java;


import scala.jdk.CollectionConverters;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * High-performance string with internal UTF-8 encoding.
 *
 * <p>The philosophy behind this class is that, more often than not, text data is
 * stored and read in UTF-8 encoding, and written to output with little or no
 * processing. Most often such process can be done at byte level (such as
 * concatenation), and in cases when iteration of code points is required, it
 * can be done directly over the UTF-8 stream. By avoiding unnecessary encoding and
 * decoding to and from UTF-8, unlike Java String, we can save our CPU
 * resources. Also since UTF-8 is the most compact Unicode representation, we save
 * some memory as well.</p>
 *
 * <p>Use toUtf8() to get raw bytes consisting this string, or if necessary use
 * uCharIterator() to read each character one-by-one.</p>
 *
 * <p>getLength() returns the length of string in characters rather than bytes. To
 * get the number of bytes use getUtf8Length(). substring() methods also
 * work based on character (codepoint) location, as one would naturally expect.</p>
 */
public class UString {

    /**
     * Convenience constant for empty string.
     */
    public static UString EMPTY = new UString(net.kfoundation.scala.UString.EMPTY());


    private final net.kfoundation.scala.UString impl;


    /**
     * Converts a Scala-based UString to Java.
     */
    public UString(net.kfoundation.scala.UString impl) {
        this.impl = impl;
    }


    /**
     * Converts a Scala-based UString to Java.
     */
    public static UString of(net.kfoundation.scala.UString scalaUString) {
        return new UString(scalaUString);
    }


    /**
     * Converts this Java-based UString to scala.
     */
    public net.kfoundation.scala.UString asScala() {
        return impl;
    }



    // --- Delegates --- //

    /**
     * Reads a UString from a UTF-8 encoded stream.
     */
    public static UString readUtf8(InputStream input, int nOctets) {
        return of(net.kfoundation.scala.UString.readUtf8(input, nOctets));
    }


    /**
     * Converts a native String to UString.
     */
    public static UString of(String str) {
        return of(net.kfoundation.scala.UString.of(str));
    }


    /**
     * Constructs a UString containing only the given character.
     */
    public static UString of(UChar ch) {
        return of(net.kfoundation.scala.UString.of(ch.asScala()));
    }


    /**
     * Produces a UString from a raw UTF-8 encoded array of bytes.
     */
    public static UString of(byte[] octets) {
        return of(net.kfoundation.scala.UString.of(octets));
    }


    /**
     * Constructs a UString from the given portion of a raw UTF-8 encoded array
     * of bytes.
     */
    public static UString of(byte[] octets, int offset, int size) {
        return of(net.kfoundation.scala.UString.of(octets, offset, size));
    }


    /**
     * Joins a list of UStrings putting the given delimiter in between them.
     */
    public static UString join(List<UString> strings, UString delimiter) {
        List<net.kfoundation.scala.UObject> scalaList = strings.stream()
            .map(s -> s.impl)
            .collect(Collectors.toList());

        return of(net.kfoundation.scala.UString.join(
            CollectionConverters.ListHasAsScala(scalaList).asScala().toSeq(),
            delimiter.impl));
    }


    /**
     * Returns the UTF-8 encoded representation of this string.
     */
    public byte[] toUtf8() {
        return impl.toUtf8();
    }


    /**
     * Produces an iterator that can walk through this string
     * character-by-character.
     */
    public Iterator<UChar> uCharIterator() {
        scala.collection.Iterator<UChar> it = impl.uCharIterator().map(UChar::of);
        return CollectionConverters.<UChar>IteratorHasAsJava(it).asJava();
    }


    /**
     * Returns the number of characters (codepoints) in this string. The
     * result is computed and cached the first time this method is called.
     */
    public int getLength() {
        return impl.getLength();
    }


    /**
     * Get the number of octets required to store this string in UTF-8 format.
     */
    public int getUtf8Length() {
        return impl.getUtf8Length();
    }


    /**
     * Tests if this string is empty.
     */
    public boolean isEmpty() {
        return impl.isEmpty();
    }


    /**
     * Tests if this string is the same as the given one ignoring cases.
     */
    public boolean equalsIgnoreCase(UString that) {
        return impl.equalsIgnoreCase(that.impl);
    }


    /**
     * Finds and reports the location of the first occurrence of the given
     * octet after the given offset in UTF-8 representation of this string.
     * Returns -1 if not found.
     */
    public int find(byte octet, int offset) {
        return impl.find(octet, offset);
    }


    /**
     * Finds and reports the location of the first occurrence of the given
     * character after the given offset, or -1 if not found. The location
     * is measured in octets from the beginning of the UTF-8 representation
     * of this string.
     */
    public int find(UChar ch, int offset) {
        return impl.find(ch.asScala(), offset);
    }


    /**
     * Finds and reports the location of the first occurrence of the given
     * string after the given offset, or -1 if not found. The location
     * is measured in octets from the beginning of the UTF-8 representation
     * of this string.
     */
    public int find(UString str, int offset) {
        return impl.find(str.impl, offset);
    }


    /**
     * Converts all characters in this string to lower-case.
     */
    public UString toLowerCase() {
        return of(impl.toLowerCase());
    }


    /**
     * Converts all characters in this string to upper-case.
     */
    public UString toUpperCase() {
        return of(impl.toUpperCase());
    }


    /**
     * Converts only the first character of this string to upper-case.
     */
    public UString toFirstUpperCase() {
        return of(impl.toFirstUpperCase());
    }


    /**
     * Returns a desired portion of this string. Begin is inclusive, end
     * is exclusive. Location is measured in number of codepoints (as opposed
     * to number of octets) from the beginning of this string.
     */
    public UString subString(int begin, int end) {
        return of(impl.subString(begin, end));
    }


    /**
     * Returns right portion of this string starting from the character
     * (codepoint) at the given index
     */
    public UString subString(int begin) {
        return of(impl.subString(begin));
    }


    /**
     * Produces a new string by appending the given string to the end of this
     * one.
     */
    public UString append(UString str) {
        return of(impl.append(str.impl));
    }


    /**
     * Produces a new string by appending the given raw UTF-8 encoded string
     * to the end of this one.
     */
    public UString append(byte[] raw) {
        return of(impl.append(raw));
    }


    /**
     * Produces a new string by appending a character to the end of this string.
     */
    public UString append(UChar ch) {
        return of(impl.append(ch.asScala()));
    }


    /**
     * Writes the contents of this string to the given OutputStream in UTF-8
     * format.
     */
    public void printToStream(OutputStream os) {
        impl.writeToStream(os);
    }


    /**
     * Converts this UString to native String.
     */
    @Override
    public String toString() {
        return impl.toString();
    }

    @Override
    public int hashCode() {
        return impl.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return impl.equals(o);
    }

}
