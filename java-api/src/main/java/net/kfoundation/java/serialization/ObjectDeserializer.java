package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

import java.util.Optional;


public interface ObjectDeserializer {
    Optional<UString> readObjectBegin();
    Optional<UString> readObjectEnd();
    void readCollectionBegin();
    boolean tryReadCollectionEnd();
    UString readStringLiteral();
    long readIntegerLiteral();
    double readDecimalLiteral();
    boolean readBooleanLiteral();
    Optional<UString> tryReadPropertyName();
}