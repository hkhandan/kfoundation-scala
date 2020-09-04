package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

public interface ObjectSerializer {
    ObjectSerializer writePropertyName(UString name);
    ObjectSerializer writeLiteral(UString value);
    ObjectSerializer writeLiteral(long value);
    ObjectSerializer writeLiteral(double value);
    ObjectSerializer writeLiteral(boolean value);
    ObjectSerializer writeNull();
    ObjectSerializer writeObjectBegin(UString name);
    ObjectSerializer writeObjectEnd();
    ObjectSerializer writeCollectionBegin();
    ObjectSerializer writeCollectionEnd();
    void writeStreamEnd();
}
