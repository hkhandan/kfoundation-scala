package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;
import net.kfoundation.java.io.Path;
import scala.jdk.javaapi.OptionConverters;

import java.util.Optional;


public class K4ObjectDeserializer implements ObjectDeserializer {

    private final net.kfoundation.scala.serialization.K4ObjectDeserializer impl;

    public K4ObjectDeserializer(Path p) {
        impl = new net.kfoundation.scala.serialization.K4ObjectDeserializer(p.toScala());
    }

    @Override
    public Optional<UString> readObjectBegin() {
        return OptionConverters.toJava(
            impl.readObjectBegin().value().map(UString::of));
    }

    @Override
    public Optional<UString> readObjectEnd() {
        return OptionConverters.toJava(
            impl.readObjectEnd().value().map(UString::of));
    }

    @Override
    public void readCollectionBegin() {
        impl.readCollectionBegin();
    }

    @Override
    public boolean tryReadCollectionEnd() {
        return impl.tryReadCollectionEnd().isDefined();
    }

    @Override
    public UString readStringLiteral() {
        return UString.of(impl.readStringLiteral().value());
    }

    @Override
    public long readIntegerLiteral() {
        return (Long)impl.readIntegerLiteral().value();
    }

    @Override
    public double readDecimalLiteral() {
        return (Double)impl.readDecimalLiteral().value();
    }

    @Override
    public boolean readBooleanLiteral() {
        return (Boolean)impl.readBooleanLiteral().value();
    }

    @Override
    public Optional<UString> tryReadPropertyName() {
        return OptionConverters.toJava(
            impl.tryReadPropertyName().map(t -> UString.of(t.value())));
    }
}
