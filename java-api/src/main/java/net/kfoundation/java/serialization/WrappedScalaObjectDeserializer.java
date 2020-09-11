package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;
import scala.jdk.javaapi.OptionConverters;

import java.util.Optional;



public class WrappedScalaObjectDeserializer implements ObjectDeserializer {
    private final net.kfoundation.scala.serialization.ObjectDeserializer impl;


    public WrappedScalaObjectDeserializer(
        net.kfoundation.scala.serialization.ObjectDeserializer impl)
    {
        this.impl = impl;
    }


    @Override
    public Optional<UString> readObjectBegin() {
        return OptionConverters.toJava(
                impl.readObjectBegin().map(UString::of));
    }


    @Override
    public Optional<UString> readObjectEnd() {
        return OptionConverters.toJava(
                impl.readObjectEnd().map(UString::of));
    }


    @Override
    public void readCollectionBegin() {
        impl.readCollectionBegin();
    }


    @Override
    public boolean tryReadCollectionEnd() {
        return impl.tryReadCollectionEnd();
    }


    @Override
    public UString readStringLiteral() {
        return UString.of(impl.readStringLiteral());
    }


    @Override
    public long readIntegerLiteral() {
        return impl.readIntegerLiteral();
    }


    @Override
    public double readDecimalLiteral() {
        return impl.readDecimalLiteral();
    }


    @Override
    public boolean readBooleanLiteral() {
        return impl.readBooleanLiteral();
    }


    @Override
    public Optional<UString> tryReadPropertyName() {
        return OptionConverters.toJava(
                impl.tryReadPropertyName().map(UString::of));
    }
}
