package net.kfoundation.java;


import scala.jdk.CollectionConverters;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;



public class UString {

    public static UString EMPTY = new UString(net.kfoundation.scala.UString.EMPTY());

    private final net.kfoundation.scala.UString impl;

    public UString(net.kfoundation.scala.UString impl) {
        this.impl = impl;
    }

    public static UString of(net.kfoundation.scala.UString scalaUString) {
        return new UString(scalaUString);
    }

    public net.kfoundation.scala.UString asScala() {
        return impl;
    }



    // --- Delegates --- //

    public static UString readUtf8(InputStream input, int nOctets) {
        return of(net.kfoundation.scala.UString.readUtf8(input, nOctets));
    }

    public static UString of(String str) {
        return of(net.kfoundation.scala.UString.of(str));
    }

    public static UString of(UChar ch) {
        return of(net.kfoundation.scala.UString.of(ch.asScala()));
    }

    public static UString of(byte[] octets) {
        return of(net.kfoundation.scala.UString.of(octets));
    }

    public static UString of(byte[] octets, int offset, int size) {
        return of(net.kfoundation.scala.UString.of(octets, offset, size));
    }

    public static UString join(List<UString> strings, UString delimiter) {
        List<net.kfoundation.scala.UString> scalaList = strings.stream()
            .map(s -> s.impl)
            .collect(Collectors.toList());

        return of(net.kfoundation.scala.UString.join(
            CollectionConverters.ListHasAsScala(scalaList).asScala().toSeq(),
            delimiter.impl));
    }

    public byte[] toUtf8() {
        return impl.toUtf8();
    }

    public Iterator<UChar> uCharIterator() {
        scala.collection.Iterator<UChar> it = impl.uCharIterator().map(UChar::of);
        return CollectionConverters.<UChar>IteratorHasAsJava(it).asJava();
    }

    public int getLength() {
        return impl.getLength();
    }

    public int getUtf8Length() {
        return impl.getUtf8Length();
    }

    public boolean isEmpty() {
        return impl.isEmpty();
    }

    public boolean equalsIgnoreCase(UString that) {
        return impl.equalsIgnoreCase(that.impl);
    }

    public int find(byte octet, int offset) {
        return impl.find(octet, offset);
    }

    public int find(UChar ch, int offset) {
        return impl.find(ch.asScala(), offset);
    }

    public int find(UString str, int offset) {
        return impl.find(str.impl, offset);
    }

    public UString toLowerCase() {
        return of(impl.toLowerCase());
    }

    public UString toUpperCase() {
        return of(impl.toUpperCase());
    }

    public UString toFirstUpperCase() {
        return of(impl.toFirstUpperCase());
    }

    public UString subString(int begin, int end) {
        return of(impl.subString(begin, end));
    }

    public UString subString(int begin) {
        return of(impl.subString(begin));
    }

    public UString append(UString str) {
        return of(impl.append(str.impl));
    }

    public UString append(byte[] raw) {
        return of(impl.append(raw));
    }

    public UString append(UChar ch) {
        return of(impl.append(ch.asScala()));
    }

    public void printToStream(OutputStream os) {
        impl.printToStream(os);
    }

    public net.kfoundation.scala.UString $plus(net.kfoundation.scala.UString v) {
        return impl.$plus(v);
    }

    public net.kfoundation.scala.UString $plus(Object o) {
        return impl.$plus(o);
    }

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
