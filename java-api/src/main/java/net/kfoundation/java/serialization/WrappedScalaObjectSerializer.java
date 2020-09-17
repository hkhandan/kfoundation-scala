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
 * Used to port Scala-based object serializers to Java.
 */
public class WrappedScalaObjectSerializer implements ObjectSerializer {

    private final net.kfoundation.scala.serialization.ObjectSerializer impl;


    public WrappedScalaObjectSerializer(
        net.kfoundation.scala.serialization.ObjectSerializer impl)
    {
        this.impl = impl;
    }


    @Override
    public ObjectSerializer writePropertyName(UString name) {
        impl.writePropertyName(name.asScala());
        return this;
    }


    @Override
    public ObjectSerializer writeLiteral(UString value) {
        impl.writeLiteral(value.asScala());
        return this;
    }


    @Override
    public ObjectSerializer writeLiteral(long value) {
        impl.writeLiteral(value);
        return this;
    }


    @Override
    public ObjectSerializer writeLiteral(double value) {
        impl.writeLiteral(value);
        return this;
    }


    @Override
    public ObjectSerializer writeLiteral(boolean value) {
        impl.writeLiteral(value);
        return this;
    }


    @Override
    public ObjectSerializer writeNull() {
        impl.writeNull();
        return this;
    }


    @Override
    public ObjectSerializer writeObjectBegin(UString name) {
        impl.writeObjectBegin(name.asScala());
        return this;
    }


    @Override
    public ObjectSerializer writeObjectEnd() {
        impl.writeObjectEnd();
        return this;
    }


    @Override
    public ObjectSerializer writeCollectionBegin() {
        impl.writeCollectionBegin();
        return this;
    }


    @Override
    public ObjectSerializer writeCollectionEnd() {
        impl.writeCollectionEnd();
        return this;
    }


    @Override
    public void writeStreamEnd() {
        impl.writeStreamEnd();
    }

}
